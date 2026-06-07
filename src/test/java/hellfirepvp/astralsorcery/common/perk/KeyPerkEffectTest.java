/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVorux;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for key perk special effects, seal mechanics,
 * and perk tree data building.
 */
class KeyPerkEffectTest {

    @BeforeEach
    void clearAll() {
        AttributeTypeRegistry.clearForTesting();
        PerkTree.clearForTesting();
    }

    // ========================================================================
    // Vorux low-health bonus calculation (pure math, no Player needed)
    // ========================================================================

    @Test
    void testVoruxBonusAtFullHealth() {
        // At full health (fraction = 1.0), missing = 0.0, bonus = 0.0
        float bonus = computeVoruxBonus(1.0f);
        assertEquals(0.0f, bonus, 0.001f, "Full health should give zero bonus");
    }

    @Test
    void testVoruxBonusAtHalfHealth() {
        // At half health (fraction = 0.5), missing = 0.5, bonus = 0.5 * 0.50 = 0.25
        float bonus = computeVoruxBonus(0.5f);
        assertEquals(0.25f, bonus, 0.001f, "Half health should give 25% bonus");
    }

    @Test
    void testVoruxBonusAtOneHP() {
        // Near zero health (fraction ≈ 0.05 for 1/20 HP), missing ≈ 0.95
        float bonus = computeVoruxBonus(0.05f);
        float expected = 0.95f * KeyVorux.MAX_LOW_HEALTH_BONUS;
        assertEquals(expected, bonus, 0.01f, "Near-death should give ~47.5% bonus");
    }

    @Test
    void testVoruxMaxBonus() {
        // At exactly 0 health fraction (theoretical)
        float bonus = computeVoruxBonus(0.0f);
        assertEquals(KeyVorux.MAX_LOW_HEALTH_BONUS, bonus, 0.001f,
                "Zero health should give max bonus");
    }

    /**
     * Computes the Vorux bonus from a health fraction without needing a Player instance.
     * Mirrors the logic in {@link KeyVorux#getLowHealthBonus}.
     */
    private float computeVoruxBonus(float healthFraction) {
        float missingFraction = 1.0f - healthFraction;
        return missingFraction * KeyVorux.MAX_LOW_HEALTH_BONUS;
    }

    // ========================================================================
    // Perk seal mechanics (PlayerProgress-level)
    // ========================================================================

    @Nested
    class SealTests {

        @Test
        void testSealPerk() {
            PlayerProgress progress = new PlayerProgress();
            ResourceLocation perkKey = new ResourceLocation("astralsorcery", "test_perk");
            progress.allocatePerk(perkKey);

            assertFalse(progress.isPerkSealed(perkKey), "Should not be sealed initially");

            progress.sealPerk(perkKey);
            assertTrue(progress.isPerkSealed(perkKey), "Should be sealed after sealPerk()");
            assertTrue(progress.hasPerkAllocated(perkKey),
                    "Sealed perk should still be counted as allocated");
        }

        @Test
        void testUnsealPerk() {
            PlayerProgress progress = new PlayerProgress();
            ResourceLocation perkKey = new ResourceLocation("astralsorcery", "test_perk");
            progress.allocatePerk(perkKey);
            progress.sealPerk(perkKey);

            assertTrue(progress.isPerkSealed(perkKey));

            progress.unsealPerk(perkKey);
            assertFalse(progress.isPerkSealed(perkKey), "Should be unsealed after unsealPerk()");
            assertTrue(progress.hasPerkAllocated(perkKey), "Should still be allocated");
        }

        @Test
        void testSealPersistsThroughSerialization() {
            PlayerProgress original = new PlayerProgress();
            ResourceLocation perkA = new ResourceLocation("astralsorcery", "perk_a");
            ResourceLocation perkB = new ResourceLocation("astralsorcery", "perk_b");
            original.allocatePerk(perkA);
            original.allocatePerk(perkB);
            original.sealPerk(perkA);

            CompoundTag nbt = original.writeToNBT();
            PlayerProgress loaded = new PlayerProgress();
            loaded.readFromNBT(nbt);

            assertTrue(loaded.hasPerkAllocated(perkA), "Perk A should be allocated");
            assertTrue(loaded.hasPerkAllocated(perkB), "Perk B should be allocated");
            assertTrue(loaded.isPerkSealed(perkA), "Perk A should be sealed");
            assertFalse(loaded.isPerkSealed(perkB), "Perk B should not be sealed");
        }

        @Test
        void testSealNonAllocatedPerkStillTracked() {
            PlayerProgress progress = new PlayerProgress();
            ResourceLocation perkKey = new ResourceLocation("astralsorcery", "not_allocated");

            // sealPerk(ResourceLocation) does not check allocation — it stores the key.
            // The actual allocation check happens at the packet handler level.
            progress.sealPerk(perkKey);
            assertTrue(progress.isPerkSealed(perkKey),
                    "sealPerk should store the key regardless of allocation state");
        }
    }

    // ========================================================================
    // PerkTreeData building verification
    // ========================================================================

    @Nested
    class TreeDataTests {

        @Test
        void testBuildTreeRegistersAllPerks() {
            // Need attribute types registered for PerkTreeData
            initMinimalAttributes();

            hellfirepvp.astralsorcery.common.perk.PerkTreeData.buildTree();

            // Should have at least 50 perks (5 roots + branches + bridges + keys + gems + focuses)
            assertTrue(PerkTree.size() >= 50,
                    "Tree should have at least 50 perks, got " + PerkTree.size());
        }

        @Test
        void testBuildTreeHasFiveRoots() {
            initMinimalAttributes();
            PerkTreeData.buildTree();

            Set<ResourceLocation> roots = PerkTree.getRootPerkKeys();
            assertEquals(5, roots.size(), "Should have exactly 5 root perks");
        }

        @Test
        void testBuildTreeHasConnections() {
            initMinimalAttributes();
            PerkTreeData.buildTree();

            var connections = PerkTree.getConnections();
            assertTrue(connections.size() > 40,
                    "Tree should have > 40 connections, got " + connections.size());
        }

        @Test
        void testBuildTreeAllRootsConnected() {
            initMinimalAttributes();
            PerkTreeData.buildTree();

            // Each root should be adjacent to at least one other perk
            for (ResourceLocation root : PerkTree.getRootPerkKeys()) {
                Set<ResourceLocation> adj = PerkTree.getAdjacentKeys(root);
                assertFalse(adj.isEmpty(),
                        "Root " + root + " should have at least one connection");
            }
        }

        @Test
        void testBuildTreeKeyPerksExist() {
            initMinimalAttributes();
            PerkTreeData.buildTree();

            // Verify major key perks are registered
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_discidia")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_armara")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_vicio")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_aevitas")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_evorsio")));

            // Outer constellation keys
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_lucerna")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_mineralis")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_horologium")));

            // New minor constellation keys
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_pelotrio")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_vorux")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_alcara")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_gelu")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "key_ulteria")));
        }

        @Test
        void testBuildTreeGemSocketPerksExist() {
            initMinimalAttributes();
            PerkTreeData.buildTree();

            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "gem_discidia")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "gem_armara")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "gem_vicio")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "gem_aevitas")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "gem_evorsio")));
            assertNotNull(PerkTree.getPerk(new ResourceLocation("astralsorcery", "gem_central")));
        }

        /**
         * Registers the minimal attribute types needed by PerkTreeData.
         */
        private void initMinimalAttributes() {
            hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS.init();
        }
    }
}
