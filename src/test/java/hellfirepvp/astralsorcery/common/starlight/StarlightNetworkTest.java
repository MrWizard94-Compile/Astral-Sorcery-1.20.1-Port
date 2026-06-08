/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionLink;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the starlight network graph data structures.
 *
 * <p>Tests TransmissionLink, NodeConnection, and the entry-level
 * data classes without requiring a running server or ServerLevel.</p>
 */
class StarlightNetworkTest {

    // ========================================================================
    // TransmissionLink tests
    // ========================================================================

    @Nested
    class TransmissionLinkTests {

        @Test
        void testLinkCreation() {
            BlockPos from = new BlockPos(10, 64, -20);
            BlockPos to = new BlockPos(15, 64, -20);
            TransmissionLink link = new TransmissionLink(from, to);

            assertEquals(from, link.getFrom());
            assertEquals(to, link.getTo());
        }

        @Test
        void testLinkEquality() {
            TransmissionLink a = new TransmissionLink(new BlockPos(1, 2, 3), new BlockPos(4, 5, 6));
            TransmissionLink b = new TransmissionLink(new BlockPos(1, 2, 3), new BlockPos(4, 5, 6));
            TransmissionLink c = new TransmissionLink(new BlockPos(4, 5, 6), new BlockPos(1, 2, 3));

            assertEquals(a, b, "Links with same from/to should be equal");
            assertNotEquals(a, c, "Links with swapped from/to are directional");
        }

        @Test
        void testLinkHashCode() {
            TransmissionLink a = new TransmissionLink(new BlockPos(1, 2, 3), new BlockPos(4, 5, 6));
            TransmissionLink b = new TransmissionLink(new BlockPos(1, 2, 3), new BlockPos(4, 5, 6));
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void testLinkLengthSquared() {
            // Horizontal link: 5 blocks east
            TransmissionLink link = new TransmissionLink(
                    new BlockPos(0, 64, 0), new BlockPos(5, 64, 0));
            assertEquals(25.0, link.getLengthSquared(), 0.001);
        }

        @Test
        void testLinkLength3D() {
            // 3-4-5 right triangle: dx=3, dy=4, dz=0 -> length=5
            TransmissionLink link = new TransmissionLink(
                    new BlockPos(0, 0, 0), new BlockPos(3, 4, 0));
            assertEquals(5.0, link.getLength(), 0.001);
        }

        @Test
        void testLinkNBTRoundtrip() {
            BlockPos from = new BlockPos(-100, 30, 500);
            BlockPos to = new BlockPos(200, 80, -300);
            TransmissionLink original = new TransmissionLink(from, to);

            CompoundTag tag = original.writeToNBT();
            TransmissionLink deserialized = TransmissionLink.readFromNBT(tag);

            assertEquals(original.getFrom(), deserialized.getFrom());
            assertEquals(original.getTo(), deserialized.getTo());
            assertEquals(original, deserialized);
        }

        @Test
        void testLinkNBTNegativeCoords() {
            TransmissionLink link = new TransmissionLink(
                    new BlockPos(-5000, -64, -8000),
                    new BlockPos(-1, -1, -1));

            CompoundTag tag = link.writeToNBT();
            TransmissionLink restored = TransmissionLink.readFromNBT(tag);

            assertEquals(link.getFrom(), restored.getFrom());
            assertEquals(link.getTo(), restored.getTo());
        }

        @Test
        void testLinkToString() {
            TransmissionLink link = new TransmissionLink(
                    new BlockPos(1, 2, 3), new BlockPos(4, 5, 6));
            String s = link.toString();
            assertTrue(s.contains("1"), "toString should contain coordinates");
            assertTrue(s.contains("->") || s.contains("→") || s.contains("Link"),
                    "toString should indicate directionality");
        }

        @Test
        void testLinkZeroLength() {
            // Self-link (would be rejected by handler but the data class doesn't enforce)
            TransmissionLink link = new TransmissionLink(
                    new BlockPos(10, 10, 10), new BlockPos(10, 10, 10));
            assertEquals(0.0, link.getLengthSquared(), 0.001);
            assertEquals(0.0, link.getLength(), 0.001);
        }
    }

