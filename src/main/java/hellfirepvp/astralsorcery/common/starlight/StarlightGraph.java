/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionLink;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure graph data structure for the starlight transmission network.
 * Tracks nodes (sources, receivers, transmissions) and directed links between them.
 *
 * <p>Has no knowledge of Minecraft worlds, block entities, or persistence —
 * those concerns belong to {@link WorldNetworkHandler}. The BFS distribution
 * math lives in {@link StarlightDistributor}.</p>
 *
 * <p>Obtain an instance via
 * {@link WorldNetworkHandler#getOrCreate(net.minecraft.server.level.ServerLevel)};
 * do not construct directly.</p>
 */
public class StarlightGraph {

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

    // ---- Independent source data (persist across chunk unloads) ----
    @Nonnull
    private final Map<BlockPos, CompoundTag> independentSourceData = new HashMap<>();

    // ---- Dirty source positions (for client sync, consumed by WorldNetworkHandler) ----
    @Nonnull
    private final Set<BlockPos> dirtySourcePositions = new HashSet<>();

    // ---- Cached adjacency list rebuilt lazily from the link set ----
    @Nonnull
    private final Map<BlockPos, List<BlockPos>> adjacency = new HashMap<>();
    private boolean adjacencyDirty = true;

    // ---- Snapshot caches rebuilt lazily when topology changes ----
    @Nullable
    private Map<BlockPos, Double> receiverMaxInputsCache = null;
    @Nullable
    private Map<BlockPos, Double> transmissionEfficienciesCache = null;

    // ========================================================================
    // Source management
    // ========================================================================

    /**
     * Registers a starlight source at the given position.
     *
     * @param pos           source block position
     * @param constellation attuned constellation, or null if unattuned
     * @param autoLink      whether this source supports auto-linking (works while chunk is unloaded)
     */
    public void registerSource(@Nonnull BlockPos pos,
                               @Nullable ResourceLocation constellation,
                               boolean autoLink) {
        pos = pos.immutable();
        if (sources.containsKey(pos)) {
            // Re-registering (e.g. constellation change) — remove stale outgoing links
            // so the network rebuilds them with the new source state.
            removeAllLinksFrom(pos);
        }
        sources.put(pos, new SourceEntry(pos, constellation, autoLink));
        invalidateTopology();
        dirtySourcePositions.add(pos);
        AstralSorcery.log.debug("Registered starlight source at {}", pos.toShortString());
    }

    /** Removes a source and all links originating from it. */
    public void removeSource(@Nonnull BlockPos pos) {
        pos = pos.immutable();
        if (sources.remove(pos) != null) {
            removeAllLinksFrom(pos);
            independentSourceData.remove(pos);
            invalidateTopology();
            dirtySourcePositions.add(pos);
            AstralSorcery.log.debug("Removed starlight source at {}", pos.toShortString());
        }
    }

    /** Updates a source's attuned constellation without re-registering. */
    public void updateSourceConstellation(@Nonnull BlockPos pos,
                                          @Nullable ResourceLocation constellation) {
        SourceEntry entry = sources.get(pos);
        if (entry != null) {
            entry.constellation = constellation;
            dirtySourcePositions.add(pos.immutable());
        }
    }

    /** Stores independent source NBT for cross-chunk persistence. */
    public void storeIndependentSourceData(@Nonnull BlockPos pos, @Nonnull CompoundTag data) {
        independentSourceData.put(pos.immutable(), data.copy());
    }

    /** Returns a copy of the independent source data, or null if not present. */
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
        invalidateTopology();
        AstralSorcery.log.debug("Registered starlight receiver at {}", pos.toShortString());
    }

    /** Removes a receiver and all links targeting it. */
    public void removeReceiver(@Nonnull BlockPos pos) {
        pos = pos.immutable();
        if (receivers.remove(pos) != null) {
            removeAllLinksTo(pos);
            invalidateTopology();
            AstralSorcery.log.debug("Removed starlight receiver at {}", pos.toShortString());
        }
    }

    public boolean hasReceiver(@Nonnull BlockPos pos) {
        return receivers.containsKey(pos);
    }

    @Nonnull
    public Set<BlockPos> getReceiverPositions() {
        return Collections.unmodifiableSet(receivers.keySet());
    }

    /**
     * Returns a map of receiver position → maxInput for use by {@link StarlightDistributor}.
     */
    @Nonnull
    public Map<BlockPos, Double> getReceiverMaxInputs() {
        Map<BlockPos, Double> cache = receiverMaxInputsCache;
        if (cache == null) {
            Map<BlockPos, Double> built = new HashMap<>(receivers.size());
            for (ReceiverEntry entry : receivers.values()) {
                built.put(entry.pos, entry.maxInput);
            }
            cache = Collections.unmodifiableMap(built);
            receiverMaxInputsCache = cache;
        }
        return cache;
    }

    // ========================================================================
    // Transmission node management
    // ========================================================================

    /**
     * Registers a transmission (relay) node at the given position.
     *
     * @param pos        node block position
     * @param efficiency transmission efficiency in [0, 1]
     */
    public void registerTransmission(@Nonnull BlockPos pos, double efficiency) {
        pos = pos.immutable();
        transmissions.put(pos, new TransmissionEntry(pos, efficiency));
        invalidateTopology();
        AstralSorcery.log.debug("Registered transmission node at {}", pos.toShortString());
    }

    /** Removes a transmission node and all links through it. */
    public void removeTransmission(@Nonnull BlockPos pos) {
        pos = pos.immutable();
        if (transmissions.remove(pos) != null) {
            removeAllLinksFrom(pos);
            removeAllLinksTo(pos);
            invalidateTopology();
            AstralSorcery.log.debug("Removed transmission node at {}", pos.toShortString());
        }
    }

    public boolean hasTransmission(@Nonnull BlockPos pos) {
        return transmissions.containsKey(pos);
    }

    /**
     * Returns a map of transmission position → efficiency for use by {@link StarlightDistributor}.
     */
    @Nonnull
    public Map<BlockPos, Double> getTransmissionEfficiencies() {
        if (transmissionEfficienciesCache == null) {
            Map<BlockPos, Double> built = new HashMap<>(transmissions.size());
            for (TransmissionEntry entry : transmissions.values()) {
                built.put(entry.pos, entry.efficiency);
            }
            transmissionEfficienciesCache = Collections.unmodifiableMap(built);
        }
        return transmissionEfficienciesCache;
    }

    // ========================================================================
    // Node convenience
    // ========================================================================

    /** Returns true if the position is registered as any node type. */
    public boolean hasNode(@Nonnull BlockPos pos) {
        return sources.containsKey(pos)
                || receivers.containsKey(pos)
                || transmissions.containsKey(pos);
    }

    /**
     * Removes any node (source, receiver, or transmission) at the given position.
     * Convenience method for block-break handling.
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
     * Both positions must be registered nodes and within the configured max range.
     *
     * @return true if the link was added, false if invalid or already present
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
        double maxDist = CommonConfig.CONFIG.maxNetworkRange.get();
        if (from.distSqr(to) > maxDist * maxDist) {
            AstralSorcery.log.debug("Rejected link {} -> {}: exceeds max distance {}",
                    from.toShortString(), to.toShortString(), maxDist);
            return false;
        }
        int maxConn = CommonConfig.CONFIG.maxNodeConnections.get();
        final BlockPos fromFinal = from;
        long outgoing = activeLinks.stream().filter(l -> l.getFrom().equals(fromFinal)).count();
        if (outgoing >= maxConn) {
            AstralSorcery.log.debug("Rejected link {} -> {}: source already has {} connections (max {})",
                    from.toShortString(), to.toShortString(), outgoing, maxConn);
            return false;
        }
        // Receivers cannot be link sources
        if (receivers.containsKey(from) && !sources.containsKey(from) && !transmissions.containsKey(from)) {
            AstralSorcery.log.debug("Rejected link {} -> {}: source is receiver-only and cannot be a source",
                    from.toShortString(), to.toShortString());
            return false;
        }
        // Pure sources cannot be link targets
        if (sources.containsKey(to) && !transmissions.containsKey(to) && !receivers.containsKey(to)) {
            AstralSorcery.log.debug("Rejected link {} -> {}: target is pure source and cannot be a target",
                    from.toShortString(), to.toShortString());
            return false;
        }

        TransmissionLink link = new TransmissionLink(from, to);
        if (activeLinks.add(link)) {
            invalidateTopology();
            dirtySourcePositions.addAll(findUpstreamSources(from));
            AstralSorcery.log.debug("Added starlight link: {}", link);
            return true;
        }
        AstralSorcery.log.debug("Rejected link {} -> {}: link already present or invalid", from.toShortString(), to.toShortString());
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
            invalidateTopology();
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

    /** Gets all outgoing link targets from a position. */
    @Nonnull
    public List<BlockPos> getOutgoingTargets(@Nonnull BlockPos from) {
        rebuildAdjacencyIfNeeded();
        return adjacency.getOrDefault(from, Collections.emptyList());
    }

    /** Gets all incoming link sources to a position. */
    @Nonnull
    public List<BlockPos> getIncomingSources(@Nonnull BlockPos to) {
        return activeLinks.stream()
                .filter(link -> link.getTo().equals(to))
                .map(TransmissionLink::getFrom)
                .collect(Collectors.toList());
    }

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
    // Adjacency (lazily rebuilt from the link set)
    // ========================================================================

    /** Rebuilds the adjacency map from the link set, if it has changed since the last rebuild. */
    public void rebuildAdjacencyIfNeeded() {
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

    /** Marks topology as changed and invalidates all derived snapshot caches. */
    private void invalidateTopology() {
        adjacencyDirty = true;
        receiverMaxInputsCache = null;
        transmissionEfficienciesCache = null;
    }

    /**
     * Returns an unmodifiable view of the adjacency map (rebuilds from links if stale).
     * Pass this to {@link StarlightDistributor#distribute} and BFS operations.
     */
    @Nonnull
    public Map<BlockPos, List<BlockPos>> getAdjacency() {
        rebuildAdjacencyIfNeeded();
        return Collections.unmodifiableMap(adjacency);
    }

    /**
     * Finds all source positions that can route starlight through the given position.
     * Used to mark the appropriate sources dirty when downstream topology changes.
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
            for (TransmissionLink link : activeLinks) {
                if (link.getTo().equals(current) && visited.add(link.getFrom())) {
                    queue.add(link.getFrom());
                }
            }
        }
        return upstream;
    }

    // ========================================================================
    // Auto-linking
    // ========================================================================

    /**
     * Scans for receivers within range of the given auto-linking source and
     * creates links from source to any reachable receiver below it.
     * Call after registering a new source (e.g., collector crystal placed).
     */
    public void attemptAutoLinkFrom(@Nonnull BlockPos sourcePos) {
        double maxDistSq = getMaxLinkDistanceSq();
        for (BlockPos receiverPos : receivers.keySet()) {
            if (sourcePos.getY() <= receiverPos.getY()) continue;
            if (sourcePos.distSqr(receiverPos) > maxDistSq) continue;
            addLink(sourcePos, receiverPos);
        }
    }

    /**
     * Scans for auto-linking sources within range of the given receiver and
     * creates links from any auto-linking source that is above it.
     * Call after registering a new receiver (e.g., altar placed).
     */
    public void attemptAutoLinkTo(@Nonnull BlockPos receiverPos) {
        double maxDistSq = getMaxLinkDistanceSq();
        for (Map.Entry<BlockPos, SourceEntry> entry : sources.entrySet()) {
            BlockPos sourcePos = entry.getKey();
            SourceEntry source = entry.getValue();
            if (!source.autoLink) continue;
            if (sourcePos.getY() <= receiverPos.getY()) continue;
            if (sourcePos.distSqr(receiverPos) > maxDistSq) continue;
            addLink(sourcePos, receiverPos);
        }
    }

    /** Removes all auto-link connections from auto-linking sources to the given receiver. */
    public void removeAutoLinkTo(@Nonnull BlockPos receiverPos) {
        for (BlockPos sourcePos : new ArrayList<>(sources.keySet())) {
            SourceEntry source = sources.get(sourcePos);
            if (source != null && source.autoLink) {
                removeLink(sourcePos, receiverPos);
            }
        }
    }

    private double getMaxLinkDistanceSq() {
        double maxDist = CommonConfig.CONFIG.maxNetworkRange.get();
        return maxDist * maxDist;
    }

    // ========================================================================
    // Dirty source positions (consumed by WorldNetworkHandler for client sync)
    // ========================================================================

    /**
     * Returns and clears the set of source positions that changed since the last call.
     * {@link WorldNetworkHandler} calls this each tick to send beam-sync packets.
     */
    @Nonnull
    public Set<BlockPos> takeDirtySourcePositions() {
        Set<BlockPos> dirty = new HashSet<>(dirtySourcePositions);
        dirtySourcePositions.clear();
        return dirty;
    }

    // ========================================================================
    // Network info queries
    // ========================================================================

    /** Total number of unique node positions (sources + receivers + transmissions). */
    public int getNodeCount() {
        Set<BlockPos> all = new HashSet<>();
        all.addAll(sources.keySet());
        all.addAll(receivers.keySet());
        all.addAll(transmissions.keySet());
        return all.size();
    }

    /** Total number of active directed links. */
    public int getLinkCount() {
        return activeLinks.size();
    }

    // ========================================================================
    // NBT serialization
    // ========================================================================

    /**
     * Serializes this graph into the given tag.
     * Called from {@link WorldNetworkHandler#save(CompoundTag)}.
     */
    @Nonnull
    public CompoundTag saveToNBT(@Nonnull CompoundTag tag) {
        ListTag sourceList = new ListTag();
        for (SourceEntry source : sources.values()) {
            sourceList.add(source.writeToNBT());
        }
        tag.put("sources", sourceList);

        ListTag receiverList = new ListTag();
        for (ReceiverEntry receiver : receivers.values()) {
            receiverList.add(receiver.writeToNBT());
        }
        tag.put("receivers", receiverList);

        ListTag transmissionList = new ListTag();
        for (TransmissionEntry transmission : transmissions.values()) {
            transmissionList.add(transmission.writeToNBT());
        }
        tag.put("transmissions", transmissionList);

        ListTag linkList = new ListTag();
        for (TransmissionLink link : activeLinks) {
            linkList.add(link.writeToNBT());
        }
        tag.put("links", linkList);

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
     * Deserializes a graph from NBT.
     * Called from {@link WorldNetworkHandler}'s load factory method.
     */
    @Nonnull
    public static StarlightGraph loadFromNBT(@Nonnull CompoundTag tag,
                                             @Nonnull ResourceKey<Level> dimension) {
        StarlightGraph graph = new StarlightGraph();

        ListTag sourceList = tag.getList("sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < sourceList.size(); i++) {
            SourceEntry source = SourceEntry.readFromNBT(sourceList.getCompound(i));
            graph.sources.put(source.pos, source);
        }

        ListTag receiverList = tag.getList("receivers", Tag.TAG_COMPOUND);
        for (int i = 0; i < receiverList.size(); i++) {
            ReceiverEntry receiver = ReceiverEntry.readFromNBT(receiverList.getCompound(i));
            graph.receivers.put(receiver.pos, receiver);
        }

        ListTag transmissionList = tag.getList("transmissions", Tag.TAG_COMPOUND);
        for (int i = 0; i < transmissionList.size(); i++) {
            TransmissionEntry transmission = TransmissionEntry.readFromNBT(transmissionList.getCompound(i));
            graph.transmissions.put(transmission.pos, transmission);
        }

        ListTag linkList = tag.getList("links", Tag.TAG_COMPOUND);
        for (int i = 0; i < linkList.size(); i++) {
            graph.activeLinks.add(TransmissionLink.readFromNBT(linkList.getCompound(i)));
        }

        ListTag independentList = tag.getList("independentSources", Tag.TAG_COMPOUND);
        for (int i = 0; i < independentList.size(); i++) {
            CompoundTag indTag = independentList.getCompound(i);
            BlockPos pos = NBTHelper.readBlockPosFromNBT(indTag.getCompound("pos"));
            CompoundTag data = indTag.getCompound("data").copy();
            graph.independentSourceData.put(pos, data);
        }

        graph.invalidateTopology();
        AstralSorcery.log.info(
                "Loaded starlight graph for {}: {} sources, {} receivers, {} transmissions, {} links",
                dimension.location(),
                graph.sources.size(),
                graph.receivers.size(),
                graph.transmissions.size(),
                graph.activeLinks.size());

        return graph;
    }

    // ========================================================================
    // Inner data classes
    // ========================================================================

    /** Tracks a registered starlight source node. */
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
                    ? ResourceLocation.tryParse(tag.getString("constellation")) : null;
            boolean autoLink = tag.getBoolean("autoLink");
            return new SourceEntry(pos, constellation, autoLink);
        }
    }

    /** Tracks a registered starlight receiver node. */
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

    /** Tracks a registered transmission (relay) node. */
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
}
