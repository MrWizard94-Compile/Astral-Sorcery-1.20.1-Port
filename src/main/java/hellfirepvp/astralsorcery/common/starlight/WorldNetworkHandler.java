/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.TransmutationHelper;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncStarlightNetwork;
import hellfirepvp.astralsorcery.common.starlight.transmission.NodeConnection;
import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionLink;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-dimension orchestrator for the starlight transmission network.
 * Persists via {@link SavedData} (one instance per dimension) and drives
 * the tick loop: resolve source production → distribute → deliver to receivers
 * → sync beam data to clients.
 *
 * <p>Graph topology (nodes and links) lives in {@link StarlightGraph}.
 * BFS distribution math lives in {@link StarlightDistributor}.
 * This class wires them together with Minecraft world interactions and persistence.</p>
 *
 * <p>Block entities interact through {@link StarlightNetworkHelper}, which routes
 * calls here. External callers should prefer that facade.</p>
 *
 * <p>1.16 → 1.20: WorldSavedData → SavedData,
 * computeIfAbsent on ServerLevel.getDataStorage(),
 * RegistryKey&lt;World&gt; → ResourceKey&lt;Level&gt;.</p>
 */
public class WorldNetworkHandler extends SavedData {

    private static final String DATA_NAME = AstralSorcery.MODID + "_starlight_network";

    @Nonnull
    private final ResourceKey<Level> dimension;
    @Nonnull
    private final StarlightGraph graph;

    private WorldNetworkHandler(@Nonnull ResourceKey<Level> dimension) {
        this.dimension = dimension;
        this.graph = new StarlightGraph();
    }

    private WorldNetworkHandler(@Nonnull ResourceKey<Level> dimension,
                                @Nonnull StarlightGraph graph) {
        this.dimension = dimension;
        this.graph = graph;
    }

    // ========================================================================
    // Factory methods
    // ========================================================================

    /**
     * Gets or creates the starlight network handler for the given server level.
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
     * Gets the handler if it already exists, without creating a new one.
     * Used by tick handlers that must not force-create a storage entry.
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
    // Source API — delegates to graph + marks dirty
    // ========================================================================

    public void registerSource(@Nonnull BlockPos pos,
                               @Nullable ResourceLocation constellation,
                               boolean autoLink) {
        graph.registerSource(pos, constellation, autoLink);
        setDirty();
    }

    public void removeSource(@Nonnull BlockPos pos) {
        graph.removeSource(pos);
        setDirty();
    }

    public void updateSourceConstellation(@Nonnull BlockPos pos,
                                          @Nullable ResourceLocation constellation) {
        graph.updateSourceConstellation(pos, constellation);
        setDirty();
    }

    public void storeIndependentSourceData(@Nonnull BlockPos pos, @Nonnull CompoundTag data) {
        graph.storeIndependentSourceData(pos, data);
        setDirty();
    }

    @Nullable
    public CompoundTag getIndependentSourceData(@Nonnull BlockPos pos) {
        return graph.getIndependentSourceData(pos);
    }

    @Nonnull
    public Set<BlockPos> getSourcePositions() {
        return graph.getSourcePositions();
    }

    public boolean hasSource(@Nonnull BlockPos pos) {
        return graph.hasSource(pos);
    }

    @Nullable
    public StarlightGraph.SourceEntry getSource(@Nonnull BlockPos pos) {
        return graph.getSource(pos);
    }

    // ========================================================================
    // Receiver API — delegates to graph + marks dirty
    // ========================================================================

    public void registerReceiver(@Nonnull BlockPos pos, double maxInput) {
        graph.registerReceiver(pos, maxInput);
        setDirty();
    }

    public void removeReceiver(@Nonnull BlockPos pos) {
        graph.removeReceiver(pos);
        setDirty();
    }

    public boolean hasReceiver(@Nonnull BlockPos pos) {
        return graph.hasReceiver(pos);
    }

    @Nonnull
    public Set<BlockPos> getReceiverPositions() {
        return graph.getReceiverPositions();
    }

    // ========================================================================
    // Transmission API — delegates to graph + marks dirty
    // ========================================================================

    public void registerTransmission(@Nonnull BlockPos pos, double efficiency) {
        graph.registerTransmission(pos, efficiency);
        setDirty();
    }

    public void removeTransmission(@Nonnull BlockPos pos) {
        graph.removeTransmission(pos);
        setDirty();
    }

    public boolean hasTransmission(@Nonnull BlockPos pos) {
        return graph.hasTransmission(pos);
    }

    // ========================================================================
    // Node API — delegates to graph + marks dirty
    // ========================================================================

    public boolean hasNode(@Nonnull BlockPos pos) {
        return graph.hasNode(pos);
    }

    public void removeNode(@Nonnull BlockPos pos) {
        graph.removeNode(pos);
        setDirty();
    }

    // ========================================================================
    // Link API — delegates to graph + marks dirty on change
    // ========================================================================

    public boolean addLink(@Nonnull BlockPos from, @Nonnull BlockPos to) {
        boolean added = graph.addLink(from, to);
        if (added) {
            setDirty();
        }
        return added;
    }

    public boolean removeLink(@Nonnull BlockPos from, @Nonnull BlockPos to) {
        boolean removed = graph.removeLink(from, to);
        if (removed) {
            setDirty();
        }
        return removed;
    }

    @Nonnull
    public Set<TransmissionLink> getActiveLinks() {
        return graph.getActiveLinks();
    }

    @Nonnull
    public List<BlockPos> getOutgoingTargets(@Nonnull BlockPos from) {
        return graph.getOutgoingTargets(from);
    }

    @Nonnull
    public List<BlockPos> getIncomingSources(@Nonnull BlockPos to) {
        return graph.getIncomingSources(to);
    }

    // ========================================================================
    // Auto-link API — delegates to graph + marks dirty
    // ========================================================================

    public void attemptAutoLinkFrom(@Nonnull BlockPos sourcePos) {
        graph.attemptAutoLinkFrom(sourcePos);
        setDirty();
    }

    public void attemptAutoLinkTo(@Nonnull BlockPos receiverPos) {
        graph.attemptAutoLinkTo(receiverPos);
        setDirty();
    }

    public void removeAutoLinkTo(@Nonnull BlockPos receiverPos) {
        graph.removeAutoLinkTo(receiverPos);
        setDirty();
    }

    // ========================================================================
    // Network info queries
    // ========================================================================

    /**
     * Gets a {@link NodeConnection} view of the node at the given position,
     * or null if the position is not a registered node.
     */
    @Nullable
    public NodeConnection getNodeInfo(@Nonnull BlockPos pos) {
        if (!hasNode(pos)) {
            return null;
        }
        NodeConnection node = new NodeConnection(pos, dimension);
        node.setSource(graph.hasSource(pos));
        node.setReceiver(graph.hasReceiver(pos));
        node.setTransmission(graph.hasTransmission(pos));
        for (BlockPos target : graph.getOutgoingTargets(pos)) {
            node.addConnection(target);
        }
        return node;
    }