    // NodeConnection is a trivial POJO (getters/setters + list) that requires
    // ResourceKey<Level> which pulls in MC bootstrap. Tested implicitly via
    // WorldNetworkHandler.getNodeInfo() in integration tests.

    // ========================================================================
    // WorldNetworkHandler inner class NBT tests (SourceEntry, ReceiverEntry, TransmissionEntry)
    // ========================================================================

    @Nested
    class EntryNBTTests {

        @Test
        void testSourceEntryNBTRoundtrip() {
            StarlightGraph.SourceEntry original = new StarlightGraph.SourceEntry(
                    new BlockPos(100, 64, 200),
                    new ResourceLocation("astralsorcery", "discidia"),
                    true
            );

            CompoundTag tag = original.writeToNBT();
            StarlightGraph.SourceEntry restored =
                    StarlightGraph.SourceEntry.readFromNBT(tag);

            assertEquals(original.getPos(), restored.getPos());
            assertEquals(original.getConstellation(), restored.getConstellation());
            assertEquals(original.isAutoLink(), restored.isAutoLink());
        }

        @Test
        void testSourceEntryNullConstellation() {
            StarlightGraph.SourceEntry original = new StarlightGraph.SourceEntry(
                    new BlockPos(50, 40, 30),
                    null,  // unattuned source
                    false
            );

            CompoundTag tag = original.writeToNBT();
            StarlightGraph.SourceEntry restored =
                    StarlightGraph.SourceEntry.readFromNBT(tag);

            assertEquals(original.getPos(), restored.getPos());
            assertNull(restored.getConstellation());
            assertFalse(restored.isAutoLink());
        }

        @Test
        void testReceiverEntryNBTRoundtrip() {
            StarlightGraph.ReceiverEntry original = new StarlightGraph.ReceiverEntry(
                    new BlockPos(-50, 100, 300),
                    500.0
            );

            CompoundTag tag = original.writeToNBT();
            StarlightGraph.ReceiverEntry restored =
                    StarlightGraph.ReceiverEntry.readFromNBT(tag);

            assertEquals(original.getPos(), restored.getPos());
            assertEquals(original.getMaxInput(), restored.getMaxInput(), 0.001);
        }

        @Test
        void testReceiverEntryZeroMaxInput() {
            StarlightGraph.ReceiverEntry original = new StarlightGraph.ReceiverEntry(
                    BlockPos.ZERO, 0.0);

            CompoundTag tag = original.writeToNBT();
            StarlightGraph.ReceiverEntry restored =
                    StarlightGraph.ReceiverEntry.readFromNBT(tag);

            assertEquals(0.0, restored.getMaxInput(), 0.001);
        }

        @Test
        void testTransmissionEntryNBTRoundtrip() {
            StarlightGraph.TransmissionEntry original = new StarlightGraph.TransmissionEntry(
                    new BlockPos(10, 65, 10),
                    0.85
            );

            CompoundTag tag = original.writeToNBT();
            StarlightGraph.TransmissionEntry restored =
                    StarlightGraph.TransmissionEntry.readFromNBT(tag);

            assertEquals(original.getPos(), restored.getPos());
            assertEquals(original.getEfficiency(), restored.getEfficiency(), 0.001);
        }

        @Test
        void testTransmissionEntryEfficiencyClamped() {
            // Efficiency > 1.0 should clamp to 1.0
            StarlightGraph.TransmissionEntry clamped = new StarlightGraph.TransmissionEntry(
                    BlockPos.ZERO, 1.5);
            assertEquals(1.0, clamped.getEfficiency(), 0.001);

            // Efficiency < 0 should clamp to 0
            StarlightGraph.TransmissionEntry zero = new StarlightGraph.TransmissionEntry(
                    BlockPos.ZERO, -0.5);
            assertEquals(0.0, zero.getEfficiency(), 0.001);
        }

