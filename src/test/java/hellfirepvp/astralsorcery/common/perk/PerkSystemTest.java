/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the perk system's core data structures:
 * - ModifierType serialization
 * - PerkAttributeModifier serialization and application
 * - PerkAttributeType registry operations
 * - PerkLevelManager XP/level math
 * - PerkAttributeHelper modifier aggregation
 * - PerkTreePoint serialization
 */
class PerkSystemTest {

    @BeforeEach
    void clearRegistries() {
        AttributeTypeRegistry.clearForTesting();
        PerkTree.clearForTesting();
    }

    // ========================================================================
    // ModifierType
    // ========================================================================

    @Test
    void testModifierTypeFromString() {
        assertEquals(ModifierType.ADDITION, ModifierType.fromString("addition"));
        assertEquals(ModifierType.ADDED_MULTIPLY, ModifierType.fromString("added_multiply"));
        assertEquals(ModifierType.STACKING_MULTIPLY, ModifierType.fromString("stacking_multiply"));
        assertEquals(ModifierType.ADDITION, ModifierType.fromString("unknown_garbage"),
                "Unknown string should default to ADDITION");
    }

    @Test
    void testModifierTypeSerializedName() {
        assertEquals("addition", ModifierType.ADDITION.getSerializedName());
        assertEquals("added_multiply", ModifierType.ADDED_MULTIPLY.getSerializedName());
        assertEquals("stacking_multiply", ModifierType.STACKING_MULTIPLY.getSerializedName());
    }

    // ========================================================================
    // PerkAttributeModifier
    // ========================================================================

    @Test
    void testPerkModifierRoundtrip() {
        UUID id = UUID.randomUUID();
        ResourceLocation type = new ResourceLocation("astralsorcery", "perk.attr.armor");
        PerkAttributeModifier original = new PerkAttributeModifier(
                id, type, ModifierType.ADDITION, 5.0);

        CompoundTag tag = original.writeToNBT();
        PerkAttributeModifier loaded = PerkAttributeModifier.readFromNBT(tag);

        assertEquals(id, loaded.getId(), "UUID mismatch");
        assertEquals(type, loaded.getAttributeType(), "Attribute type mismatch");
        assertEquals(ModifierType.ADDITION, loaded.getModifierType(), "Modifier type mismatch");
        assertEquals(5.0, loaded.getValue(), 0.001, "Value mismatch");
    }

    @Test
    void testPerkModifierWithMultiplier() {
        PerkAttributeModifier original = new PerkAttributeModifier(
                UUID.randomUUID(),
                new ResourceLocation("astralsorcery", "perk.attr.damage"),
                ModifierType.ADDITION,
                10.0);

        PerkAttributeModifier scaled = original.withMultiplier(0.5);

        assertEquals(original.getId(), scaled.getId(), "Scaled modifier should keep same UUID");
        assertEquals(5.0, scaled.getValue(), 0.001, "10.0 * 0.5 = 5.0");
    }

    @Test
    void testPerkModifierEquality() {
        UUID id = UUID.randomUUID();
        ResourceLocation type = new ResourceLocation("astralsorcery", "perk.attr.armor");

        PerkAttributeModifier a = new PerkAttributeModifier(id, type, ModifierType.ADDITION, 5.0);
        PerkAttributeModifier b = new PerkAttributeModifier(id, type, ModifierType.ADDITION, 10.0);
        PerkAttributeModifier c = new PerkAttributeModifier(
                UUID.randomUUID(), type, ModifierType.ADDITION, 5.0);

        assertEquals(a, b, "Same UUID should be equal regardless of value");
        assertNotEquals(a, c, "Different UUIDs should not be equal");
    }

    // ========================================================================
    // AttributeTypeRegistry
    // ========================================================================

    @Test
    void testAttributeTypeRegistration() {
        ResourceLocation key = new ResourceLocation("astralsorcery", "test.attr");
        PerkAttributeType type = new PerkAttributeType(key);

        PerkAttributeType registered = AttributeTypeRegistry.register(type);
        assertSame(type, registered);
        assertTrue(AttributeTypeRegistry.isRegistered(key));
        assertEquals(1, AttributeTypeRegistry.size());
        assertSame(type, AttributeTypeRegistry.getType(key));
    }

