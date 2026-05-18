/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.effect;

import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PerkAttributeHelper modifier computation system.
 * Tests the core mathematical model: ADDITION → ADDED_MULTIPLY → STACKING_MULTIPLY.
 * Also tests PerkAttributeModifier serialization and the apply() method.
 *
 * <p>These tests verify the perk system's value-computation contract
 * without requiring Minecraft entity/player instances.</p>
 */
class PerkAttributeHelperTest {

    private static final ResourceLocation ATTR_ATTACK_DAMAGE =
            new ResourceLocation("astralsorcery", "attr_type_attack_damage");
    private static final ResourceLocation ATTR_MOVEMENT_SPEED =
            new ResourceLocation("astralsorcery", "attr_type_movement_speed");
    private static final ResourceLocation ATTR_ARMOR =
            new ResourceLocation("astralsorcery", "attr_type_armor");

    // ========================================================================
    // applyModifiers() tests — the core stacking formula
    // ========================================================================

    @Nested
    class ApplyModifiersTests {

        @Test
        void testEmptyModifierList() {
            double result = PerkAttributeHelper.applyModifiers(10.0, List.of());
            assertEquals(10.0, result, 0.001, "No modifiers → base value unchanged");
        }

        @Test
        void testSingleAddition() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 2.0)
            );
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(12.0, result, 0.001, "Base 10 + 2 = 12");
        }

        @Test
        void testMultipleAdditions() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 1.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 3.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 0.5)
            );
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(14.5, result, 0.001, "Base 10 + (1+3+0.5) = 14.5");
        }

        @Test
        void testNegativeAddition() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ARMOR, ModifierType.ADDITION, -2.0)
            );
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(8.0, result, 0.001, "Base 10 - 2 = 8");
        }

        @Test
        void testSingleAddedMultiply() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_MOVEMENT_SPEED, ModifierType.ADDED_MULTIPLY, 0.15)
            );
            // base * (1 + 0.15) = 10 * 1.15 = 11.5
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(11.5, result, 0.001, "Base 10 * (1+0.15) = 11.5");
        }

        @Test
        void testMultipleAddedMultiply() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_MOVEMENT_SPEED, ModifierType.ADDED_MULTIPLY, 0.10),
                    new PerkAttributeModifier(ATTR_MOVEMENT_SPEED, ModifierType.ADDED_MULTIPLY, 0.20)
            );
            // base * (1 + 0.10 + 0.20) = 10 * 1.30 = 13.0
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(13.0, result, 0.001, "Base 10 * (1+0.1+0.2) = 13.0");
        }

        @Test
        void testSingleStackingMultiply() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.15)
            );
            // base * 1.15 = 10 * 1.15 = 11.5
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(11.5, result, 0.001, "Base 10 * 1.15 = 11.5");
        }

        @Test
        void testMultipleStackingMultiply() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.10),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.20)
            );
            // base * 1.10 * 1.20 = 10 * 1.32 = 13.2
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(13.2, result, 0.001, "Base 10 * 1.10 * 1.20 = 13.2");
        }

        @Test
        void testFullStackingOrder() {
            // The core formula: (base + additions) * (1 + addedMultiplies) * stacking1 * stacking2
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 2.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDED_MULTIPLY, 0.10),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.15)
            );
            // Step 1: 10 + 2 = 12
            // Step 2: 12 * (1 + 0.10) = 12 * 1.10 = 13.2
            // Step 3: 13.2 * 1.15 = 15.18
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(15.18, result, 0.001, "Full stack: (10+2)*(1+0.1)*1.15 = 15.18");
        }

        @Test
        void testComplexMultiModifier() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 1.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 3.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDED_MULTIPLY, 0.05),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDED_MULTIPLY, 0.15),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.10),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.05)
            );
            // Step 1: 10 + 1 + 3 = 14
            // Step 2: 14 * (1 + 0.05 + 0.15) = 14 * 1.20 = 16.8
            // Step 3: 16.8 * 1.10 * 1.05 = 16.8 * 1.155 = 19.404
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(19.404, result, 0.001, "Complex: (10+4)*1.2*1.10*1.05 = 19.404");
        }

        @Test
        void testZeroBase() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 5.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 2.0)
            );
            // (0 + 5) * (1 + 0) * 2.0 = 10.0
            double result = PerkAttributeHelper.applyModifiers(0.0, mods);
            assertEquals(10.0, result, 0.001, "Zero base with addition + multiply");
        }

        @Test
        void testNegativeMultiplier() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ARMOR, ModifierType.ADDED_MULTIPLY, -0.25)
            );
            // 10 * (1 - 0.25) = 10 * 0.75 = 7.5
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(7.5, result, 0.001, "Negative added multiply: 10*(1-0.25)=7.5");
        }

        @Test
        void testStackingBelowOne() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ARMOR, ModifierType.STACKING_MULTIPLY, 0.9)
            );
            // 10 * 0.9 = 9.0
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(9.0, result, 0.001, "Stacking below 1: 10*0.9=9.0");
        }

        @Test
        void testDiminishingReturns() {
            // Multiple small multipliers: 1.05^5 ≈ 1.276
            List<PerkAttributeModifier> mods = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                mods.add(new PerkAttributeModifier(ATTR_ATTACK_DAMAGE,
                        ModifierType.STACKING_MULTIPLY, 1.05));
            }
            double expected = 10.0 * Math.pow(1.05, 5);
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(expected, result, 0.001, "5x 1.05 stacking multipliers");
        }
    }

    // ========================================================================
    // PerkAttributeModifier serialization tests
    // ========================================================================

    @Nested
    class ModifierSerializationTests {

        @Test
        void testRoundtripAddition() {
            UUID id = UUID.randomUUID();
            PerkAttributeModifier original = new PerkAttributeModifier(
                    id, ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 2.5);

            CompoundTag tag = original.writeToNBT();
            PerkAttributeModifier loaded = PerkAttributeModifier.readFromNBT(tag);

            assertEquals(id, loaded.getId());
            assertEquals(ATTR_ATTACK_DAMAGE, loaded.getAttributeType());
            assertEquals(ModifierType.ADDITION, loaded.getModifierType());
            assertEquals(2.5, loaded.getValue(), 0.001);
        }

        @Test
        void testRoundtripMultiply() {
            UUID id = UUID.randomUUID();
            PerkAttributeModifier original = new PerkAttributeModifier(
                    id, ATTR_MOVEMENT_SPEED, ModifierType.STACKING_MULTIPLY, 1.15);

            CompoundTag tag = original.writeToNBT();
            PerkAttributeModifier loaded = PerkAttributeModifier.readFromNBT(tag);

            assertEquals(id, loaded.getId());
            assertEquals(ATTR_MOVEMENT_SPEED, loaded.getAttributeType());
            assertEquals(ModifierType.STACKING_MULTIPLY, loaded.getModifierType());
            assertEquals(1.15, loaded.getValue(), 0.001);
        }

        @Test
        void testRoundtripNegativeValue() {
            PerkAttributeModifier original = new PerkAttributeModifier(
                    ATTR_ARMOR, ModifierType.ADDITION, -3.0);

            CompoundTag tag = original.writeToNBT();
            PerkAttributeModifier loaded = PerkAttributeModifier.readFromNBT(tag);

            assertEquals(ModifierType.ADDITION, loaded.getModifierType());
            assertEquals(-3.0, loaded.getValue(), 0.001);
        }

        @Test
        void testWithMultiplier() {
            UUID id = UUID.randomUUID();
            PerkAttributeModifier original = new PerkAttributeModifier(
                    id, ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 2.0);

            PerkAttributeModifier scaled = original.withMultiplier(1.5);
            assertEquals(id, scaled.getId(), "UUID preserved after scaling");
            assertEquals(3.0, scaled.getValue(), 0.001, "Value scaled by 1.5");
            assertEquals(ModifierType.ADDITION, scaled.getModifierType());
        }

        @Test
        void testEquality() {
            UUID id = UUID.randomUUID();
            PerkAttributeModifier a = new PerkAttributeModifier(
                    id, ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 1.0);
            PerkAttributeModifier b = new PerkAttributeModifier(
                    id, ATTR_MOVEMENT_SPEED, ModifierType.STACKING_MULTIPLY, 99.0);

            assertEquals(a, b, "Modifiers with same UUID are equal regardless of other fields");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void testInequality() {
            PerkAttributeModifier a = new PerkAttributeModifier(
                    UUID.randomUUID(), ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 1.0);
            PerkAttributeModifier b = new PerkAttributeModifier(
                    UUID.randomUUID(), ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 1.0);

            assertNotEquals(a, b, "Modifiers with different UUIDs are not equal");
        }
    }

    // ========================================================================
    // ModifierType enum tests
    // ========================================================================

    @Nested
    class ModifierTypeTests {

        @Test
        void testFromString() {
            assertEquals(ModifierType.ADDITION, ModifierType.fromString("addition"));
            assertEquals(ModifierType.ADDED_MULTIPLY, ModifierType.fromString("added_multiply"));
            assertEquals(ModifierType.STACKING_MULTIPLY, ModifierType.fromString("stacking_multiply"));
        }

        @Test
        void testFromStringInvalid() {
            assertEquals(ModifierType.ADDITION, ModifierType.fromString("invalid"),
                    "Unknown strings should default to ADDITION");
        }

        @Test
        void testGetSerializedName() {
            assertEquals("addition", ModifierType.ADDITION.getSerializedName());
            assertEquals("added_multiply", ModifierType.ADDED_MULTIPLY.getSerializedName());
            assertEquals("stacking_multiply", ModifierType.STACKING_MULTIPLY.getSerializedName());
        }

        @Test
        void testRoundtripAllTypes() {
            for (ModifierType type : ModifierType.values()) {
                assertEquals(type, ModifierType.fromString(type.getSerializedName()),
                        "Roundtrip failed for " + type);
            }
        }
    }

    // ========================================================================
    // Edge case tests
    // ========================================================================

    @Nested
    class EdgeCaseTests {

        @Test
        void testVeryLargeModifierCount() {
            List<PerkAttributeModifier> mods = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                mods.add(new PerkAttributeModifier(
                        ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 0.1));
            }
            // 10 + (100 * 0.1) = 10 + 10 = 20
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(20.0, result, 0.01, "100 small additions sum correctly");
        }

        @Test
        void testZeroValueModifiers() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 0.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.ADDED_MULTIPLY, 0.0),
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.0)
            );
            // (10 + 0) * (1 + 0) * 1.0 = 10
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(10.0, result, 0.001, "Zero-effect modifiers don't change value");
        }

        @Test
        void testStackingMultiplyByZero() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 0.0)
            );
            double result = PerkAttributeHelper.applyModifiers(100.0, mods);
            assertEquals(0.0, result, 0.001, "Multiply by zero zeroes everything");
        }

        @Test
        void testAddedMultiplyNegativeOne() {
            List<PerkAttributeModifier> mods = List.of(
                    new PerkAttributeModifier(ATTR_ARMOR, ModifierType.ADDED_MULTIPLY, -1.0)
            );
            // 10 * (1 - 1) = 10 * 0 = 0
            double result = PerkAttributeHelper.applyModifiers(10.0, mods);
            assertEquals(0.0, result, 0.001, "-100% added multiply zeroes the value");
        }

        @Test
        void testModifierApplyMethod() {
            PerkAttributeModifier addMod = new PerkAttributeModifier(
                    ATTR_ATTACK_DAMAGE, ModifierType.ADDITION, 5.0);
            assertEquals(5.0, addMod.apply(10.0), 0.001,
                    "ADDITION apply returns raw value");

            PerkAttributeModifier multMod = new PerkAttributeModifier(
                    ATTR_ATTACK_DAMAGE, ModifierType.STACKING_MULTIPLY, 1.5);
            assertEquals(15.0, multMod.apply(10.0), 0.001,
                    "STACKING_MULTIPLY apply returns current * value");
        }
    }
}