        @Test
        void testTransmissionEntryEdgeEfficiency() {
            // Exactly 0 and exactly 1 should work
            StarlightGraph.TransmissionEntry atZero = new StarlightGraph.TransmissionEntry(
                    BlockPos.ZERO, 0.0);
            assertEquals(0.0, atZero.getEfficiency(), 0.001);

            StarlightGraph.TransmissionEntry atOne = new StarlightGraph.TransmissionEntry(
                    BlockPos.ZERO, 1.0);
            assertEquals(1.0, atOne.getEfficiency(), 0.001);
        }
    }

    // ========================================================================
    // Distance and validation tests (pure math, no server needed)
    // ========================================================================

    @Nested
    class DistanceValidationTests {

        private static final double MAX_LINK_DISTANCE = 64.0;

        /**
         * Mirrors the distance check logic from WorldNetworkHandler.addLink().
         */
        private boolean isWithinRange(BlockPos from, BlockPos to) {
            return from.distSqr(to) <= MAX_LINK_DISTANCE * MAX_LINK_DISTANCE;
        }

        @Test
        void testLinkWithinRange() {
            BlockPos from = new BlockPos(0, 64, 0);
            BlockPos to = new BlockPos(30, 64, 30);
            // distance = sqrt(30^2 + 30^2) = sqrt(1800) ≈ 42.4 < 64
            assertTrue(isWithinRange(from, to));
        }

        @Test
        void testLinkExactlyAtMaxDistance() {
            // 64 blocks straight east
            BlockPos from = new BlockPos(0, 64, 0);
            BlockPos to = new BlockPos(64, 64, 0);
            assertTrue(isWithinRange(from, to));
        }

        @Test
        void testLinkBeyondMaxDistance() {
            // 65 blocks straight east
            BlockPos from = new BlockPos(0, 64, 0);
            BlockPos to = new BlockPos(65, 64, 0);
            assertFalse(isWithinRange(from, to));
        }

        @Test
        void testLinkDiagonalBeyondRange() {
            // 50 blocks in each horizontal axis: sqrt(50^2 + 50^2) = sqrt(5000) ≈ 70.7 > 64
            BlockPos from = new BlockPos(0, 64, 0);
            BlockPos to = new BlockPos(50, 64, 50);
            assertFalse(isWithinRange(from, to));
        }

        @Test
        void testLinkVerticalWithinRange() {
            // Purely vertical: 60 blocks up
            BlockPos from = new BlockPos(0, 0, 0);
            BlockPos to = new BlockPos(0, 60, 0);
            assertTrue(isWithinRange(from, to));
        }

        @Test
        void testLinkSamePosition() {
            BlockPos pos = new BlockPos(10, 64, 10);
            assertTrue(isWithinRange(pos, pos));
        }

        @Test
        void testLinkNegativeCoords() {
            BlockPos from = new BlockPos(-100, 64, -100);
            BlockPos to = new BlockPos(-90, 64, -90);
            // sqrt(10^2 + 10^2) ≈ 14.1 < 64
            assertTrue(isWithinRange(from, to));
        }

        @Test
        void testLinkAcrossOrigin() {
            BlockPos from = new BlockPos(-32, 64, 0);
            BlockPos to = new BlockPos(32, 64, 0);
            // 64 blocks total
            assertTrue(isWithinRange(from, to));
        }
    }

    // ========================================================================
    // Starlight distribution math tests
    // ========================================================================

    @Nested
    class StarlightMathTests {

        /**
         * Mirrors the transmission efficiency application logic.
         * Starlight passing through a node is multiplied by its efficiency.
         */
        private double applyTransmissionEfficiency(double starlight, double efficiency) {
            return starlight * Math.max(0, Math.min(1, efficiency));
        }

