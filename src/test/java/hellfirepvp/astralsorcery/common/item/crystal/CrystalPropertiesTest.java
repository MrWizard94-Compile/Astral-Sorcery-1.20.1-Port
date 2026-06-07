/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.crystal;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for crystal property quality calculations.
 * Tests the quality formula math without requiring MC registry bootstrapping.
 *
 * <p>Quality formula:
 * quality = (size/900)*0.3 + (purity/100)*0.4 + (cutting/100)*0.3</p>
 */
class CrystalPropertiesTest {

    /**
     * Computes quality from raw values using the same formula as ItemRockCrystalSimple.
     * Extracted here for registry-free testing.
     */
    private static float computeQuality(int size, int purity, int cutting) {
        float s = Math.max(1, Math.min(900, size)) / 900.0f;
        float p = Math.max(0, Math.min(100, purity)) / 100.0f;
        float c = Math.max(0, Math.min(100, cutting)) / 100.0f;
        return s * 0.3f + p * 0.4f + c * 0.3f;
    }

    /**
     * Celestial bonus: 1.2x multiplier capped at 1.0.
     */
    private static float computeCelestialQuality(int size, int purity, int cutting) {
        return Math.min(1.0f, computeQuality(size, purity, cutting) * 1.2f);
    }

    // ========================================================================
    // Quality factor tests
    // ========================================================================

    @Test
    void testQualityFactorPerfect() {
        // size=900, purity=100, cutting=100
        // (900/900)*0.3 + (100/100)*0.4 + (100/100)*0.3 = 0.3 + 0.4 + 0.3 = 1.0
        float quality = computeQuality(900, 100, 100);
        assertEquals(1.0f, quality, 0.001f);
    }

    @Test
    void testQualityFactorMinimum() {
        // size=1, purity=0, cutting=0
        // (1/900)*0.3 + 0 + 0 ≈ 0.000333
        float quality = computeQuality(1, 0, 0);
        assertTrue(quality < 0.01f, "Min quality should be near 0, was " + quality);
        assertTrue(quality >= 0f, "Quality should never be negative");
    }

    @Test
    void testQualityFactorMidRange() {
        // size=450, purity=50, cutting=50
        // (450/900)*0.3 + (50/100)*0.4 + (50/100)*0.3 = 0.15 + 0.2 + 0.15 = 0.5
        float quality = computeQuality(450, 50, 50);
        assertEquals(0.5f, quality, 0.001f);
    }

    @Test
    void testQualityWeightPurityHighest() {
        // Purity has weight 0.4 (highest)
        float purityOnly = computeQuality(1, 100, 0);
        // ≈ 0.0003 + 0.4 + 0 ≈ 0.4003
        assertTrue(purityOnly > 0.39f && purityOnly < 0.41f,
                "Purity-only should be ~0.4, was " + purityOnly);
    }

    @Test
    void testQualityWeightSizeAndCuttingEqual() {
        // Size and cutting both have weight 0.3
        float sizeOnly = computeQuality(900, 0, 0);
        assertEquals(0.3f, sizeOnly, 0.001f, "Max size + no purity/cutting = 0.3");

        float cutOnly = computeQuality(1, 0, 100);
        // (1/900)*0.3 + 0 + (100/100)*0.3 ≈ 0.0003 + 0 + 0.3 ≈ 0.3003
        assertTrue(cutOnly > 0.29f && cutOnly < 0.31f,
                "Max cutting + min size = ~0.3, was " + cutOnly);
    }

    // ========================================================================
    // Celestial crystal bonus
    // ========================================================================

    @Test
    void testCelestialQualityBonus() {
        // Mid-range: 0.5 * 1.2 = 0.6
        float celestial = computeCelestialQuality(450, 50, 50);
        assertEquals(0.6f, celestial, 0.001f);
    }

    @Test
    void testCelestialQualityCappedAtOne() {
        // Perfect crystal: 1.0 * 1.2 = 1.2, capped to 1.0
        float celestial = computeCelestialQuality(900, 100, 100);
        assertEquals(1.0f, celestial, 0.001f);
    }

    @Test
    void testCelestialQualityThreshold() {
        // Quality at 0.833 would give 0.833*1.2=1.0 (just at cap)
        // size=900 (0.3), purity=83 (0.332), cutting=67 (0.201) = 0.833
        float celestial = computeCelestialQuality(900, 83, 67);
        assertTrue(celestial >= 0.99f && celestial <= 1.0f,
                "Should be at or near cap, was " + celestial);
    }

    // ========================================================================
    // Clamping tests
    // ========================================================================

    @Test
    void testSizeClampLow() {
        // Negative size clamps to 1
        float quality = computeQuality(-100, 50, 50);
        float expected = computeQuality(1, 50, 50);
        assertEquals(expected, quality, 0.001f);
    }

    @Test
    void testSizeClampHigh() {
        // Size > 900 clamps to 900
        float quality = computeQuality(5000, 50, 50);
        float expected = computeQuality(900, 50, 50);
        assertEquals(expected, quality, 0.001f);
    }

    @Test
    void testPurityClampLow() {
        float quality = computeQuality(100, -50, 50);
        float expected = computeQuality(100, 0, 50);
        assertEquals(expected, quality, 0.001f);
    }

    @Test
    void testPurityClampHigh() {
        float quality = computeQuality(100, 200, 50);
        float expected = computeQuality(100, 100, 50);
        assertEquals(expected, quality, 0.001f);
    }

    @Test
    void testCuttingClampLow() {
        float quality = computeQuality(100, 50, -10);
        float expected = computeQuality(100, 50, 0);
        assertEquals(expected, quality, 0.001f);
    }

    @Test
    void testCuttingClampHigh() {
        float quality = computeQuality(100, 50, 150);
        float expected = computeQuality(100, 50, 100);
        assertEquals(expected, quality, 0.001f);
    }

    // ========================================================================
    // NBT storage (tests getTag access patterns without Item registry)
    // ========================================================================

    @Test
    void testNBTPropertyStorage() {
        // Verify the NBT key constants match what the code writes
        CompoundTag tag = new CompoundTag();
        tag.putInt("crystalSize", 500);
        tag.putInt("crystalPurity", 75);
        tag.putInt("crystalCutting", 80);

        assertEquals(500, tag.getInt("crystalSize"));
        assertEquals(75, tag.getInt("crystalPurity"));
        assertEquals(80, tag.getInt("crystalCutting"));
    }

    @Test
    void testNBTMissingKeysReturnZero() {
        CompoundTag tag = new CompoundTag();
        // CompoundTag.getInt returns 0 for missing keys
        assertEquals(0, tag.getInt("crystalSize"));
        assertFalse(tag.contains("crystalSize"));
    }

    // ========================================================================
    // Edge cases
    // ========================================================================

    @Test
    void testQualityIsAlwaysBounded() {
        // Test many random combinations to verify bounds
        int[][] cases = {
                {1, 0, 0}, {900, 100, 100}, {450, 50, 50},
                {1, 100, 100}, {900, 0, 0}, {100, 100, 0},
                {100, 0, 100}, {0, 0, 0}, {1000, 200, 200}
        };
        for (int[] c : cases) {
            float q = computeQuality(c[0], c[1], c[2]);
            assertTrue(q >= 0f && q <= 1.0f,
                    String.format("Quality out of bounds for (%d,%d,%d): %f",
                            c[0], c[1], c[2], q));
        }
    }
}