    @Test
    void testAttributeTypeNotFound() {
        ResourceLocation key = new ResourceLocation("astralsorcery", "nonexistent");
        assertNull(AttributeTypeRegistry.getType(key));
        assertThrows(IllegalArgumentException.class,
                () -> AttributeTypeRegistry.getTypeOrThrow(key));
    }

    @Test
    void testDuplicateRegistration() {
        ResourceLocation key = new ResourceLocation("astralsorcery", "dup.attr");
        PerkAttributeType first = new PerkAttributeType(key);
        PerkAttributeType second = new PerkAttributeType(key);

        AttributeTypeRegistry.register(first);
        PerkAttributeType result = AttributeTypeRegistry.register(second);

        assertSame(first, result, "Duplicate should return the first registered instance");
        assertEquals(1, AttributeTypeRegistry.size());
    }

    // ========================================================================
    // PerkLevelManager
    // ========================================================================

    @Test
    void testLevelZeroExp() {
        assertEquals(0, PerkLevelManager.getLevelFromExp(0));
        assertEquals(0, PerkLevelManager.getTotalExpForLevel(0));
    }

    @Test
    void testLevelOneRequirements() {
        long expForLevel1 = PerkLevelManager.getExpForLevelUp(1);
        assertTrue(expForLevel1 > 0, "Level 1 should require positive exp");

        assertEquals(0, PerkLevelManager.getLevelFromExp(expForLevel1 - 1),
                "Just below level 1 should still be level 0");
        assertEquals(1, PerkLevelManager.getLevelFromExp(expForLevel1),
                "Exact level 1 exp should give level 1");
    }

    @Test
    void testLevelCap() {
        long hugeExp = Long.MAX_VALUE / 2;
        assertEquals(PerkLevelManager.MAX_LEVEL, PerkLevelManager.getLevelFromExp(hugeExp),
                "Should cap at MAX_LEVEL");
    }

    @Test
    void testPerkPointsForLevel() {
        assertEquals(1, PerkLevelManager.getPerkPointsForLevel(0),
                "Level 0 should have 1 starting point");
        assertEquals(2, PerkLevelManager.getPerkPointsForLevel(1),
                "Level 1 should have 2 points");
        assertEquals(41, PerkLevelManager.getPerkPointsForLevel(40),
                "Max level should have 41 points");
    }

    @Test
    void testLevelProgressBounds() {
        float progressAtZero = PerkLevelManager.getLevelProgress(0);
        assertEquals(0f, progressAtZero, 0.001f, "Progress at 0 exp should be 0");

        long maxExp = PerkLevelManager.getTotalExpForLevel(PerkLevelManager.MAX_LEVEL);
        float progressAtMax = PerkLevelManager.getLevelProgress(maxExp);
        assertEquals(1.0f, progressAtMax, 0.001f, "Progress at max should be 1.0");
    }

    @Test
    void testExpCurveIsMonotonicallyIncreasing() {
        long prev = 0;
        for (int i = 1; i <= PerkLevelManager.MAX_LEVEL; i++) {
            long needed = PerkLevelManager.getExpForLevelUp(i);
            assertTrue(needed > prev,
                    "Exp for level " + i + " (" + needed + ") should exceed level " + (i - 1) + " (" + prev + ")");
            prev = needed;
        }
    }

    // ========================================================================
    // PerkAttributeHelper — modifier aggregation
    // ========================================================================