    public int getNodeCount() {
        return graph.getNodeCount();
    }

    public int getLinkCount() {
        return graph.getLinkCount();
    }

    // ========================================================================
    // Server tick — orchestration
    // ========================================================================

    /**
     * Distributes starlight for one tick. Called once per dimension per server tick
     * by {@link StarlightNetworkRegistry}.
     *
     * <p>Steps:
     * <ol>
     *   <li>Resolve production from block entities for each registered source.</li>
     *   <li>Run BFS distribution via {@link StarlightDistributor} (pure math).</li>
     *   <li>Deliver accumulated amounts to receiver block entities.</li>
     *   <li>Feed transmutation accumulation into {@link TransmutationHelper}.</li>
     *   <li>Sync topology changes to tracking clients.</li>
     * </ol>
     */
    public void tick(@Nonnull ServerLevel level) {
        graph.rebuildAdjacencyIfNeeded();

        // Step 1: resolve production from block entities
        Map<BlockPos, Double> sourceProductions = new HashMap<>();
        Map<BlockPos, ResourceLocation> sourceConstellations = new HashMap<>();
        for (BlockPos sourcePos : graph.getSourcePositions()) {
            StarlightGraph.SourceEntry source = graph.getSource(sourcePos);
            if (source == null) continue;
            double production = resolveSourceProduction(level, sourcePos, source);
            if (production > 0) {
                sourceProductions.put(sourcePos, production);
                sourceConstellations.put(sourcePos, source.getConstellation());
            }
        }

        // Step 2: BFS distribution (no Minecraft world access inside the distributor)
        double lossPerBlock = CommonConfig.CONFIG.transmissionLossPerBlock.get();
        StarlightDistributor.DistributionResult result = StarlightDistributor.distribute(
                graph.getAdjacency(),
                graph.getTransmissionEfficiencies(),
                graph.getReceiverPositions(),
                sourceProductions,
                sourceConstellations,
                lossPerBlock);

        // Step 3: deliver to receivers (cap at maxInput)
        Map<BlockPos, Double> receiverMaxInputs = graph.getReceiverMaxInputs();
        for (Map.Entry<BlockPos, Double> entry : result.receiverAccumulation().entrySet()) {
            BlockPos receiverPos = entry.getKey();
            double maxInput = receiverMaxInputs.getOrDefault(receiverPos, Double.MAX_VALUE);
            double delivered = Math.min(entry.getValue(), maxInput);
            ResourceLocation constellation = result.receiverConstellations().get(receiverPos);
            deliverStarlight(level, receiverPos, delivered, constellation);
        }

        // Step 4: feed block transmutation
        for (Map.Entry<BlockPos, Double> entry : result.transmutationAccumulation().entrySet()) {
            BlockPos targetPos = entry.getKey();
            boolean completed = TransmutationHelper.addStarlight(level, targetPos, entry.getValue());
            if (completed) {
                PacketChannel.sendToAllTracking(
                        new PktPlayEffect(PktPlayEffect.EffectType.TRANSMUTATION_COMPLETE, targetPos),
                        level, targetPos);
            }
        }

        // Step 5: sync topology changes to tracking clients
        syncDirtySources(level);
    }