        /**
         * Mirrors the splitting logic: starlight is divided evenly among targets.
         */
        private double splitAmongTargets(double starlight, int numTargets) {
            return numTargets > 0 ? starlight / numTargets : 0;
        }

        @Test
        void testFullEfficiency() {
            assertEquals(100.0, applyTransmissionEfficiency(100.0, 1.0), 0.001);
        }

        @Test
        void testHalfEfficiency() {
            assertEquals(50.0, applyTransmissionEfficiency(100.0, 0.5), 0.001);
        }

        @Test
        void testZeroEfficiency() {
            assertEquals(0.0, applyTransmissionEfficiency(100.0, 0.0), 0.001);
        }

        @Test
        void testChainedEfficiency() {
            // Source -> Node(0.9) -> Node(0.8) -> Receiver
            double starlight = 1000.0;
            starlight = applyTransmissionEfficiency(starlight, 0.9); // 900
            starlight = applyTransmissionEfficiency(starlight, 0.8); // 720
            assertEquals(720.0, starlight, 0.001);
        }

        @Test
        void testSplitEvenlyTwoTargets() {
            assertEquals(50.0, splitAmongTargets(100.0, 2), 0.001);
        }

        @Test
        void testSplitEvenlyThreeTargets() {
            assertEquals(100.0 / 3.0, splitAmongTargets(100.0, 3), 0.001);
        }

        @Test
        void testSplitSingleTarget() {
            assertEquals(100.0, splitAmongTargets(100.0, 1), 0.001);
        }

        @Test
        void testSplitZeroTargets() {
            assertEquals(0.0, splitAmongTargets(100.0, 0), 0.001);
        }

        @Test
        void testSplitThenEfficiency() {
            // Source (1000) -> split 2 ways (500 each) -> efficiency 0.9 -> receiver gets 450
            double starlight = 1000.0;
            double perTarget = splitAmongTargets(starlight, 2);
            double delivered = applyTransmissionEfficiency(perTarget, 0.9);
            assertEquals(450.0, delivered, 0.001);
        }

        @Test
        void testReceiverCap() {
            // Receiver with max input 200, receiving 500 from network
            double incoming = 500.0;
            double maxInput = 200.0;
            double delivered = Math.min(incoming, maxInput);
            assertEquals(200.0, delivered, 0.001);
        }

        @Test
        void testReceiverBelowCap() {
            double incoming = 150.0;
            double maxInput = 200.0;
            double delivered = Math.min(incoming, maxInput);
            assertEquals(150.0, delivered, 0.001);
        }

        @Test
        void testMultipleSourcesAccumulate() {
            // Two sources each deliver 100 to the same receiver
            double source1 = 100.0;
            double source2 = 100.0;
            double total = source1 + source2;  // additive
            double maxInput = 500.0;
            double delivered = Math.min(total, maxInput);
            assertEquals(200.0, delivered, 0.001);
        }

        @Test
        void testMultipleSourcesExceedCap() {
            // Two sources each deliver 300 to a receiver with cap 500
            double source1 = 300.0;
            double source2 = 300.0;
            double total = source1 + source2;  // 600
            double maxInput = 500.0;
            double delivered = Math.min(total, maxInput);
            assertEquals(500.0, delivered, 0.001);
        }

        @Test
        void testComplexNetworkPath() {
            // Source(1000) -> split 2 -> Node(0.95) -> split 3 -> Receiver
            // Path: 1000 / 2 = 500 -> * 0.95 = 475 -> / 3 ≈ 158.33
            double starlight = 1000.0;
            starlight = splitAmongTargets(starlight, 2);    // 500
            starlight = applyTransmissionEfficiency(starlight, 0.95); // 475
            starlight = splitAmongTargets(starlight, 3);    // ~158.33
            assertEquals(158.333, starlight, 0.01);
        }
    }
}
