/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncStarlightNetwork;
import hellfirepvp.astralsorcery.common.starlight.transmission.NodeConnection;
import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionLink;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central per-dimension manager for the starlight transmission network.
 * Maintains a directed graph of sources, transmission nodes, and receivers
 * connected by {@link TransmissionLink}s. Each server tick, starlight flows
 * from sources through the graph to receivers, applying efficiency losses
 * at each transmission node.
 *
 * <p>Persists via {@link SavedData} (1.20 replacement for WorldSavedData).
 * One instance exists per dimension, obtained through
 * {@link #getOrCreate(ServerLevel)}.</p>
 *
 * <p>Graph topology changes (node add/remove, link add/remove) trigger
 * a client sync via {@link PktSyncStarlightNetwork} so beam rendering
 * stays up to date.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * WorldSavedData -> SavedData,
 * getOrCreate uses ServerLevel.getDataStorage().computeIfAbsent(),
 * RegistryKey&lt;World&gt; -> ResourceKey&lt;Level&gt;,
 * CompoundNBT -> CompoundTag, ListNBT -> ListTag</p>
 */
public class WorldNetworkHandler extends SavedData {

    private static final String DATA_NAME = AstralSorcery.MODID + "_starlight_network";

    /** Maximum distance (blocks) for a valid starlight link. */
    private static final double MAX_LINK_DISTANCE = 64.0;

    /** Maximum depth for BFS graph traversal (prevents infinite loops). */
    private static final int MAX_TRAVERSAL_DEPTH = 32;

    @Nonnull
    private final ResourceKey<Level> dimension;

    // ---- Node maps keyed by BlockPos ----
    @Nonnull
    private final Map<BlockPos, SourceEntry> sources = new HashMap<>();
    @Nonnull
    private final Map<BlockPos, ReceiverEntry> receivers = new HashMap<>();
    @Nonnull
    private final Map<BlockPos, TransmissionEntry> transmissions = new HashMap<>();

    // ---- Link set ----
    @Nonnull
    private final Set<TransmissionLink> activeLinks = new LinkedHashSet<>();

    // ---- Independent sources (persist across chunk unloads) ----
    @Nonnull
    private final Map<BlockPos, CompoundTag> independentSourceData = new HashMap<>();

    // ---- Dirty tracking for sync ----
    @Nonnull
    private final Set<BlockPos> dirtySourcePositions = new HashSet<>();

    // ---- Cached adjacency list for fast traversal ----
    @Nonnull
    private final Map<BlockPos, List<BlockPos>> adjacency = new HashMap<>();
    private boolean adjacencyDirty = true;

    /**
     * Private constructor — use {@link #getOrCreate(ServerLevel)} to obtain an instance.
     */
    private WorldNetworkHandler(@Nonnull ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    /**
     * Gets or creates the starlight network handler for the given server level.
     *
     * @param level the server level (dimension)
     * @return the network handler for that dimension
     */
    @Nonnull
    public static WorldNetworkHandler getOrCreate(@Nonnull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> load(tag, level.dimension()),
                () -> new WorldNetworkHandler(level.dimension()),
                DATA_NAME
        );
    }

    /**
     * Gets the starlight network handler if it already exists for this level.
     * Does not create a new handler if absent — returns null instead.
     * Used by tick handlers that should not force-create data storage entries.
     *
     * @param level the server level (dimension)
     * @return the existing handler, or null if not yet initialized
     */
    @Nullable
    public static WorldNetworkHandler getIfPresent(@Nonnull ServerLevel level) {
        return level.getDataStorage().get(
                tag -> load(tag, level.dimension()),
                DATA_NAME
        );
    }

    @Nonnull
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    // ========================================================================
    // Source management
    // ========================================================================

    /**
     * Registers a starlight source at the given position.
     *
     * @param pos           source block position
     * @param constellation attuned constellation, or null if unattuned
     * @param autoLink      whether this source provides auto-linking (independent source)
     */
    public void registerSource(@Nonnull BlockPos pos,
                               @Nullable ResourceLocation constellation,
                               boolean autoLink) {
        pos = pos.immutable();
        SourceEntry entry = new SourceEntry(pos, constellation, autoLink);
        sources.put(pos, entry);
        setDirty();
        adjacencyDirty = true;
        dirtySourcePositions.add(pos);
        AstralSorcery.log.debug("Registered starlight source at {} in {}",
                pos.toShortString(), dimension.location());
    }

    /**
     * Removes a starlight source and all links originating from it.
     */
    public void removeSource(@Nonnull BlockPos pos) {
        pos = pos.immutable();
        if (sources.remove(pos) != null) {
            removeAllLinksFrom(pos);
            independentSourceData.remove(pos);
            setDirty();
            adjacencyDirty = true;
            dirtySourcePositions.add(pos);
            AstralSorcery.log.debug("Removed starlight source at {} in {}",
                    pos.toShortString(), dimension.location());
        }
    }

    /**
     * Updates a source's attuned constellation.
     */
    public void updateSourceConstellation(@Nonnull BlockPos pos,
                                          @Nullable ResourceLocation constellation) {
        SourceEntry entry = sources.get(pos);
        if (entry != null) {
            entry.constellation = constellation;
            setDirty();
            dirtySourcePositions.add(pos.immutable());
        }
    }

    /**
     * Stores independent source NBT data for cross-chunk persistence.
     */
    public void storeIndependentSourceData(@Nonnull BlockPos pos, @Nonnull CompoundTag data) {
        independentSourceData.put(pos.immutable(), data.copy());
        setDirty();
    }

    /**
     * Retrieves independent source NBT data, or null if none stored.
     */
    @Nullable
    public CompoundTag getIndependentSourceData(@Nonnull BlockPos pos) {
        CompoundTag data = independentSourceData.get(pos);
        return data != null ? data.copy() : null;
    }

    @Nonnull
    public Set<BlockPos> getSourcePositions() {
        return Collections.unmodifiableSet(sources.keySet());
    }

    public boolean hasSource(@Nonnull BlockPos pos) {
        return sources.containsKey(pos);
    }

    @Nullable
    public SourceEntry getSource(@Nonnull BlockPos pos) {
        return sources.get(pos);
    }

    // ========================================================================
    // Receiver management
    // ========================================================================

    /**
     * Registers a starlight receiver at the given position.
     *
     * @param pos      receiver block position
     * @param maxInput maximum starlight this receiver can accept per tick
     */
    public void registerReceiver(@Nonnull BlockPos pos, double maxInput) {
        pos = pos.immutable();
        receivers.put(pos, new ReceiverEntry(pos, maxInput));
        setDirty();
        adjacencyDirty = true;
        AstralSorcery.log.debug("Registered starlight receiver at {} in {}",
                pos.toShortString(), dimension.location());
    }

    /**
     * Removes a starlight receiver and all links targeting it.
     */
    public void removeReceiver(@Nonnull BlockPos pos) {
        pos = pos.immutable();
        if (receivers.remove(pos) != null) {
            removeAllLinksTo(pos);
            setDirty();
            adjacencyDirty = true;
            AstralSorcery.log.debug("Removed starlight receiver at {} in {}",
                    pos.toShortString(), dimension.location());
        }
    }

    public boolean hasReceiver(@Nonnull BlockPos pos) {
        return receivers.containsKey(pos);
    }

    @Nonnull
    public Set<BlockPos> getReceiverPositions() {
        return Collections.unmodifiableSet(receivers.keySet());
    }

    // ========================================================================
    // Transmission node management
    // ========================================================================

    /**
     * Registers a transmission (relay) node at the given position.
     *
     * @param pos        node block position
     * @param efficiency transmission efficiency [0, 1]
     */
    public void registerTransmission(@Nonnull BlockPos pos, double efficiency) {
        pos = pos.immutable();
        transmissions.put(pos, new TransmissionEntry(pos, efficiency));
        setDirty();
        adjacencyDirty = true;
        AstralSorcery.log.debug("Registered transmission node at {} in {}",
                pos.toShortString(), dimension.location());
    }

    /**
     * Removes a transmission node and all links through it.
     */
    public void removeTransmission(@Nonnull BlockPos pos) {
        pos = pos.immutable();
        if (transmissions.remove(pos) != null) {
            removeAllLinksFrom(pos);
            removeAllLinksTo(pos);
            setDirty();
            adjacencyDirty = true;
            AstralSorcery.log.debug("Removed transmission node at {} in {}",
                    pos.toShortString(), dimension.location());
        }
    }

    public boolean hasTransmission(@Nonnull BlockPos pos) {
        return transmissions.containsKey(pos);
    }

    /**
     * Whether the given position is any registered node type (source, receiver, or transmission).
     */
    public boolean hasNode(@Nonnull BlockPos pos) {
        return sources.containsKey(pos)
                || receivers.containsKey(pos)
                || transmissions.containsKey(pos);
    }

    /**
     * Removes any node (source, receiver, or transmission) at the given position.
     * Convenience method for block break handling.
     */
    public void removeNode(@Nonnull BlockPos pos) {
        removeSource(pos);
        removeReceiver(pos);
        removeTransmission(pos);
    }

    // ========================================================================
    // Link management
    // ========================================================================

    /**
     * Adds a directed link from one node to another.
     * Both positions must be registered nodes and within range.
     *
     * @param from source or transmission node position
     * @param to   transmission node or receiver position
     * @return true if the link was added, false if invalid
     */
    public boolean addLink(@Nonnull BlockPos from, @Nonnull BlockPos to) {
        from = from.immutable();
        to = to.immutable();

        if (from.equals(to)) {
            return false;
        }
        if (!isKnownNode(from) || !isKnownNode(to)) {
            AstralSorcery.log.debug("Rejected link {} -> {}: one or both positions not registered",
                    from.toShortString(), to.toShortString());
            return false;
        }
        if (from.distSqr(to) > MAX_LINK_DISTANCE * MAX_LINK_DISTANCE) {
            AstralSorcery.log.debug("Rejected link {} -> {}: exceeds max distance {}",
                    from.toShortString(), to.toShortString(), MAX_LINK_DISTANCE);
            return false;
        }
        // Receivers cannot be link sources
        if (receivers.containsKey(from) && !sources.containsKey(from) && !transmissions.containsKey(from)) {
            return false;
        }
        // Sources cannot be link targets (unless also a transmission)
        if (sources.containsKey(to) && !transmissions.containsKey(to) && !receivers.containsKey(to)) {
            return false;
        }

        TransmissionLink link = new TransmissionLink(from, to);
        if (activeLinks.add(link)) {
            setDirty();
            adjacencyDirty = true;
            dirtySourcePositions.addAll(findUpstreamSources(from));
            AstralSorcery.log.debug("Added starlight link: {}", link);
            return true;
        }
        return false;
    }

    /**
     * Removes a directed link between two nodes.
     *
     * @return true if the link existed and was removed
     */
    public boolean removeLink(@Nonnull BlockPos from, @Nonnull BlockPos to) {
        TransmissionLink link = new TransmissionLink(from.immutable(), to.immutable());
        if (activeLinks.remove(link)) {
            setDirty();
            adjacencyDirty = true;
            dirtySourcePositions.addAll(findUpstreamSources(from));
            AstralSorcery.log.debug("Removed starlight link: {}", link);
            return true;
        }
        return false;
    }

    @Nonnull
    public Set<TransmissionLink> getActiveLinks() {
        return Collections.unmodifiableSet(activeLinks);
    }

    /**
     * Gets all outgoing link targets from a position.
     */
    @Nonnull
    public List<BlockPos> getOutgoingTargets(@Nonnull BlockPos from) {
        rebuildAdjacencyIfNeeded();
        return adjacency.getOrDefault(from, Collections.emptyList());
    }

    /**
     * Gets all incoming link sources to a position.
     */
    @Nonnull
    public List<BlockPos> getIncomingSources(@Nonnull BlockPos to) {
        return activeLinks.stream()
                .filter(link -> link.getTo().equals(to))
                .map(TransmissionLink::getFrom)
                .collect(Collectors.toList());
    }

    // ---- Internal link helpers ----

    private void removeAllLinksFrom(@Nonnull BlockPos from) {
        activeLinks.removeIf(link -> link.getFrom().equals(from));
    }

    private void removeAllLinksTo(@Nonnull BlockPos to) {
        activeLinks.removeIf(link -> link.getTo().equals(to));
    }

    private boolean isKnownNode(@Nonnull BlockPos pos) {
        return sources.containsKey(pos)
                || receivers.containsKey(pos)
                || transmissions.containsKey(pos);
    }

    // ========================================================================
    // Starlight distribution (server tick)
    // ========================================================================

    /**
     * Distributes starlight from all sources through the graph to receivers.
     * Called once per server tick per dimension by the network tick handler.
     *
     * <p>Algorithm:
     * For each source, BFS through outgoing links. At each transmission node
     * multiply by that node's efficiency. When reaching a receiver, deliver
     * the remaining starlight (capped by receiver max input). If multiple
     * sources feed the same receiver, starlight accumulates additively per tick.</p>
     *
     * @param level the server level to resolve block entities from
     */
    public void tick(@Nonnull ServerLevel level) {
        rebuildAdjacencyIfNeeded();

        // Reset receiver accumulation for this tick
        Map<BlockPos, Double> receiverAccumulation = new HashMap<>();
        Map<BlockPos, ResourceLocation> receiverConstellation = new HashMap<>();

        for (Map.Entry<BlockPos, SourceEntry> entry : sources.entrySet()) {
            BlockPos sourcePos = entry.getKey();
            SourceEntry source = entry.getValue();

            // Resolve actual starlight production from the block entity
            double production = resolveSourceProduction(level, sourcePos, source);
            if (production <= 0) {
                continue;
            }

            // BFS from this source
            distributeFromSource(sourcePos, source.constellation, production,
                    receiverAccumulation, receiverConstellation);
        }

        // Deliver accumulated starlight to receivers
        for (Map.Entry<BlockPos, Double> entry : receiverAccumulation.entrySet()) {
            BlockPos receiverPos = entry.getKey();
            double amount = entry.getValue();
            ReceiverEntry receiverEntry = receivers.get(receiverPos);
            if (receiverEntry == null) {
                continue;
            }

            // Cap at receiver's max input
            double delivered = Math.min(amount, receiverEntry.maxInput);
            ResourceLocation constellation = receiverConstellation.get(receiverPos);

            deliverStarlight(level, receiverPos, delivered, constellation);
        }

        // Sync dirty sources to clients
        syncDirtySources(level);
    }

    /**
     * BFS traversal from a single source, accumulating starlight at reached receivers.
     */
    private void distributeFromSource(@Nonnull BlockPos sourcePos,
                                      @Nullable ResourceLocation constellation,
                                      double production,
                                      @Nonnull Map<BlockPos, Double> receiverAccumulation,
                                      @Nonnull Map<BlockPos, ResourceLocation> receiverConstellation) {
        // BFS queue: (position, remaining starlight at that position)
        Deque<StarlightWave> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(new StarlightWave(sourcePos, production));
        visited.add(sourcePos);

        int depth = 0;
        while (!queue.isEmpty() && depth < MAX_TRAVERSAL_DEPTH) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                StarlightWave wave = queue.poll();
                if (wave == null) break;

                List<BlockPos> targets = adjacency.getOrDefault(wave.pos, Collections.emptyList());
                if (targets.isEmpty()) {
                    continue;
                }

                // Split starlight evenly among targets
                double perTarget = wave.starlight / targets.size();

                for (BlockPos target : targets) {
                    if (visited.contains(target)) {
                        continue; // prevent cycles
                    }

                    double arriving = perTarget;

                    // Apply transmission efficiency if target is a transmission node
                    TransmissionEntry transmission = transmissions.get(target);
                    if (transmission != null) {
                        arriving *= transmission.efficiency;
                        visited.add(target);
                        queue.add(new StarlightWave(target, arriving));
                    }

                    // Accumulate at receiver
                    if (receivers.containsKey(target)) {
                        receiverAccumulation.merge(target, arriving, Double::sum);
                        // First constellation to reach a receiver wins (source priority)
                        receiverConstellation.putIfAbsent(target, constellation);
                        visited.add(target);
                    }
                }
            }
            depth++;
        }
    }

    /**
     * Resolves actual starlight production from a source's block entity.
     * Falls back to independent source data if the chunk is unloaded.
     */
    private double resolveSourceProduction(@Nonnull ServerLevel level,
                                           @Nonnull BlockPos sourcePos,
                                           @Nonnull SourceEntry source) {
        if (level.isLoaded(sourcePos)) {
            BlockEntity be = level.getBlockEntity(sourcePos);
            if (be instanceof IStarlightSource starlightSource) {
                double prod = starlightSource.getStarlightProduction();

                // Update independent source data if applicable
                if (be instanceof IIndependentStarlightSource independentSource) {
                    independentSourceData.put(sourcePos, independentSource.serializeSourceNBT());
                }

                return starlightSource.hasSkyAccess() ? prod : 0;
            }
            // Block entity exists but doesn't implement IStarlightSource — stale registration
            return 0;
        }

        // Chunk unloaded — use cached independent data if the source supports auto-link
        if (source.autoLink) {
            CompoundTag cached = independentSourceData.get(sourcePos);
            if (cached != null && cached.contains("cachedProduction")) {
                return cached.getDouble("cachedProduction");
            }
        }
        return 0;
    }

    /**
     * Delivers starlight to a receiver's block entity.
     */
    private void deliverStarlight(@Nonnull ServerLevel level,
                                  @Nonnull BlockPos receiverPos,
                                  double amount,
                                  @Nullable ResourceLocation constellation) {
        if (!level.isLoaded(receiverPos)) {
            return;
        }
        BlockEntity be = level.getBlockEntity(receiverPos);
        if (be instanceof IStarlightReceiver receiver) {
            receiver.receiveStarlight(amount, constellation);
        }
    }

    // ========================================================================
    // Adjacency graph
    // ========================================================================

    private void rebuildAdjacencyIfNeeded() {
        if (!adjacencyDirty) {
            return;
        }
        adjacency.clear();
        for (TransmissionLink link : activeLinks) {
            adjacency.computeIfAbsent(link.getFrom(), k -> new ArrayList<>())
                    .add(link.getTo());
        }
        adjacencyDirty = false;
    }

    /**
     * Finds all source positions that are upstream of the given position
     * (i.e., sources that could route starlight through this position).
     * Used to mark sources dirty when downstream topology changes.
     */
    @Nonnull
    private Set<BlockPos> findUpstreamSources(@Nonnull BlockPos pos) {
        Set<BlockPos> upstream = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);
        visited.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (sources.containsKey(current)) {
                upstream.add(current);
            }
            // Walk backward through links
            for (TransmissionLink link : activeLinks) {
                if (link.getTo().equals(current) && visited.add(link.getFrom())) {
                    queue.add(link.getFrom());
                }
            }
        }
        return upstream;
    }

    // ========================================================================
    // Client synchronization
    // ========================================================================

    /**
     * Sends network sync packets for all dirty sources to tracking clients.
     */
    private void syncDirtySources(@Nonnull ServerLevel level) {
        for (BlockPos sourcePos : dirtySourcePositions) {
            List<BlockPos> reachableTargets = computeReachableTargets(sourcePos);
            SourceEntry source = sources.get(sourcePos);
            double starlight = source != null ? resolveSourceProduction(level, sourcePos, source) : 0;

            PktSyncStarlightNetwork pkt = new PktSyncStarlightNetwork(
                    sourcePos, reachableTargets, starlight);
            PacketChannel.sendToAllTracking(pkt, level, sourcePos);
        }
        dirtySourcePositions.clear();
    }

    /**
     * Computes all positions reachable from a source via BFS for client rendering.
     */
    @Nonnull
    private List<BlockPos> computeReachableTargets(@Nonnull BlockPos sourcePos) {
        rebuildAdjacencyIfNeeded();
        List<BlockPos> reachable = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(sourcePos);
        visited.add(sourcePos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            List<BlockPos> targets = adjacency.getOrDefault(current, Collections.emptyList());
            for (BlockPos target : targets) {
                if (visited.add(target)) {
                    reachable.add(target);
                    // Continue BFS through transmission nodes
                    if (transmissions.containsKey(target)) {
                        queue.add(target);
                    }
                }
            }
        }
        return reachable;
    }

    /**
     * Forces a full sync of all sources to the given player (e.g., on login or dimension change).
     */
    public void syncAllToPlayer(@Nonnull net.minecraft.server.level.ServerPlayer player,
                                @Nonnull ServerLevel level) {
        for (Map.Entry<BlockPos, SourceEntry> entry : sources.entrySet()) {
            BlockPos sourcePos = entry.getKey();
            List<BlockPos> reachable = computeReachableTargets(sourcePos);
            double starlight = resolveSourceProduction(level, sourcePos, entry.getValue());

            PktSyncStarlightNetwork pkt = new PktSyncStarlightNetwork(
                    sourcePos, reachable, starlight);
            PacketChannel.sendToPlayer(pkt, player);
        }
    }

    // ========================================================================
    // Network info queries (for commands, debug)
    // ========================================================================

    /**
     * Gets a {@link NodeConnection} view of the node at the given position.
     *
     * @return the node connection, or null if not a registered node
     */
    @Nullable
    public NodeConnection getNodeInfo(@Nonnull BlockPos pos) {
        if (!hasNode(pos)) {
            return null;
        }
        NodeConnection node = new NodeConnection(pos, dimension);
        node.setSource(sources.containsKey(pos));
        node.setReceiver(receivers.containsKey(pos));
        node.setTransmission(transmissions.containsKey(pos));

        rebuildAdjacencyIfNeeded();
        List<BlockPos> targets = adjacency.getOrDefault(pos, Collections.emptyList());
        for (BlockPos target : targets) {
            node.addConnection(target);
        }
        return node;
    }

    /**
     * Gets the total number of registered nodes in this dimension.
     */
    public int getNodeCount() {
        Set<BlockPos> allPositions = new HashSet<>();
        allPositions.addAll(sources.keySet());
        allPositions.addAll(receivers.keySet());
        allPositions.addAll(transmissions.keySet());
        return allPositions.size();
    }

    /**
     * Gets the total number of active links in this dimension.
     */
    public int getLinkCount() {
        return activeLinks.size();
    }

    // ========================================================================
    // NBT serialization
    // ========================================================================

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag) {
        // Sources
        ListTag sourceList = new ListTag();
        for (SourceEntry source : sources.values()) {
            sourceList.add(source.writeToNBT());
        }
        tag.put("sources", sourceList);

        // Receivers
        ListTag receiverList = new ListTag();
        for (ReceiverEntry receiver : receivers.values()) {
            receiverList.add(receiver.writeToNBT());
        }
        tag.put("receivers", receiverList);

        // Transmissions
        ListTag transmissionList = new ListTag();
        for (TransmissionEntry transmission : transmissions.values()) {
            transmissionList.add(transmission.writeToNBT());
        }
        tag.put("transmissions", transmissionList);

        // Links
        ListTag linkList = new ListTag();
        for (TransmissionLink link : activeLinks) {
            linkList.add(link.writeToNBT());
        }
        tag.put("links", linkList);

        // Independent source data
        ListTag independentList = new ListTag();
        for (Map.Entry<BlockPos, CompoundTag> entry : independentSourceData.entrySet()) {
            CompoundTag indTag = new CompoundTag();
            indTag.put("pos", NBTHelper.writeBlockPosToNBT(entry.getKey(), new CompoundTag()));
            indTag.put("data", entry.getValue().copy());
            independentList.add(indTag);
        }
        tag.put("independentSources", independentList);

        return tag;
    }

    /**
     * Loads a WorldNetworkHandler from NBT data.
     */
    @Nonnull
    private static WorldNetworkHandler load(@Nonnull CompoundTag tag,
                                            @Nonnull ResourceKey<Level> dimension) {
        WorldNetworkHandler handler = new WorldNetworkHandler(dimension);

        // Sources
        ListTag sourceList = tag.getList("sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < sourceList.size(); i++) {
            SourceEntry source = SourceEntry.readFromNBT(sourceList.getCompound(i));
            handler.sources.put(source.pos, source);
        }

        // Receivers
        ListTag receiverList = tag.getList("receivers", Tag.TAG_COMPOUND);
        for (int i = 0; i < receiverList.size(); i++) {
            ReceiverEntry receiver = ReceiverEntry.readFromNBT(receiverList.getCompound(i));
            handler.receivers.put(receiver.pos, receiver);
        }

        // Transmissions
        ListTag transmissionList = tag.getList("transmissions", Tag.TAG_COMPOUND);
        for (int i = 0; i < transmissionList.size(); i++) {
            TransmissionEntry transmission = TransmissionEntry.readFromNBT(
                    transmissionList.getCompound(i));
            handler.transmissions.put(transmission.pos, transmission);
        }

        // Links
        ListTag linkList = tag.getList("links", Tag.TAG_COMPOUND);
        for (int i = 0; i < linkList.size(); i++) {
            handler.activeLinks.add(TransmissionLink.readFromNBT(linkList.getCompound(i)));
        }

        // Independent source data
        ListTag independentList = tag.getList("independentSources", Tag.TAG_COMPOUND);
        for (int i = 0; i < independentList.size(); i++) {
            CompoundTag indTag = independentList.getCompound(i);
            BlockPos pos = NBTHelper.readBlockPosFromNBT(indTag.getCompound("pos"));
            CompoundTag data = indTag.getCompound("data").copy();
            handler.independentSourceData.put(pos, data);
        }

        handler.adjacencyDirty = true;
        AstralSorcery.log.info("Loaded starlight network for {}: {} sources, {} receivers, " +
                        "{} transmissions, {} links",
                dimension.location(),
                handler.sources.size(),
                handler.receivers.size(),
                handler.transmissions.size(),
                handler.activeLinks.size());

        return handler;
    }

    // ========================================================================
    // Inner data classes
    // ========================================================================

    /**
     * Tracks a registered starlight source.
     */
    public static class SourceEntry {

        @Nonnull
        private final BlockPos pos;
        @Nullable
        private ResourceLocation constellation;
        private final boolean autoLink;

        SourceEntry(@Nonnull BlockPos pos,
                    @Nullable ResourceLocation constellation,
                    boolean autoLink) {
            this.pos = pos.immutable();
            this.constellation = constellation;
            this.autoLink = autoLink;
        }

        @Nonnull
        public BlockPos getPos() {
            return pos;
        }

        @Nullable
        public ResourceLocation getConstellation() {
            return constellation;
        }

        public boolean isAutoLink() {
            return autoLink;
        }

        @Nonnull
        CompoundTag writeToNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("pos", NBTHelper.writeBlockPosToNBT(pos, new CompoundTag()));
            if (constellation != null) {
                tag.putString("constellation", constellation.toString());
            }
            tag.putBoolean("autoLink", autoLink);
            return tag;
        }

        @Nonnull
        static SourceEntry readFromNBT(@Nonnull CompoundTag tag) {
            BlockPos pos = NBTHelper.readBlockPosFromNBT(tag.getCompound("pos"));
            ResourceLocation constellation = tag.contains("constellation")
                    ? new ResourceLocation(tag.getString("constellation")) : null;
            boolean autoLink = tag.getBoolean("autoLink");
            return new SourceEntry(pos, constellation, autoLink);
        }
    }

    /**
     * Tracks a registered starlight receiver.
     */
    public static class ReceiverEntry {

        @Nonnull
        private final BlockPos pos;
        private final double maxInput;

        ReceiverEntry(@Nonnull BlockPos pos, double maxInput) {
            this.pos = pos.immutable();
            this.maxInput = maxInput;
        }

        @Nonnull
        public BlockPos getPos() {
            return pos;
        }

        public double getMaxInput() {
            return maxInput;
        }

        @Nonnull
        CompoundTag writeToNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("pos", NBTHelper.writeBlockPosToNBT(pos, new CompoundTag()));
            tag.putDouble("maxInput", maxInput);
            return tag;
        }

        @Nonnull
        static ReceiverEntry readFromNBT(@Nonnull CompoundTag tag) {
            BlockPos pos = NBTHelper.readBlockPosFromNBT(tag.getCompound("pos"));
            double maxInput = tag.getDouble("maxInput");
            return new ReceiverEntry(pos, maxInput);
        }
    }

    /**
     * Tracks a registered transmission node.
     */
    public static class TransmissionEntry {

        @Nonnull
        private final BlockPos pos;
        private final double efficiency;

        TransmissionEntry(@Nonnull BlockPos pos, double efficiency) {
            this.pos = pos.immutable();
            this.efficiency = Math.max(0, Math.min(1, efficiency));
        }

        @Nonnull
        public BlockPos getPos() {
            return pos;
        }

        public double getEfficiency() {
            return efficiency;
        }

        @Nonnull
        CompoundTag writeToNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("pos", NBTHelper.writeBlockPosToNBT(pos, new CompoundTag()));
            tag.putDouble("efficiency", efficiency);
            return tag;
        }

        @Nonnull
        static TransmissionEntry readFromNBT(@Nonnull CompoundTag tag) {
            BlockPos pos = NBTHelper.readBlockPosFromNBT(tag.getCompound("pos"));
            double efficiency = tag.getDouble("efficiency");
            return new TransmissionEntry(pos, efficiency);
        }
    }

    /**
     * BFS traversal helper: a wave of starlight at a position.
     */
    private record StarlightWave(@Nonnull BlockPos pos, double starlight) {}
}
