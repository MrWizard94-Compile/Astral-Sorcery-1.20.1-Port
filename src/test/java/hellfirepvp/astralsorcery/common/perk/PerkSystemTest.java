/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
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
}
