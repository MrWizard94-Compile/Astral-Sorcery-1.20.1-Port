/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless BFS distribution engine for the starlight network.
 *
 * <p>Given a network topology (adjacency map, transmission efficiencies) and
 * resolved source productions, computes how much starlight each receiver and
 * each transmutable block position receives this tick. Has zero Minecraft
 * world dependencies — it only operates on plain Java maps and sets.</p>
 *
 * <p>Called once per tick from {@link WorldNetworkHandler#tick}. The caller
 * is responsible for resolving block-entity production values before calling
 * {@link #distribute} and for delivering results to block entities afterward.</p>
 */
public final class StarlightDistributor {

    /** Maximum BFS depth — prevents runaway traversal on exotic or malformed topologies. */
    static final int MAX_TRAVERSAL_DEPTH = 32;

    private StarlightDistributor() {}

    // ========================================================================
    // Result type
    // ========================================================================

    /**
     * The output of one tick's distribution pass.
     *
     * @param receiverAccumulation       receiver position → total starlight delivered this tick
     *                                   (before receiver cap is applied by the caller)
     * @param receiverConstellations     receiver position → first constellation to reach it
     *                                   (first-source-wins; used for recipe matching)
     * @param transmutationAccumulation  non-receiver link target → starlight for block transmutation
     */
    public record DistributionResult(
            @Nonnull Map<BlockPos, Double> receiverAccumulation,
            @Nonnull Map<BlockPos, ResourceLocation> receiverConstellations,
            @Nonnull Map<BlockPos, Double> transmutationAccumulation) {

        /** Sentinel returned when there are no producing sources. */
        public static final DistributionResult EMPTY = new DistributionResult(
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    // ========================================================================
    // Main entry point
    // ========================================================================

    /**
     * Distributes starlight from all producing sources through the network.
     *
     * @param adjacency               outgoing adjacency map from {@link StarlightGraph#getAdjacency()}
     * @param transmissionEfficiencies transmission position → [0,1] efficiency
     * @param receiverPositions       set of positions that are registered receivers
     * @param sourceProductions       source position → resolved production amount (only positive values)
     * @param sourceConstellations    source position → constellation (may be null for unattuned sources)
     * @param lossPerBlock            per-block distance attenuation coefficient; 0 means lossless
     * @return accumulated delivery amounts for receivers and transmutable blocks
     */
    @Nonnull
    public static DistributionResult distribute(
            @Nonnull Map<BlockPos, List<BlockPos>> adjacency,
            @Nonnull Map<BlockPos, Double> transmissionEfficiencies,
            @Nonnull Set<BlockPos> receiverPositions,
            @Nonnull Map<BlockPos, Double> sourceProductions,
            @Nonnull Map<BlockPos, ResourceLocation> sourceConstellations,
            double lossPerBlock) {

        if (sourceProductions.isEmpty()) {
            return DistributionResult.EMPTY;
        }

        Map<BlockPos, Double> receiverAccumulation = new HashMap<>();
        Map<BlockPos, ResourceLocation> receiverConstellations = new HashMap<>();
        Map<BlockPos, Double> transmutationAccumulation = new HashMap<>();

        for (Map.Entry<BlockPos, Double> entry : sourceProductions.entrySet()) {
            BlockPos sourcePos = entry.getKey();
            distributeFromSource(
                    sourcePos,
                    sourceConstellations.get(sourcePos),
                    entry.getValue(),
                    adjacency,
                    transmissionEfficiencies,
                    receiverPositions,
                    lossPerBlock,
                    receiverAccumulation,
                    receiverConstellations,
                    transmutationAccumulation);
        }

        return new DistributionResult(
                receiverAccumulation,
                receiverConstellations,
                transmutationAccumulation);
    }

    // ========================================================================
    // BFS from a single source
    // ========================================================================

    /**
     * BFS traversal from one source, accumulating starlight at reached receivers.
     *
     * <p>Algorithm: enqueue the source with its full production. At each step,
     * split the wave evenly among all outgoing targets. Apply per-block loss
     * over the link distance, then the transmission node's efficiency multiplier
     * if the target is a relay. Receivers and transmutable blocks accumulate the
     * arriving amount. A visited set prevents cycles and double-delivery.</p>
     */
    private static void distributeFromSource(
            @Nonnull BlockPos sourcePos,
            @Nullable ResourceLocation constellation,
            double production,
            @Nonnull Map<BlockPos, List<BlockPos>> adjacency,
            @Nonnull Map<BlockPos, Double> transmissionEfficiencies,
            @Nonnull Set<BlockPos> receiverPositions,
            double lossPerBlock,
            @Nonnull Map<BlockPos, Double> receiverAccumulation,
            @Nonnull Map<BlockPos, ResourceLocation> receiverConstellations,
            @Nonnull Map<BlockPos, Double> transmutationAccumulation) {

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

                List<BlockPos> targets = adjacency.getOrDefault(wave.pos(), Collections.emptyList());
                if (targets.isEmpty()) {
                    continue;
                }

                // Split starlight evenly among all outgoing targets
                double perTarget = wave.starlight() / targets.size();

                for (BlockPos target : targets) {
                    if (visited.contains(target)) {
                        continue; // already visited — prevents cycles and double-delivery
                    }

                    double arriving = perTarget;

                    // Per-block distance attenuation: (1 - loss)^distance
                    if (lossPerBlock > 0.0) {
                        double distance = Math.sqrt(wave.pos().distSqr(target));
                        arriving *= Math.pow(1.0 - lossPerBlock, distance);
                    }

                    // Transmission node: apply efficiency and continue BFS
                    Double efficiency = transmissionEfficiencies.get(target);
                    if (efficiency != null) {
                        arriving *= efficiency;
                        visited.add(target);
                        queue.add(new StarlightWave(target, arriving));
                        continue;
                    }

                    // Receiver: accumulate starlight (cap applied later by WorldNetworkHandler)
                    if (receiverPositions.contains(target)) {
                        receiverAccumulation.merge(target, arriving, Double::sum);
                        // First constellation to reach a receiver wins
                        receiverConstellations.putIfAbsent(target, constellation);
                        visited.add(target);
                        continue;
                    }

                    // Neither transmission nor receiver — may be a transmutable block
                    transmutationAccumulation.merge(target, arriving, Double::sum);
                    visited.add(target);
                }
            }
            depth++;
        }
    }

    // ========================================================================
    // Internal helper
    // ========================================================================

    /** BFS queue element: a position and the starlight intensity arriving there. */
    private record StarlightWave(@Nonnull BlockPos pos, double starlight) {}
}