    // ========================================================================
    // Block entity interaction
    // ========================================================================

    /**
     * Resolves actual starlight production for a source by querying its block entity.
     * Falls back to cached independent data if the chunk is unloaded.
     */
    private double resolveSourceProduction(@Nonnull ServerLevel level,
                                           @Nonnull BlockPos sourcePos,
                                           @Nonnull StarlightGraph.SourceEntry source) {
        if (level.isLoaded(sourcePos)) {
            BlockEntity be = level.getBlockEntity(sourcePos);
            if (be instanceof IStarlightSource starlightSource) {
                double prod = starlightSource.getStarlightProduction();
                // Update cached data for when the chunk later unloads
                if (be instanceof IIndependentStarlightSource independentSource) {
                    graph.storeIndependentSourceData(sourcePos, independentSource.serializeSourceNBT());
                }
                return starlightSource.hasSkyAccess() ? prod : 0;
            }
            return 0; // Stale registration — block entity no longer implements IStarlightSource
        }

        // Chunk unloaded — use the last-known cached production if the source supports auto-link
        if (source.isAutoLink()) {
            CompoundTag cached = graph.getIndependentSourceData(sourcePos);
            if (cached != null && cached.contains("cachedProduction")) {
                return cached.getDouble("cachedProduction");
            }
        }
        return 0;
    }

    /** Delivers the given amount of starlight to the receiver at the given position. */
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
    // Client synchronization
    // ========================================================================

    /**
     * Sends beam-sync packets to tracking clients for all sources that changed
     * since the last tick.
     */
    private void syncDirtySources(@Nonnull ServerLevel level) {
        Set<BlockPos> dirty = graph.takeDirtySourcePositions();
        for (BlockPos sourcePos : dirty) {
            List<BlockPos> reachable = computeReachableTargets(sourcePos);
            StarlightGraph.SourceEntry source = graph.getSource(sourcePos);
            double starlight = source != null ? resolveSourceProduction(level, sourcePos, source) : 0;

            PktSyncStarlightNetwork pkt = new PktSyncStarlightNetwork(sourcePos, reachable, starlight);
            PacketChannel.sendToAllTracking(pkt, level, sourcePos);
        }
    }

    /**
     * Computes all positions reachable from a source via BFS, for client beam rendering.
     */
    @Nonnull
    private List<BlockPos> computeReachableTargets(@Nonnull BlockPos sourcePos) {
        Map<BlockPos, List<BlockPos>> adjacency = graph.getAdjacency();
        List<BlockPos> reachable = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(sourcePos);
        visited.add(sourcePos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (BlockPos target : adjacency.getOrDefault(current, Collections.emptyList())) {
                if (visited.add(target)) {
                    reachable.add(target);
                    // Continue through transmission nodes so beams draw along the full path
                    if (graph.hasTransmission(target)) {
                        queue.add(target);
                    }
                }
            }
        }
        return reachable;
    }

    /**
     * Syncs the full network state to a single player.
     * Call on login and dimension change so the client can render beams immediately.
     */
    public void syncAllToPlayer(@Nonnull ServerPlayer player, @Nonnull ServerLevel level) {
        for (BlockPos sourcePos : graph.getSourcePositions()) {
            StarlightGraph.SourceEntry source = graph.getSource(sourcePos);
            List<BlockPos> reachable = computeReachableTargets(sourcePos);
            double starlight = source != null ? resolveSourceProduction(level, sourcePos, source) : 0;

            PktSyncStarlightNetwork pkt = new PktSyncStarlightNetwork(sourcePos, reachable, starlight);
            PacketChannel.sendToPlayer(pkt, player);
        }
    }

    // ========================================================================
    // SavedData persistence
    // ========================================================================

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag) {
        return graph.saveToNBT(tag);
    }

    @Nonnull
    private static WorldNetworkHandler load(@Nonnull CompoundTag tag,
                                            @Nonnull ResourceKey<Level> dimension) {
        StarlightGraph graph = StarlightGraph.loadFromNBT(tag, dimension);
        return new WorldNetworkHandler(dimension, graph);
    }
}