    @Test
    void testApplyModifiersAdditionOnly() {
        List<PerkAttributeModifier> mods = List.of(
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.ADDITION, 3.0),
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.ADDITION, 7.0)
        );
        double result = PerkAttributeHelper.applyModifiers(10.0, mods);
        assertEquals(20.0, result, 0.001, "10 + 3 + 7 = 20");
    }

    @Test
    void testApplyModifiersAddedMultiply() {
        List<PerkAttributeModifier> mods = List.of(
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.ADDITION, 5.0),
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.ADDED_MULTIPLY, 0.20),
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.ADDED_MULTIPLY, 0.30)
        );
        // base=10, +5 = 15, * (1 + 0.20 + 0.30) = 15 * 1.50 = 22.5
        double result = PerkAttributeHelper.applyModifiers(10.0, mods);
        assertEquals(22.5, result, 0.001);
    }

    @Test
    void testApplyModifiersStackingMultiply() {
        List<PerkAttributeModifier> mods = List.of(
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.STACKING_MULTIPLY, 1.5),
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.STACKING_MULTIPLY, 2.0)
        );
        // base=10, * 1.5 = 15, * 2.0 = 30
        double result = PerkAttributeHelper.applyModifiers(10.0, mods);
        assertEquals(30.0, result, 0.001);
    }

    @Test
    void testApplyModifiersAllTypes() {
        List<PerkAttributeModifier> mods = List.of(
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.ADDITION, 10.0),
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.ADDED_MULTIPLY, 0.5),
                new PerkAttributeModifier(
                        new ResourceLocation("test", "a"), ModifierType.STACKING_MULTIPLY, 2.0)
        );
        // base=100, +10 = 110, * (1 + 0.5) = 165, * 2.0 = 330
        double result = PerkAttributeHelper.applyModifiers(100.0, mods);
        assertEquals(330.0, result, 0.001);
    }

    @Test
    void testApplyModifiersEmpty() {
        double result = PerkAttributeHelper.applyModifiers(42.0, List.of());
        assertEquals(42.0, result, 0.001, "No modifiers should return base value");
    }

    // ========================================================================
    // PerkTreePoint
    // ========================================================================

    @Test
    void testPerkTreePointRoundtrip() {
        ResourceLocation key = new ResourceLocation("astralsorcery", "test_perk");
        PerkTreePoint original = new PerkTreePoint(key, 15, -20);

        CompoundTag tag = original.writeToNBT();
        PerkTreePoint loaded = PerkTreePoint.readFromNBT(tag);

        assertEquals(key, loaded.getPerkKey());
        assertEquals(15, loaded.getOffsetX());
        assertEquals(-20, loaded.getOffsetY());
    }

    @Test
    void testPerkTreePointDistance() {
        PerkTreePoint a = new PerkTreePoint(
                new ResourceLocation("test", "a"), 0, 0);
        PerkTreePoint b = new PerkTreePoint(
                new ResourceLocation("test", "b"), 3, 4);

        assertEquals(25.0, a.distanceSqTo(b), 0.001, "3^2 + 4^2 = 25");
    }

    // ========================================================================
    // PerkTree allocation/deallocation logic
    // ========================================================================

    /**
     * Concrete test implementation of AbstractPerk for unit testing.
     */
    private static class TestPerk extends AbstractPerk {
        private int allocateCount = 0;
        private int deallocateCount = 0;

        TestPerk(@Nonnull ResourceLocation key, int x, int y, @Nonnull PerkCategory category) {
            super(key, x, y, category);
        }

        @Override
        public void onAllocate(@Nonnull Player player) {
            allocateCount++;
        }

        @Override
        public void onDeallocate(@Nonnull Player player) {
            deallocateCount++;
        }

        int getAllocateCount() { return allocateCount; }
        int getDeallocateCount() { return deallocateCount; }
    }

    /**
     * Creates a mock-free PlayerProgress pre-configured for testing.
     */
    private PlayerProgress createTestProgress(@Nonnull ProgressionTier tier, long perkExp) {
        PlayerProgress progress = new PlayerProgress();
        progress.setTierReached(tier);
        progress.setPerkExp(perkExp);
        return progress;
    }

    @Nested
    class AllocationTests {

        private static final ResourceLocation ROOT_KEY =
                new ResourceLocation("astralsorcery", "root.discidia");
        private static final ResourceLocation NODE_A =
                new ResourceLocation("astralsorcery", "node.a");
        private static final ResourceLocation NODE_B =
                new ResourceLocation("astralsorcery", "node.b");
        private static final ResourceLocation NODE_C =
                new ResourceLocation("astralsorcery", "node.c");
        private static final ResourceLocation ISOLATED =
                new ResourceLocation("astralsorcery", "node.isolated");

        @BeforeEach
        void buildTestTree() {
            PerkTree.clearForTesting();

            // Build a simple tree: ROOT -> A -> B -> C, and ISOLATED (no connections)
            //   ROOT
            //    |
            //    A
            //   / \
            //  B   C
            TestPerk root = new TestPerk(ROOT_KEY, 0, 0, AbstractPerk.PerkCategory.ROOT);
            TestPerk nodeA = new TestPerk(NODE_A, 0, -10, AbstractPerk.PerkCategory.SMALL);
            TestPerk nodeB = new TestPerk(NODE_B, -5, -20, AbstractPerk.PerkCategory.SMALL);
            TestPerk nodeC = new TestPerk(NODE_C, 5, -20, AbstractPerk.PerkCategory.MAJOR);
            TestPerk isolated = new TestPerk(ISOLATED, 50, 50, AbstractPerk.PerkCategory.SMALL);

            PerkTree.register(root);
            PerkTree.register(nodeA);
            PerkTree.register(nodeB);
            PerkTree.register(nodeC);
            PerkTree.register(isolated);

            PerkTree.connect(ROOT_KEY, NODE_A);
            PerkTree.connect(NODE_A, NODE_B);
            PerkTree.connect(NODE_A, NODE_C);
            // ISOLATED has no connections
        }

        @Test
        void testAllocateRootPerk() {
            // Level 1 gives enough exp for at least 1 perk point
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(5));

            AllocationStatus status = PerkTree.allocate(null, progress, ROOT_KEY);
            assertEquals(AllocationStatus.SUCCESS, status);
            assertTrue(progress.hasPerkAllocated(ROOT_KEY));
        }

        @Test
        void testAllocateRequiresAttunementTier() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.DISCOVERY,
                    PerkLevelManager.getTotalExpForLevel(5));

            AllocationStatus status = PerkTree.allocate(null, progress, ROOT_KEY);
            assertEquals(AllocationStatus.INSUFFICIENT_TIER, status);
            assertFalse(progress.hasPerkAllocated(ROOT_KEY));
        }

        @Test
        void testAllocateAdjacentPerk() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY); // Pre-allocate root

            AllocationStatus status = PerkTree.allocate(null, progress, NODE_A);
            assertEquals(AllocationStatus.SUCCESS, status);
            assertTrue(progress.hasPerkAllocated(NODE_A));
        }

        @Test
        void testAllocateNonAdjacentFails() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);

            // NODE_B is not adjacent to ROOT — it's adjacent to NODE_A
            AllocationStatus status = PerkTree.allocate(null, progress, NODE_B);
            assertEquals(AllocationStatus.NOT_REACHABLE, status);
            assertFalse(progress.hasPerkAllocated(NODE_B));
        }

        @Test
        void testAllocateIsolatedFails() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);

            AllocationStatus status = PerkTree.allocate(null, progress, ISOLATED);
            assertEquals(AllocationStatus.NOT_REACHABLE, status);
        }

        @Test
        void testAllocateAlreadyAllocated() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);

            AllocationStatus status = PerkTree.allocate(null, progress, ROOT_KEY);
            assertEquals(AllocationStatus.ALREADY_ALLOCATED, status);
        }

        @Test
        void testAllocateInsufficientPoints() {
            // Level 0 gives 1 point
            PlayerProgress progress = createTestProgress(ProgressionTier.ATTUNEMENT, 0);
            progress.allocatePerk(ROOT_KEY); // Spent the 1 point

            AllocationStatus status = PerkTree.allocate(null, progress, NODE_A);
            assertEquals(AllocationStatus.INSUFFICIENT_POINTS, status);
        }

        @Test
        void testAllocateDisabledPerkFails() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));

            AbstractPerk perk = PerkTree.getPerk(ROOT_KEY);
            assertNotNull(perk);
            perk.setEnabled(false);

            AllocationStatus status = PerkTree.allocate(null, progress, ROOT_KEY);
            assertEquals(AllocationStatus.FAILED, status);

            perk.setEnabled(true); // Reset
        }

        @Test
        void testAllocateConstellationRequirement() {
            ResourceLocation requiredConst = new ResourceLocation("astralsorcery", "discidia");
            AbstractPerk perk = PerkTree.getPerk(NODE_A);
            assertNotNull(perk);
            perk.setRequiredConstellation(requiredConst);

            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);

            // Wrong constellation
            progress.setAttunedConstellation(
                    new ResourceLocation("astralsorcery", "aevitas"));
            AllocationStatus status = PerkTree.allocate(null, progress, NODE_A);
            assertEquals(AllocationStatus.WRONG_CONSTELLATION, status);

            // Correct constellation
            progress.setAttunedConstellation(requiredConst);
            status = PerkTree.allocate(null, progress, NODE_A);
            assertEquals(AllocationStatus.SUCCESS, status);

            // Cleanup
            perk.setRequiredConstellation(null);
        }

        @Test
        void testAllocateUnregisteredPerkFails() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));

            AllocationStatus status = PerkTree.allocate(null, progress,
                    new ResourceLocation("astralsorcery", "nonexistent"));
            assertEquals(AllocationStatus.FAILED, status);
        }
    }

    @Nested
    class DeallocationTests {

        private static final ResourceLocation ROOT_KEY =
                new ResourceLocation("astralsorcery", "root.discidia");
        private static final ResourceLocation NODE_A =
                new ResourceLocation("astralsorcery", "node.a");
        private static final ResourceLocation NODE_B =
                new ResourceLocation("astralsorcery", "node.b");
        private static final ResourceLocation NODE_C =
                new ResourceLocation("astralsorcery", "node.c");

        @BeforeEach
        void buildTestTree() {
            PerkTree.clearForTesting();

            // ROOT -> A -> B
            //           -> C
            TestPerk root = new TestPerk(ROOT_KEY, 0, 0, AbstractPerk.PerkCategory.ROOT);
            TestPerk nodeA = new TestPerk(NODE_A, 0, -10, AbstractPerk.PerkCategory.SMALL);
            TestPerk nodeB = new TestPerk(NODE_B, -5, -20, AbstractPerk.PerkCategory.SMALL);
            TestPerk nodeC = new TestPerk(NODE_C, 5, -20, AbstractPerk.PerkCategory.SMALL);

            PerkTree.register(root);
            PerkTree.register(nodeA);
            PerkTree.register(nodeB);
            PerkTree.register(nodeC);

            PerkTree.connect(ROOT_KEY, NODE_A);
            PerkTree.connect(NODE_A, NODE_B);
            PerkTree.connect(NODE_A, NODE_C);
        }

        @Test
        void testDeallocateLeafPerk() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);
            progress.allocatePerk(NODE_A);
            progress.allocatePerk(NODE_B);

            // NODE_B is a leaf — can be safely removed
            AllocationStatus status = PerkTree.deallocate(null, progress, NODE_B);
            assertEquals(AllocationStatus.SUCCESS, status);
            assertFalse(progress.hasPerkAllocated(NODE_B));
        }

        @Test
        void testDeallocateNotAllocated() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));

            AllocationStatus status = PerkTree.deallocate(null, progress, NODE_A);
            assertEquals(AllocationStatus.NOT_ALLOCATED, status);
        }

        @Test
        void testDeallocateWouldDisconnect() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);
            progress.allocatePerk(NODE_A);
            progress.allocatePerk(NODE_B);

            // NODE_A connects ROOT to NODE_B — removing it would disconnect B
            AllocationStatus status = PerkTree.deallocate(null, progress, NODE_A);
            assertEquals(AllocationStatus.WOULD_DISCONNECT, status);
            assertTrue(progress.hasPerkAllocated(NODE_A), "Perk should still be allocated");
        }

        @Test
        void testDeallocateRootWithDependents() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);
            progress.allocatePerk(NODE_A);

            // ROOT has NODE_A connected and allocated — cannot remove
            AllocationStatus status = PerkTree.deallocate(null, progress, ROOT_KEY);
            assertEquals(AllocationStatus.WOULD_DISCONNECT, status);
        }

        @Test
        void testDeallocateRootAlone() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);

            // ROOT is the only perk — safe to remove
            AllocationStatus status = PerkTree.deallocate(null, progress, ROOT_KEY);
            assertEquals(AllocationStatus.SUCCESS, status);
            assertFalse(progress.hasPerkAllocated(ROOT_KEY));
        }

        @Test
        void testDeallocateBranchPreservesOtherBranch() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));
            progress.allocatePerk(ROOT_KEY);
            progress.allocatePerk(NODE_A);
            progress.allocatePerk(NODE_B);
            progress.allocatePerk(NODE_C);

            // Both B and C are leaves — can remove either
            AllocationStatus statusB = PerkTree.deallocate(null, progress, NODE_B);
            assertEquals(AllocationStatus.SUCCESS, statusB);
            assertFalse(progress.hasPerkAllocated(NODE_B));
            assertTrue(progress.hasPerkAllocated(NODE_C), "C should remain");

            AllocationStatus statusC = PerkTree.deallocate(null, progress, NODE_C);
            assertEquals(AllocationStatus.SUCCESS, statusC);
            assertFalse(progress.hasPerkAllocated(NODE_C));
        }

        @Test
        void testDeallocateUnregisteredPerkFails() {
            PlayerProgress progress = createTestProgress(
                    ProgressionTier.ATTUNEMENT,
                    PerkLevelManager.getTotalExpForLevel(10));

            AllocationStatus status = PerkTree.deallocate(null, progress,
                    new ResourceLocation("astralsorcery", "fake_perk"));
            assertEquals(AllocationStatus.FAILED, status);
        }
    }

    @Nested
    class ConnectivityTests {

        @BeforeEach
        void resetTree() {
            PerkTree.clearForTesting();
        }

        @Test
        void testGetConnectionsDeduplicated() {
            ResourceLocation rootKey = new ResourceLocation("test", "root");
            ResourceLocation nodeA = new ResourceLocation("test", "a");
            ResourceLocation nodeB = new ResourceLocation("test", "b");

            PerkTree.register(new TestPerk(rootKey, 0, 0, AbstractPerk.PerkCategory.ROOT));
            PerkTree.register(new TestPerk(nodeA, 5, 0, AbstractPerk.PerkCategory.SMALL));
            PerkTree.register(new TestPerk(nodeB, 10, 0, AbstractPerk.PerkCategory.SMALL));

            PerkTree.connect(rootKey, nodeA);
            PerkTree.connect(nodeA, nodeB);

            List<PerkTree.Connection> connections = PerkTree.getConnections();
            assertEquals(2, connections.size(),
                    "Should have exactly 2 unique edges (root-a, a-b)");
        }

        @Test
        void testGetPoints() {
            ResourceLocation rootKey = new ResourceLocation("test", "root");
            ResourceLocation nodeA = new ResourceLocation("test", "a");

            PerkTree.register(new TestPerk(rootKey, 0, 0, AbstractPerk.PerkCategory.ROOT));
            PerkTree.register(new TestPerk(nodeA, 10, -5, AbstractPerk.PerkCategory.SMALL));

            List<PerkTreePoint> points = PerkTree.getPoints();
            assertEquals(2, points.size());
        }

        @Test
        void testGetAdjacentKeys() {
            ResourceLocation rootKey = new ResourceLocation("test", "root");
            ResourceLocation nodeA = new ResourceLocation("test", "a");
            ResourceLocation nodeB = new ResourceLocation("test", "b");

            PerkTree.register(new TestPerk(rootKey, 0, 0, AbstractPerk.PerkCategory.ROOT));
            PerkTree.register(new TestPerk(nodeA, 5, 0, AbstractPerk.PerkCategory.SMALL));
            PerkTree.register(new TestPerk(nodeB, 10, 0, AbstractPerk.PerkCategory.SMALL));

            PerkTree.connect(rootKey, nodeA);
            PerkTree.connect(rootKey, nodeB);

            Set<ResourceLocation> adj = PerkTree.getAdjacentKeys(rootKey);
            assertEquals(2, adj.size());
            assertTrue(adj.contains(nodeA));
            assertTrue(adj.contains(nodeB));
        }

        @Test
        void testAdjacentKeysForUnregisteredReturnsEmpty() {
            Set<ResourceLocation> adj = PerkTree.getAdjacentKeys(
                    new ResourceLocation("test", "nobody"));
            assertTrue(adj.isEmpty());
        }

        @Test
        void testSizeAndRegistration() {
            assertEquals(0, PerkTree.size());

            ResourceLocation key = new ResourceLocation("test", "p1");
            PerkTree.register(new TestPerk(key, 0, 0, AbstractPerk.PerkCategory.SMALL));

            assertEquals(1, PerkTree.size());
            assertTrue(PerkTree.isRegistered(key));
            assertNotNull(PerkTree.getPerk(key));
        }

        @Test
        void testDuplicateRegistrationIgnored() {
            ResourceLocation key = new ResourceLocation("test", "dup");
            PerkTree.register(new TestPerk(key, 0, 0, AbstractPerk.PerkCategory.SMALL));
            PerkTree.register(new TestPerk(key, 5, 5, AbstractPerk.PerkCategory.MAJOR));

            assertEquals(1, PerkTree.size());
            // First registration wins
            assertEquals(0, PerkTree.getPerk(key).getX());
        }
    }
}
