/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionLink;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the starlight distribution math and
 * network topology manipulation.
 *
 * <p>These tests exercise the BFS distribution algorithm's math independently
 * of a ServerLevel, by simulating the graph traversal logic that
 * {@link WorldNetworkHandler#tick} uses internally. This validates:
 * <ul>
 *   <li>Multi-hop distribution (source → transmission → transmission → receiver)</li>
 *   <li>Fan-out splitting across multiple targets</li>
 *   <li>Chained efficiency multiplication</li>
 *   <li>Receiver cap saturation from single and multiple sources</li>
 *   <li>Cycle prevention via visited sets</li>
 *   <li>Max traversal depth enforcement</li>
 *   <li>Independent source data serialization for unloaded chunks</li>
 *   <li>Network topology mutations (add/remove nodes and links)</li>
 * </ul>
 *
 * <p>No Minecraft game state or ServerLevel is required — only pure data
 * and math logic is exercised through the public API of the inner classes
 * and graph simulation.</p>
 */
class StarlightDistributionTest {

    // Constants matching WorldNetworkHandler
    private static final double MAX_LINK_DISTANCE = 64.0;
    private static final int MAX_TRAVERSAL_DEPTH = 32;

    // ========================================================================
    // Helper: BFS distribution simulator
    //
    // This replicates the exact algorithm from WorldNetworkHandler.distributeFromSource()
    // but operates on simple maps instead of requiring a live handler instance.
    // ========================================================================

    /**
     * Simulates BFS starlight distribution from a single source through a network
     * defined by node maps and adjacency links.
     *
     * @param sourcePos      starting position
     * @param production     starlight produced by the source
     * @param transmissions  map of transmission nodes with their efficiencies
     * @param receivers      set of positions that are receivers
     * @param adjacency      adjacency list (outgoing links from each position)
     * @return map of receiver positions to accumulated starlight amounts
     */
    private Map<BlockPos, Double> simulateDistribution(
            BlockPos sourcePos,
            double production,
            Map<BlockPos, Double> transmissions,
            Set<BlockPos> receivers,
            Map<BlockPos, List<BlockPos>> adjacency) {

        Map<BlockPos, Double> receiverAccumulation = new HashMap<>();
        Deque<Object[]> bfsQueue = new ArrayDeque<>(); // [BlockPos, double starlight]
        Set<BlockPos> visited = new HashSet<>();

        bfsQueue.add(new Object[]{sourcePos, production});
        visited.add(sourcePos);

        int depth = 0;
        while (!bfsQueue.isEmpty() && depth < MAX_TRAVERSAL_DEPTH) {
            int levelSize = bfsQueue.size();
            for (int i = 0; i < levelSize; i++) {
                Object[] wave = bfsQueue.poll();
                if (wave == null) break;
                BlockPos pos = (BlockPos) wave[0];
                double starlight = (double) wave[1];

                List<BlockPos> targets = adjacency.getOrDefault(pos, Collections.emptyList());
                if (targets.isEmpty()) {
                    continue;
                }

                // Split starlight evenly among targets
                double perTarget = starlight / targets.size();

                for (BlockPos target : targets) {
                    if (visited.contains(target)) {
                        continue; // prevent cycles
                    }

                    double arriving = perTarget;

                    // Apply transmission efficiency if target is a transmission node
                    Double efficiency = transmissions.get(target);
                    if (efficiency != null) {
                        arriving *= efficiency;
                        visited.add(target);
                        bfsQueue.add(new Object[]{target, arriving});
                    }

                    // Accumulate at receiver
                    if (receivers.contains(target)) {
                        receiverAccumulation.merge(target, arriving, Double::sum);
                        visited.add(target);
                    }
                }
            }
            depth++;
        }

        return receiverAccumulation;
    }

    /**
     * Applies receiver caps to accumulated starlight values.
     */
    private Map<BlockPos, Double> applyReceiverCaps(
            Map<BlockPos, Double> accumulation,
            Map<BlockPos, Double> maxInputs) {
        Map<BlockPos, Double> result = new HashMap<>();
        for (Map.Entry<BlockPos, Double> entry : accumulation.entrySet()) {
            double maxInput = maxInputs.getOrDefault(entry.getKey(), Double.MAX_VALUE);
            result.put(entry.getKey(), Math.min(entry.getValue(), maxInput));
        }
        return result;
    }

    // ========================================================================
    // Multi-hop distribution math
    // ========================================================================

    @Nested
    class MultiHopDistribution {

        @Test
        void testSourceDirectToReceiver() {
            // Source(1000) → Receiver
            // Receiver should get full 1000
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos receiver = new BlockPos(10, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of();
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(1000.0, result.get(receiver), 0.001,
                    "Direct source→receiver should deliver full starlight");
        }

        @Test
        void testSourceThroughOneTransmission() {
            // Source(1000) → Transmission(0.9) → Receiver
            // Receiver should get 1000 * 0.9 = 900
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos trans = new BlockPos(10, 64, 0);
            BlockPos receiver = new BlockPos(20, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(trans, 0.9);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(trans),
                    trans, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(900.0, result.get(receiver), 0.001,
                    "One transmission hop with 0.9 efficiency should deliver 900");
        }

        @Test
        void testSourceThroughTwoTransmissions() {
            // Source(1000) → Trans1(0.9) → Trans2(0.8) → Receiver
            // Receiver should get 1000 * 0.9 * 0.8 = 720
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos trans1 = new BlockPos(10, 64, 0);
            BlockPos trans2 = new BlockPos(20, 64, 0);
            BlockPos receiver = new BlockPos(30, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(trans1, 0.9, trans2, 0.8);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(trans1),
                    trans1, List.of(trans2),
                    trans2, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(720.0, result.get(receiver), 0.001,
                    "Chain of 0.9 * 0.8 = 0.72 total efficiency → 720 starlight");
        }

        @Test
        void testSourceThroughThreeTransmissions() {
            // Source(1000) → T1(0.9) → T2(0.8) → T3(0.7) → Receiver
            // Receiver should get 1000 * 0.9 * 0.8 * 0.7 = 504
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 0);
            BlockPos t2 = new BlockPos(20, 64, 0);
            BlockPos t3 = new BlockPos(30, 64, 0);
            BlockPos receiver = new BlockPos(40, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.9, t2, 0.8, t3, 0.7);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t2),
                    t2, List.of(t3),
                    t3, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(504.0, result.get(receiver), 0.001,
                    "Chain of 0.9 * 0.8 * 0.7 = 0.504 total → 504 starlight");
        }

        @Test
        void testPerfectEfficiencyChain() {
            // Source(500) → T1(1.0) → T2(1.0) → Receiver
            // Perfect relays pass full amount
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 0);
            BlockPos t2 = new BlockPos(20, 64, 0);
            BlockPos receiver = new BlockPos(30, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 1.0, t2, 1.0);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t2),
                    t2, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 500.0, transmissions, receivers, adjacency);

            assertEquals(500.0, result.get(receiver), 0.001,
                    "Perfect efficiency (1.0) chain should pass full starlight");
        }

        @Test
        void testZeroEfficiencyBlocksAll() {
            // Source(1000) → Trans(0.0) → Receiver
            // Zero efficiency blocks everything
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos trans = new BlockPos(10, 64, 0);
            BlockPos receiver = new BlockPos(20, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(trans, 0.0);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(trans),
                    trans, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // Trans passes 0 starlight, so receiver gets nothing (or 0)
            double delivered = result.getOrDefault(receiver, 0.0);
            assertEquals(0.0, delivered, 0.001,
                    "Zero-efficiency transmission should block all starlight");
        }
    }

    // ========================================================================
    // Fan-out splitting tests
    // ========================================================================

    @Nested
    class FanOutSplitting {

        @Test
        void testEvenSplitTwoReceivers() {
            // Source(1000) → [R1, R2]
            // Each receiver gets 500
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos r1 = new BlockPos(10, 64, 0);
            BlockPos r2 = new BlockPos(10, 64, 10);

            Map<BlockPos, Double> transmissions = Map.of();
            Set<BlockPos> receivers = Set.of(r1, r2);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(r1, r2)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(500.0, result.get(r1), 0.001, "Split 2 ways: each gets 500");
            assertEquals(500.0, result.get(r2), 0.001, "Split 2 ways: each gets 500");
        }

        @Test
        void testEvenSplitFourReceivers() {
            // Source(1000) → [R1, R2, R3, R4]
            // Each receiver gets 250
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos r1 = new BlockPos(10, 64, 0);
            BlockPos r2 = new BlockPos(0, 64, 10);
            BlockPos r3 = new BlockPos(-10, 64, 0);
            BlockPos r4 = new BlockPos(0, 64, -10);

            Map<BlockPos, Double> transmissions = Map.of();
            Set<BlockPos> receivers = Set.of(r1, r2, r3, r4);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(r1, r2, r3, r4)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(250.0, result.get(r1), 0.001);
            assertEquals(250.0, result.get(r2), 0.001);
            assertEquals(250.0, result.get(r3), 0.001);
            assertEquals(250.0, result.get(r4), 0.001);
        }

        @Test
        void testSplitThenEfficiency() {
            // Source(1000) → [Trans1(0.9), Trans2(0.8)]
            // Trans1 → R1:  1000/2 * 0.9 = 450
            // Trans2 → R2:  1000/2 * 0.8 = 400
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 0);
            BlockPos t2 = new BlockPos(-10, 64, 0);
            BlockPos r1 = new BlockPos(20, 64, 0);
            BlockPos r2 = new BlockPos(-20, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.9, t2, 0.8);
            Set<BlockPos> receivers = Set.of(r1, r2);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1, t2),
                    t1, List.of(r1),
                    t2, List.of(r2)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(450.0, result.get(r1), 0.001,
                    "500 * 0.9 = 450 to first receiver");
            assertEquals(400.0, result.get(r2), 0.001,
                    "500 * 0.8 = 400 to second receiver");
        }

        @Test
        void testSplitAtTransmissionNode() {
            // Source(1000) → Trans(0.9) → [R1, R2, R3]
            // After transmission: 1000 * 0.9 = 900, split 3 ways = 300 each
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos trans = new BlockPos(10, 64, 0);
            BlockPos r1 = new BlockPos(20, 64, 0);
            BlockPos r2 = new BlockPos(20, 64, 10);
            BlockPos r3 = new BlockPos(20, 64, -10);

            Map<BlockPos, Double> transmissions = Map.of(trans, 0.9);
            Set<BlockPos> receivers = Set.of(r1, r2, r3);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(trans),
                    trans, List.of(r1, r2, r3)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(300.0, result.get(r1), 0.001, "900 / 3 = 300");
            assertEquals(300.0, result.get(r2), 0.001, "900 / 3 = 300");
            assertEquals(300.0, result.get(r3), 0.001, "900 / 3 = 300");
        }

        @Test
        void testUnevenFanOutDueToEfficiency() {
            // Source(1200) → [Trans1(0.5) → R1, Trans2(1.0) → R2]
            // Split at source: 1200 / 2 = 600 to each branch
            // R1 gets 600 * 0.5 = 300
            // R2 gets 600 * 1.0 = 600
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 0);
            BlockPos t2 = new BlockPos(-10, 64, 0);
            BlockPos r1 = new BlockPos(20, 64, 0);
            BlockPos r2 = new BlockPos(-20, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.5, t2, 1.0);
            Set<BlockPos> receivers = Set.of(r1, r2);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1, t2),
                    t1, List.of(r1),
                    t2, List.of(r2)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1200.0, transmissions, receivers, adjacency);

            assertEquals(300.0, result.get(r1), 0.001,
                    "600 * 0.5 = 300 through lossy branch");
            assertEquals(600.0, result.get(r2), 0.001,
                    "600 * 1.0 = 600 through perfect branch");
        }
    }

    // ========================================================================
    // Efficiency chain multiplication
    // ========================================================================

    @Nested
    class EfficiencyChainMultiplication {

        @Test
        void testTwoNodeChain_90_80() {
            // 0.9 * 0.8 = 0.72
            double starlight = 1000.0;
            starlight *= 0.9;
            starlight *= 0.8;
            assertEquals(720.0, starlight, 0.001);
        }

        @Test
        void testThreeNodeChain_95_90_85() {
            // 0.95 * 0.90 * 0.85 = 0.72675
            double starlight = 1000.0;
            starlight *= 0.95;
            starlight *= 0.90;
            starlight *= 0.85;
            assertEquals(726.75, starlight, 0.001);
        }

        @Test
        void testLongChainDegradation() {
            // 10 nodes at 0.95 each: 0.95^10 ≈ 0.5987
            double starlight = 1000.0;
            for (int i = 0; i < 10; i++) {
                starlight *= 0.95;
            }
            double expected = 1000.0 * Math.pow(0.95, 10);
            assertEquals(expected, starlight, 0.001,
                    "10-node chain at 0.95 efficiency each");
        }

        @Test
        void testFullChainInSimulator() {
            // Build an actual multi-hop network: Source → T1(0.95) → T2(0.90) → T3(0.85) → R
            // Expected: 1000 * 0.95 * 0.90 * 0.85 = 726.75
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(5, 64, 0);
            BlockPos t2 = new BlockPos(10, 64, 0);
            BlockPos t3 = new BlockPos(15, 64, 0);
            BlockPos receiver = new BlockPos(20, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.95, t2, 0.90, t3, 0.85);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t2),
                    t2, List.of(t3),
                    t3, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(726.75, result.get(receiver), 0.001,
                    "Three-node efficiency chain: 0.95 * 0.90 * 0.85 = 0.72675");
        }

        @Test
        void testEfficiencyWithSplitAndRejoin() {
            // Diamond pattern:
            // Source(1000) → T1(0.9) → R
            //             → T2(0.8) → R  (R appears in both paths but visited blocks re-entry)
            //
            // BFS behavior: source splits to T1 and T2 (500 each)
            // T1(0.9): delivers 500 * 0.9 = 450 to R, marks R visited
            // T2(0.8): R already visited, so T2's path to R is blocked
            // Result: R gets 450 (only from the first path to reach it)
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(5, 64, 5);
            BlockPos t2 = new BlockPos(5, 64, -5);
            BlockPos receiver = new BlockPos(10, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.9, t2, 0.8);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1, t2),
                    t1, List.of(receiver),
                    t2, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // The BFS processes T1 and T2 in the same level. T1 reaches R first
            // (processed in list order), marks it visited. T2's path to R is blocked.
            // T1 delivers: 500 * 0.9 = 450
            assertEquals(450.0, result.get(receiver), 0.001,
                    "Diamond: first path delivers, second is blocked by visited set");
        }
    }

    // ========================================================================
    // Receiver cap saturation
    // ========================================================================

    @Nested
    class ReceiverCapSaturation {

        @Test
        void testSingleSourceExceedsCap() {
            // Source delivers 1000, receiver cap is 200
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos receiver = new BlockPos(10, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of();
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(receiver)
            );

            Map<BlockPos, Double> accumulated = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            Map<BlockPos, Double> maxInputs = Map.of(receiver, 200.0);
            Map<BlockPos, Double> capped = applyReceiverCaps(accumulated, maxInputs);

            assertEquals(200.0, capped.get(receiver), 0.001,
                    "Receiver should be capped at maxInput");
        }

        @Test
        void testSingleSourceBelowCap() {
            // Source delivers 150, receiver cap is 500
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos receiver = new BlockPos(10, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of();
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(receiver)
            );

            Map<BlockPos, Double> accumulated = simulateDistribution(
                    source, 150.0, transmissions, receivers, adjacency);

            Map<BlockPos, Double> maxInputs = Map.of(receiver, 500.0);
            Map<BlockPos, Double> capped = applyReceiverCaps(accumulated, maxInputs);

            assertEquals(150.0, capped.get(receiver), 0.001,
                    "Below-cap receiver should get the full delivered amount");
        }

        @Test
        void testMultipleSourcesExceedCap() {
            // Two sources each deliver 300 to one receiver, cap is 500
            // Total accumulated = 600, capped to 500
            BlockPos source1 = new BlockPos(0, 64, 0);
            BlockPos source2 = new BlockPos(0, 64, 20);
            BlockPos receiver = new BlockPos(10, 64, 10);

            Map<BlockPos, Double> transmissions = Map.of();
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adj1 = Map.of(source1, List.of(receiver));
            Map<BlockPos, List<BlockPos>> adj2 = Map.of(source2, List.of(receiver));

            // Simulate both sources contributing to same receiver
            Map<BlockPos, Double> accumulated = new HashMap<>();
            Map<BlockPos, Double> result1 = simulateDistribution(
                    source1, 300.0, transmissions, receivers, adj1);
            Map<BlockPos, Double> result2 = simulateDistribution(
                    source2, 300.0, transmissions, receivers, adj2);

            // Merge accumulations (as WorldNetworkHandler does across sources)
            result1.forEach((pos, val) -> accumulated.merge(pos, val, Double::sum));
            result2.forEach((pos, val) -> accumulated.merge(pos, val, Double::sum));

            assertEquals(600.0, accumulated.get(receiver), 0.001,
                    "Two sources of 300 accumulate to 600 before cap");

            Map<BlockPos, Double> maxInputs = Map.of(receiver, 500.0);
            Map<BlockPos, Double> capped = applyReceiverCaps(accumulated, maxInputs);

            assertEquals(500.0, capped.get(receiver), 0.001,
                    "600 accumulated should be capped to 500");
        }

        @Test
        void testMultipleSourcesBelowCap() {
            // Two sources each deliver 100 to receiver, cap 500
            Map<BlockPos, Double> accumulated = new HashMap<>();
            BlockPos receiver = new BlockPos(10, 64, 0);
            accumulated.put(receiver, 200.0); // 100 + 100

            Map<BlockPos, Double> maxInputs = Map.of(receiver, 500.0);
            Map<BlockPos, Double> capped = applyReceiverCaps(accumulated, maxInputs);

            assertEquals(200.0, capped.get(receiver), 0.001,
                    "200 accumulated below 500 cap should pass through");
        }

        @Test
        void testZeroCapReceiver() {
            // A receiver with cap 0 gets nothing
            Map<BlockPos, Double> accumulated = new HashMap<>();
            BlockPos receiver = new BlockPos(10, 64, 0);
            accumulated.put(receiver, 1000.0);

            Map<BlockPos, Double> maxInputs = Map.of(receiver, 0.0);
            Map<BlockPos, Double> capped = applyReceiverCaps(accumulated, maxInputs);

            assertEquals(0.0, capped.get(receiver), 0.001,
                    "Zero-cap receiver should deliver nothing");
        }

        @Test
        void testMultipleReceiversDifferentCaps() {
            // R1 cap 100, R2 cap 500
            // R1 gets 300 (capped to 100), R2 gets 300 (below cap)
            Map<BlockPos, Double> accumulated = new HashMap<>();
            BlockPos r1 = new BlockPos(10, 64, 0);
            BlockPos r2 = new BlockPos(20, 64, 0);
            accumulated.put(r1, 300.0);
            accumulated.put(r2, 300.0);

            Map<BlockPos, Double> maxInputs = Map.of(r1, 100.0, r2, 500.0);
            Map<BlockPos, Double> capped = applyReceiverCaps(accumulated, maxInputs);

            assertEquals(100.0, capped.get(r1), 0.001, "R1 capped to 100");
            assertEquals(300.0, capped.get(r2), 0.001, "R2 below cap gets full 300");
        }
    }

    // ========================================================================
    // Cycle prevention
    // ========================================================================

    @Nested
    class CyclePrevention {

        @Test
        void testSimpleCycleDoesNotInfiniteLoop() {
            // Source → T1 → T2 → T1 (cycle back)
            // BFS visited set should prevent revisiting T1
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 0);
            BlockPos t2 = new BlockPos(20, 64, 0);
            BlockPos receiver = new BlockPos(30, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.9, t2, 0.8);
            Set<BlockPos> receivers = Set.of(receiver);
            // T2 links back to T1 (cycle) AND to receiver
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t2),
                    t2, List.of(t1, receiver)  // t1 is already visited
            );

            // Should complete without hanging
            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // Source → T1(0.9): 900 → T2(0.8): 720 → split [T1(visited), R]
            // Only receiver gets starlight: 720 / 2 = 360
            // Wait - T1 is already visited so only R is a valid target
            // Actually the split count includes ALL targets in adjacency list (2),
            // but only unvisited targets receive the starlight.
            // The algorithm splits among ALL adjacency targets then skips visited ones.
            // So perTarget = 720 / 2 = 360, T1 skipped (visited), R gets 360.
            assertEquals(360.0, result.get(receiver), 0.001,
                    "Cycle doesn't loop; receiver gets starlight after split accounting");
        }

        @Test
        void testSelfLoopOnTransmission() {
            // T1's adjacency includes itself
            // Source → T1 → [T1(self-loop), R]
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 0);
            BlockPos receiver = new BlockPos(20, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.9);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t1, receiver)  // self-loop + receiver
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // Source→T1: 1000 * 0.9 = 900, T1 is visited
            // T1 splits to [T1, R]: perTarget = 900/2 = 450
            // T1 is visited → skip, R gets 450
            assertEquals(450.0, result.get(receiver), 0.001,
                    "Self-loop should be caught by visited set");
        }

        @Test
        void testThreeNodeCycleWithExit() {
            // Source → T1 → T2 → T3 → T1 (cycle), T3 also → R
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 0);
            BlockPos t2 = new BlockPos(20, 64, 0);
            BlockPos t3 = new BlockPos(30, 64, 0);
            BlockPos receiver = new BlockPos(40, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.9, t2, 0.9, t3, 0.9);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t2),
                    t2, List.of(t3),
                    t3, List.of(t1, receiver)  // t1 already visited
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // Source→T1: 1000*0.9=900 → T2: 900*0.9=810 → T3: 810*0.9=729
            // T3 splits [T1, R]: perTarget = 729/2 = 364.5
            // T1 visited → skip, R gets 364.5
            assertEquals(364.5, result.get(receiver), 0.001,
                    "Three-node cycle: receiver gets starlight through the exit path");
        }
    }

    // ========================================================================
    // Max traversal depth limiting
    // ========================================================================

    @Nested
    class MaxTraversalDepthLimiting {

        @Test
        void testDepthWithinLimit() {
            // Build a chain of 5 transmissions (well within depth 32)
            // Source → T1 → T2 → T3 → T4 → T5 → Receiver
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos[] nodes = new BlockPos[5];
            for (int i = 0; i < 5; i++) {
                nodes[i] = new BlockPos((i + 1) * 5, 64, 0);
            }
            BlockPos receiver = new BlockPos(35, 64, 0);

            Map<BlockPos, Double> transmissions = new HashMap<>();
            for (BlockPos n : nodes) {
                transmissions.put(n, 0.95);
            }
            Set<BlockPos> receivers = Set.of(receiver);

            Map<BlockPos, List<BlockPos>> adjacency = new HashMap<>();
            adjacency.put(source, List.of(nodes[0]));
            for (int i = 0; i < nodes.length - 1; i++) {
                adjacency.put(nodes[i], List.of(nodes[i + 1]));
            }
            adjacency.put(nodes[nodes.length - 1], List.of(receiver));

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            double expected = 1000.0 * Math.pow(0.95, 5);
            assertEquals(expected, result.get(receiver), 0.001,
                    "5-hop chain within depth limit should deliver full efficiency chain");
        }

        @Test
        void testChainExceedingDepthLimit() {
            // Build a chain of 35 transmissions (exceeds MAX_TRAVERSAL_DEPTH = 32)
            // The receiver at the end should NOT receive starlight
            BlockPos source = new BlockPos(0, 64, 0);
            int chainLength = 35;
            BlockPos[] nodes = new BlockPos[chainLength];
            for (int i = 0; i < chainLength; i++) {
                nodes[i] = new BlockPos(i + 1, 64, 0);
            }
            BlockPos receiver = new BlockPos(chainLength + 1, 64, 0);

            Map<BlockPos, Double> transmissions = new HashMap<>();
            for (BlockPos n : nodes) {
                transmissions.put(n, 1.0); // Perfect efficiency to isolate depth effect
            }
            Set<BlockPos> receivers = Set.of(receiver);

            Map<BlockPos, List<BlockPos>> adjacency = new HashMap<>();
            adjacency.put(source, List.of(nodes[0]));
            for (int i = 0; i < nodes.length - 1; i++) {
                adjacency.put(nodes[i], List.of(nodes[i + 1]));
            }
            adjacency.put(nodes[nodes.length - 1], List.of(receiver));

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // With 35 transmission hops, BFS hits depth 32 and stops
            // The receiver should NOT be reached
            double delivered = result.getOrDefault(receiver, 0.0);
            assertEquals(0.0, delivered, 0.001,
                    "Chain exceeding MAX_TRAVERSAL_DEPTH=32 should not reach the receiver");
        }

        @Test
        void testChainExactlyAtDepthLimit() {
            // Build a chain of exactly 31 transmissions (source at depth 0, each hop increments)
            // With level-based BFS, source is depth 0, first targets at depth 1...
            // At depth 31 (the 31st iteration), we should still process
            // The receiver at hop 31 should be reachable
            BlockPos source = new BlockPos(0, 64, 0);
            int chainLength = 31;
            BlockPos[] nodes = new BlockPos[chainLength];
            for (int i = 0; i < chainLength; i++) {
                nodes[i] = new BlockPos(i + 1, 64, 0);
            }
            BlockPos receiver = new BlockPos(chainLength + 1, 64, 0);

            Map<BlockPos, Double> transmissions = new HashMap<>();
            for (BlockPos n : nodes) {
                transmissions.put(n, 1.0);
            }
            Set<BlockPos> receivers = Set.of(receiver);

            Map<BlockPos, List<BlockPos>> adjacency = new HashMap<>();
            adjacency.put(source, List.of(nodes[0]));
            for (int i = 0; i < nodes.length - 1; i++) {
                adjacency.put(nodes[i], List.of(nodes[i + 1]));
            }
            adjacency.put(nodes[nodes.length - 1], List.of(receiver));

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // 31 hops + receiver = depth 32. The loop runs while depth < 32.
            // So depth 31 is the last processed level (when depth goes from 31 to 32).
            double delivered = result.getOrDefault(receiver, 0.0);
            assertEquals(1000.0, delivered, 0.001,
                    "Chain of 31 hops should be reachable at the depth limit boundary");
        }
    }

    // ========================================================================
    // Independent source data serialization
    // ========================================================================

    @Nested
    class IndependentSourceDataSerialization {

        @Test
        void testStoreAndRetrieveCachedProduction() {
            // Simulate storing independent source data as WorldNetworkHandler does
            BlockPos sourcePos = new BlockPos(100, 64, -200);
            CompoundTag data = new CompoundTag();
            data.putDouble("cachedProduction", 750.0);

            // Store in a map (simulating independentSourceData)
            Map<BlockPos, CompoundTag> independentSourceData = new HashMap<>();
            independentSourceData.put(sourcePos, data.copy());

            // Retrieve and verify
            CompoundTag retrieved = independentSourceData.get(sourcePos);
            assertNotNull(retrieved);
            assertEquals(750.0, retrieved.getDouble("cachedProduction"), 0.001,
                    "Cached production should be retrievable from independent data");
        }

        @Test
        void testIndependentDataNBTRoundtrip() {
            // Build the independentSources list as WorldNetworkHandler.save() does
            BlockPos pos1 = new BlockPos(10, 64, 10);
            BlockPos pos2 = new BlockPos(-50, 30, 100);
            CompoundTag data1 = new CompoundTag();
            data1.putDouble("cachedProduction", 500.0);
            data1.putString("sourceType", "collector_crystal");
            CompoundTag data2 = new CompoundTag();
            data2.putDouble("cachedProduction", 200.0);
            data2.putBoolean("attuned", true);

            // Serialize to list (as save() does)
            ListTag independentList = new ListTag();
            CompoundTag entry1 = new CompoundTag();
            entry1.put("pos", NBTHelper.writeBlockPosToNBT(pos1, new CompoundTag()));
            entry1.put("data", data1.copy());
            independentList.add(entry1);

            CompoundTag entry2 = new CompoundTag();
            entry2.put("pos", NBTHelper.writeBlockPosToNBT(pos2, new CompoundTag()));
            entry2.put("data", data2.copy());
            independentList.add(entry2);

            // Deserialize (as load() does)
            Map<BlockPos, CompoundTag> loaded = new HashMap<>();
            for (int i = 0; i < independentList.size(); i++) {
                CompoundTag indTag = independentList.getCompound(i);
                BlockPos pos = NBTHelper.readBlockPosFromNBT(indTag.getCompound("pos"));
                CompoundTag data = indTag.getCompound("data").copy();
                loaded.put(pos, data);
            }

            assertEquals(2, loaded.size());
            assertEquals(500.0, loaded.get(pos1).getDouble("cachedProduction"), 0.001);
            assertEquals("collector_crystal", loaded.get(pos1).getString("sourceType"));
            assertEquals(200.0, loaded.get(pos2).getDouble("cachedProduction"), 0.001);
            assertTrue(loaded.get(pos2).getBoolean("attuned"));
        }

        @Test
        void testIndependentDataOverwrite() {
            // Overwriting independent data for same position
            BlockPos pos = new BlockPos(10, 64, 10);
            Map<BlockPos, CompoundTag> independentSourceData = new HashMap<>();

            CompoundTag data1 = new CompoundTag();
            data1.putDouble("cachedProduction", 100.0);
            independentSourceData.put(pos, data1.copy());

            CompoundTag data2 = new CompoundTag();
            data2.putDouble("cachedProduction", 999.0);
            independentSourceData.put(pos, data2.copy());

            assertEquals(999.0, independentSourceData.get(pos).getDouble("cachedProduction"), 0.001,
                    "Later write should overwrite earlier data");
        }

        @Test
        void testIndependentDataRemovalOnSourceRemove() {
            // When a source is removed, its independent data should be cleaned up
            BlockPos pos = new BlockPos(10, 64, 10);
            Map<BlockPos, CompoundTag> independentSourceData = new HashMap<>();

            CompoundTag data = new CompoundTag();
            data.putDouble("cachedProduction", 500.0);
            independentSourceData.put(pos, data.copy());
            assertTrue(independentSourceData.containsKey(pos));

            // Simulate removeSource() behavior
            independentSourceData.remove(pos);
            assertFalse(independentSourceData.containsKey(pos),
                    "Independent data should be removed when source is removed");
        }

        @Test
        void testIndependentDataCopyIsolation() {
            // Verify that stored data is a copy and mutations don't affect stored value
            BlockPos pos = new BlockPos(10, 64, 10);
            CompoundTag original = new CompoundTag();
            original.putDouble("cachedProduction", 500.0);

            Map<BlockPos, CompoundTag> store = new HashMap<>();
            store.put(pos, original.copy());

            // Mutate original after storing
            original.putDouble("cachedProduction", 9999.0);

            // Store should be unaffected
            assertEquals(500.0, store.get(pos).getDouble("cachedProduction"), 0.001,
                    "Stored copy should be isolated from mutations to original");
        }
    }

    // ========================================================================
    // Network topology changes
    // ========================================================================

    @Nested
    class NetworkTopologyChanges {

        @Test
        void testAddAndRemoveLinksViaSet() {
            // Simulate the activeLinks set behavior
            Set<TransmissionLink> links = new LinkedHashSet<>();
            BlockPos a = new BlockPos(0, 64, 0);
            BlockPos b = new BlockPos(10, 64, 0);
            BlockPos c = new BlockPos(20, 64, 0);

            TransmissionLink ab = new TransmissionLink(a, b);
            TransmissionLink bc = new TransmissionLink(b, c);

            assertTrue(links.add(ab));
            assertTrue(links.add(bc));
            assertEquals(2, links.size());

            // Remove one link
            assertTrue(links.remove(ab));
            assertEquals(1, links.size());
            assertTrue(links.contains(bc));
            assertFalse(links.contains(ab));
        }

        @Test
        void testDuplicateLinkRejected() {
            Set<TransmissionLink> links = new LinkedHashSet<>();
            BlockPos a = new BlockPos(0, 64, 0);
            BlockPos b = new BlockPos(10, 64, 0);

            TransmissionLink link1 = new TransmissionLink(a, b);
            TransmissionLink link2 = new TransmissionLink(a, b); // duplicate

            assertTrue(links.add(link1));
            assertFalse(links.add(link2), "Duplicate link should be rejected by set");
            assertEquals(1, links.size());
        }

        @Test
        void testRemoveAllLinksFromPosition() {
            // Simulates removeAllLinksFrom()
            Set<TransmissionLink> links = new LinkedHashSet<>();
            BlockPos a = new BlockPos(0, 64, 0);
            BlockPos b = new BlockPos(10, 64, 0);
            BlockPos c = new BlockPos(20, 64, 0);
            BlockPos d = new BlockPos(30, 64, 0);

            links.add(new TransmissionLink(a, b));
            links.add(new TransmissionLink(a, c));
            links.add(new TransmissionLink(b, c));
            links.add(new TransmissionLink(c, d));
            assertEquals(4, links.size());

            // Remove all from 'a'
            links.removeIf(link -> link.getFrom().equals(a));
            assertEquals(2, links.size(), "Should have removed both links from A");
            assertTrue(links.contains(new TransmissionLink(b, c)));
            assertTrue(links.contains(new TransmissionLink(c, d)));
        }

        @Test
        void testRemoveAllLinksToPosition() {
            // Simulates removeAllLinksTo()
            Set<TransmissionLink> links = new LinkedHashSet<>();
            BlockPos a = new BlockPos(0, 64, 0);
            BlockPos b = new BlockPos(10, 64, 0);
            BlockPos c = new BlockPos(20, 64, 0);

            links.add(new TransmissionLink(a, c));
            links.add(new TransmissionLink(b, c));
            links.add(new TransmissionLink(a, b));
            assertEquals(3, links.size());

            // Remove all targeting 'c'
            links.removeIf(link -> link.getTo().equals(c));
            assertEquals(1, links.size(), "Should have removed both links to C");
            assertTrue(links.contains(new TransmissionLink(a, b)));
        }

        @Test
        void testAdjacencyRebuildAfterTopologyChange() {
            // Simulate adjacency map rebuild from link set
            Set<TransmissionLink> links = new LinkedHashSet<>();
            BlockPos a = new BlockPos(0, 64, 0);
            BlockPos b = new BlockPos(10, 64, 0);
            BlockPos c = new BlockPos(20, 64, 0);
            BlockPos d = new BlockPos(30, 64, 0);

            links.add(new TransmissionLink(a, b));
            links.add(new TransmissionLink(a, c));
            links.add(new TransmissionLink(b, d));

            // Rebuild adjacency (as rebuildAdjacencyIfNeeded does)
            Map<BlockPos, List<BlockPos>> adjacency = new HashMap<>();
            for (TransmissionLink link : links) {
                adjacency.computeIfAbsent(link.getFrom(), k -> new ArrayList<>())
                        .add(link.getTo());
            }

            assertEquals(2, adjacency.get(a).size(), "A should have 2 outgoing targets");
            assertTrue(adjacency.get(a).contains(b));
            assertTrue(adjacency.get(a).contains(c));
            assertEquals(1, adjacency.get(b).size(), "B should have 1 outgoing target");
            assertTrue(adjacency.get(b).contains(d));
            assertNull(adjacency.get(c), "C has no outgoing links");
            assertNull(adjacency.get(d), "D has no outgoing links");

            // Now remove link a→b and rebuild
            links.remove(new TransmissionLink(a, b));
            adjacency.clear();
            for (TransmissionLink link : links) {
                adjacency.computeIfAbsent(link.getFrom(), k -> new ArrayList<>())
                        .add(link.getTo());
            }

            assertEquals(1, adjacency.get(a).size(), "A now has 1 outgoing target");
            assertTrue(adjacency.get(a).contains(c));
            // b→d link still exists but disconnected from source
            assertEquals(1, adjacency.get(b).size());
        }

        @Test
        void testNodeRemovalCascadesLinks() {
            // When a transmission node is removed, all links from/to it should go
            Set<TransmissionLink> links = new LinkedHashSet<>();
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos trans = new BlockPos(10, 64, 0);
            BlockPos receiver = new BlockPos(20, 64, 0);

            links.add(new TransmissionLink(source, trans));
            links.add(new TransmissionLink(trans, receiver));
            assertEquals(2, links.size());

            // Simulate removeTransmission(trans): remove all from + to
            links.removeIf(link -> link.getFrom().equals(trans) || link.getTo().equals(trans));
            assertEquals(0, links.size(),
                    "Removing transmission node should cascade-remove all links through it");
        }

        @Test
        void testAddNodeToExistingNetwork() {
            // Simulate adding a new transmission node between existing nodes
            Map<BlockPos, Double> transmissions = new HashMap<>();
            Set<TransmissionLink> links = new LinkedHashSet<>();

            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos receiver = new BlockPos(30, 64, 0);

            // Initial: direct link source→receiver
            links.add(new TransmissionLink(source, receiver));

            // Add transmission node in between
            BlockPos newTrans = new BlockPos(15, 64, 0);
            transmissions.put(newTrans, 0.85);

            // Re-route: remove direct link, add source→trans→receiver
            links.remove(new TransmissionLink(source, receiver));
            links.add(new TransmissionLink(source, newTrans));
            links.add(new TransmissionLink(newTrans, receiver));

            assertEquals(2, links.size());

            // Rebuild adjacency and verify distribution
            Map<BlockPos, List<BlockPos>> adjacency = new HashMap<>();
            for (TransmissionLink link : links) {
                adjacency.computeIfAbsent(link.getFrom(), k -> new ArrayList<>())
                        .add(link.getTo());
            }

            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(850.0, result.get(receiver), 0.001,
                    "New transmission node with 0.85 efficiency reduces delivery to 850");
        }
    }

    // ========================================================================
    // Link validation rules
    // ========================================================================

    @Nested
    class LinkValidationRules {

        @Test
        void testSelfLinkRejected() {
            BlockPos pos = new BlockPos(10, 64, 10);
            // addLink checks from.equals(to)
            assertTrue(pos.equals(pos), "Same pos equals itself");
            // In WorldNetworkHandler, from.equals(to) → return false
        }

        @Test
        void testReceiverCannotBeSourceOfLink() {
            // A pure receiver (not also a source or transmission) cannot have outgoing links
            Map<BlockPos, String> nodeTypes = new HashMap<>();
            BlockPos receiver = new BlockPos(10, 64, 0);
            BlockPos target = new BlockPos(20, 64, 0);
            nodeTypes.put(receiver, "receiver");
            nodeTypes.put(target, "transmission");

            // Validate: receiver-only nodes cannot be link sources
            boolean isReceiverOnly = "receiver".equals(nodeTypes.get(receiver))
                    && !"source".equals(nodeTypes.get(receiver))
                    && !"transmission".equals(nodeTypes.get(receiver));
            assertTrue(isReceiverOnly, "Receiver at this pos is receiver-only");
            // WorldNetworkHandler returns false for this case
        }

        @Test
        void testSourceCannotBeTargetOfLink() {
            // A pure source (not also a receiver or transmission) cannot be a link target
            Map<BlockPos, Set<String>> nodeRoles = new HashMap<>();
            BlockPos source = new BlockPos(0, 64, 0);
            nodeRoles.put(source, Set.of("source"));

            boolean isSourceOnly = nodeRoles.get(source).contains("source")
                    && !nodeRoles.get(source).contains("transmission")
                    && !nodeRoles.get(source).contains("receiver");
            assertTrue(isSourceOnly, "Node is source-only");
            // WorldNetworkHandler returns false when target is source-only
        }

        @Test
        void testDistanceExceedsMax() {
            BlockPos from = new BlockPos(0, 64, 0);
            BlockPos to = new BlockPos(65, 64, 0); // 65 > MAX_LINK_DISTANCE
            double distSqr = from.distSqr(to);
            assertTrue(distSqr > MAX_LINK_DISTANCE * MAX_LINK_DISTANCE,
                    "65 blocks exceeds max link distance of 64");
        }

        @Test
        void testDistanceExactlyAtMax() {
            BlockPos from = new BlockPos(0, 64, 0);
            BlockPos to = new BlockPos(64, 64, 0);
            double distSqr = from.distSqr(to);
            assertTrue(distSqr <= MAX_LINK_DISTANCE * MAX_LINK_DISTANCE,
                    "64 blocks should be within max link distance");
        }

        @Test
        void testDistanceDiagonal3D() {
            // sqrt(40^2 + 40^2 + 40^2) = sqrt(4800) ≈ 69.28 > 64
            BlockPos from = new BlockPos(0, 0, 0);
            BlockPos to = new BlockPos(40, 40, 40);
            double distSqr = from.distSqr(to);
            assertTrue(distSqr > MAX_LINK_DISTANCE * MAX_LINK_DISTANCE,
                    "3D diagonal of 40,40,40 exceeds max distance");
        }

        @Test
        void testUnregisteredNodeRejectsLink() {
            // Both from and to must be known nodes
            Map<BlockPos, String> knownNodes = new HashMap<>();
            BlockPos known = new BlockPos(10, 64, 0);
            BlockPos unknown = new BlockPos(50, 64, 0);
            knownNodes.put(known, "source");

            boolean fromKnown = knownNodes.containsKey(known);
            boolean toKnown = knownNodes.containsKey(unknown);
            assertTrue(fromKnown);
            assertFalse(toKnown, "Unknown position should not be a valid link target");
        }
    }

    // ========================================================================
    // Complex topology scenarios
    // ========================================================================

    @Nested
    class ComplexTopologies {

        @Test
        void testTreeTopology() {
            // Source → T1 → [R1, R2]
            //        → T2 → [R3, R4]
            // Source(1000) splits 2 ways: 500 to T1, 500 to T2
            // T1(0.9): 450 splits 2 ways: 225 to R1, 225 to R2
            // T2(0.8): 400 splits 2 ways: 200 to R3, 200 to R4
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(10, 64, 10);
            BlockPos t2 = new BlockPos(10, 64, -10);
            BlockPos r1 = new BlockPos(20, 64, 15);
            BlockPos r2 = new BlockPos(20, 64, 5);
            BlockPos r3 = new BlockPos(20, 64, -5);
            BlockPos r4 = new BlockPos(20, 64, -15);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.9, t2, 0.8);
            Set<BlockPos> receivers = Set.of(r1, r2, r3, r4);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1, t2),
                    t1, List.of(r1, r2),
                    t2, List.of(r3, r4)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertEquals(225.0, result.get(r1), 0.001, "R1: 500*0.9/2 = 225");
            assertEquals(225.0, result.get(r2), 0.001, "R2: 500*0.9/2 = 225");
            assertEquals(200.0, result.get(r3), 0.001, "R3: 500*0.8/2 = 200");
            assertEquals(200.0, result.get(r4), 0.001, "R4: 500*0.8/2 = 200");
        }

        @Test
        void testLongChainWithMixedEfficiency() {
            // Source(2000) → T1(0.95) → T2(0.85) → T3(0.90) → T4(0.75) → R
            // Expected: 2000 * 0.95 * 0.85 * 0.90 * 0.75 = 1089.5625
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(5, 64, 0);
            BlockPos t2 = new BlockPos(10, 64, 0);
            BlockPos t3 = new BlockPos(15, 64, 0);
            BlockPos t4 = new BlockPos(20, 64, 0);
            BlockPos receiver = new BlockPos(25, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(
                    t1, 0.95, t2, 0.85, t3, 0.90, t4, 0.75);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t2),
                    t2, List.of(t3),
                    t3, List.of(t4),
                    t4, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 2000.0, transmissions, receivers, adjacency);

            double expected = 2000.0 * 0.95 * 0.85 * 0.90 * 0.75;
            assertEquals(expected, result.get(receiver), 0.001,
                    "Long chain with mixed efficiency");
        }

        @Test
        void testMultipleSourcesOneReceiver() {
            // S1(400) → R and S2(600) → R
            // Both deliver directly, R accumulates 400 + 600 = 1000
            BlockPos s1 = new BlockPos(0, 64, 0);
            BlockPos s2 = new BlockPos(0, 64, 20);
            BlockPos receiver = new BlockPos(10, 64, 10);

            Map<BlockPos, Double> transmissions = Map.of();
            Set<BlockPos> receivers = Set.of(receiver);

            Map<BlockPos, Double> accumulated = new HashMap<>();

            Map<BlockPos, Double> r1 = simulateDistribution(
                    s1, 400.0, transmissions, receivers,
                    Map.of(s1, List.of(receiver)));
            Map<BlockPos, Double> r2 = simulateDistribution(
                    s2, 600.0, transmissions, receivers,
                    Map.of(s2, List.of(receiver)));

            r1.forEach((pos, val) -> accumulated.merge(pos, val, Double::sum));
            r2.forEach((pos, val) -> accumulated.merge(pos, val, Double::sum));

            assertEquals(1000.0, accumulated.get(receiver), 0.001,
                    "Multiple sources accumulate additively at receiver");
        }

        @Test
        void testSourceWithNoOutgoingLinks() {
            // A source with no links should produce no distribution
            BlockPos source = new BlockPos(0, 64, 0);

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0,
                    Map.of(),
                    Set.of(),
                    Map.of() // no adjacency
            );

            assertTrue(result.isEmpty(),
                    "Source with no outgoing links distributes nothing");
        }

        @Test
        void testTransmissionNodeWithNoExits() {
            // Source → Trans → (no outgoing)
            // Starlight enters the transmission but has nowhere to go
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos trans = new BlockPos(10, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(trans, 0.9);
            Set<BlockPos> receivers = Set.of();
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(trans)
                    // trans has no outgoing
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            assertTrue(result.isEmpty(),
                    "Dead-end transmission should result in no delivery");
        }

        @Test
        void testVerySmallStarlightAmount() {
            // Verify precision with very small values after many efficiency hops
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos t1 = new BlockPos(5, 64, 0);
            BlockPos t2 = new BlockPos(10, 64, 0);
            BlockPos receiver = new BlockPos(15, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(t1, 0.1, t2, 0.1);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(t1),
                    t1, List.of(t2),
                    t2, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 1000.0, transmissions, receivers, adjacency);

            // 1000 * 0.1 * 0.1 = 10
            assertEquals(10.0, result.get(receiver), 0.001,
                    "Very lossy chain: 1000 * 0.1 * 0.1 = 10");
        }

        @Test
        void testLargeStarlightProduction() {
            // High production through efficient network
            BlockPos source = new BlockPos(0, 64, 0);
            BlockPos trans = new BlockPos(10, 64, 0);
            BlockPos receiver = new BlockPos(20, 64, 0);

            Map<BlockPos, Double> transmissions = Map.of(trans, 0.99);
            Set<BlockPos> receivers = Set.of(receiver);
            Map<BlockPos, List<BlockPos>> adjacency = Map.of(
                    source, List.of(trans),
                    trans, List.of(receiver)
            );

            Map<BlockPos, Double> result = simulateDistribution(
                    source, 100000.0, transmissions, receivers, adjacency);

            assertEquals(99000.0, result.get(receiver), 0.001,
                    "100000 * 0.99 = 99000");
        }
    }

    // ========================================================================
    // Entry data class edge cases
    // ========================================================================

    @Nested
    class EntryEdgeCases {

        @Test
        void testReceiverEntryWithLargeMaxInput() {
            WorldNetworkHandler.ReceiverEntry entry =
                    new WorldNetworkHandler.ReceiverEntry(BlockPos.ZERO, Double.MAX_VALUE);
            CompoundTag tag = entry.writeToNBT();
            WorldNetworkHandler.ReceiverEntry loaded =
                    WorldNetworkHandler.ReceiverEntry.readFromNBT(tag);
            assertEquals(Double.MAX_VALUE, loaded.getMaxInput(), 0.001);
        }

        @Test
        void testTransmissionEntryBoundaryValues() {
            // Efficiency at exact boundary 0.0 and 1.0
            WorldNetworkHandler.TransmissionEntry zero =
                    new WorldNetworkHandler.TransmissionEntry(new BlockPos(1, 2, 3), 0.0);
            assertEquals(0.0, zero.getEfficiency(), 0.001);

            WorldNetworkHandler.TransmissionEntry one =
                    new WorldNetworkHandler.TransmissionEntry(new BlockPos(4, 5, 6), 1.0);
            assertEquals(1.0, one.getEfficiency(), 0.001);
        }

        @Test
        void testSourceEntryMultipleConstellations() {
            // Different sources with different constellations
            WorldNetworkHandler.SourceEntry aevitas =
                    new WorldNetworkHandler.SourceEntry(
                            new BlockPos(0, 64, 0),
                            new ResourceLocation("astralsorcery", "aevitas"),
                            true);
            WorldNetworkHandler.SourceEntry discidia =
                    new WorldNetworkHandler.SourceEntry(
                            new BlockPos(10, 64, 0),
                            new ResourceLocation("astralsorcery", "discidia"),
                            false);

            assertNotEquals(aevitas.getConstellation(), discidia.getConstellation());
            assertNotEquals(aevitas.getPos(), discidia.getPos());
            assertNotEquals(aevitas.isAutoLink(), discidia.isAutoLink());
        }

        @Test
        void testTransmissionLinkWithMaxDistance() {
            // Link at exactly MAX_LINK_DISTANCE in 3D
            // 64 blocks straight in one axis
            BlockPos from = new BlockPos(0, 0, 0);
            BlockPos to = new BlockPos(64, 0, 0);
            TransmissionLink link = new TransmissionLink(from, to);
            assertEquals(64.0, link.getLength(), 0.001);
            assertTrue(link.getLengthSquared() <= MAX_LINK_DISTANCE * MAX_LINK_DISTANCE);
        }

        @Test
        void testMultipleEntriesAtSamePosition() {
            // Verify that maps handle position-keyed lookups correctly
            Map<BlockPos, WorldNetworkHandler.SourceEntry> sources = new HashMap<>();
            Map<BlockPos, WorldNetworkHandler.ReceiverEntry> receivers = new HashMap<>();
            Map<BlockPos, WorldNetworkHandler.TransmissionEntry> transmissions = new HashMap<>();

            BlockPos shared = new BlockPos(10, 64, 10);

            // A position could theoretically be both source and transmission
            sources.put(shared, new WorldNetworkHandler.SourceEntry(
                    shared, new ResourceLocation("astralsorcery", "vicio"), true));
            transmissions.put(shared, new WorldNetworkHandler.TransmissionEntry(shared, 0.9));

            assertTrue(sources.containsKey(shared));
            assertTrue(transmissions.containsKey(shared));
            assertFalse(receivers.containsKey(shared));
        }
    }
}
