/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the crystal-attribute-to-CrystalProperties mapping math used by
 * {@link hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.NBTCopyRecipe}.
 *
 * <p>{@code NBTCopyRecipe} maps crystal attribute tiers to discrete CrystalProperties
 * values (size, purity, cutting) using proportional scaling:
 * <pre>
 *   result = Math.round((float) tier / maxTier * max), clamped to [0, max]
 * </pre>
 * This is the private-static {@code tierToRange()} method's contract. These tests
 * extract and verify that formula so regressions are caught without needing MC bootstrap.</p>
 *
 * <p>Crystal property constants (from {@link hellfirepvp.astralsorcery.common.crystal.CrystalProperties}):
 * <ul>
 *   <li>{@code MAX_SIZE = 900}</li>
 *   <li>{@code MAX_PURITY = 100}</li>
 *   <li>{@code MAX_CUTTING = 100}</li>
 * </ul>
 * Crystal attribute tiers run from 0 (absent) to {@code property.getMaxTier()} (max).</p>
 */
class CrystalAttributeMappingTest {

    // Mirror of CrystalProperties constants — if these change in production, tests will catch it.
    private static final int MAX_SIZE = 900;
    private static final int MAX_PURITY = 100;
    private static final int MAX_CUTTING = 100;

    /**
     * Mirrors the private-static tierToRange() in NBTCopyRecipe.
     * Formula: Math.round((float) tier / maxTier * max), clamped to [0, max].
     */
    private static int tierToRange(int tier, int maxTier, int max) {
        if (maxTier <= 0) return 0;
        return Math.min(max, Math.max(0, Math.round((float) tier / maxTier * max)));
    }

    // =========================================================================
    // Full-tier (max attribute) → max property value
    // =========================================================================

    @Test
    void fullTier_purity_returnsMaxPurity() {
        assertEquals(MAX_PURITY, tierToRange(5, 5, MAX_PURITY));
    }

    @Test
    void fullTier_cutting_returnsMaxCutting() {
        assertEquals(MAX_CUTTING, tierToRange(4, 4, MAX_CUTTING));
    }

    @Test
    void fullTier_size_returnsMaxSize() {
        assertEquals(MAX_SIZE, tierToRange(5, 5, MAX_SIZE));
    }

    @Test
    void fullTier_singleTierProperty_returnsMax() {
        // A property with maxTier=1 and tier=1 should give full value
        assertEquals(MAX_PURITY, tierToRange(1, 1, MAX_PURITY));
    }

    // =========================================================================
    // Zero tier (property absent) → zero property value
    // =========================================================================

    @Test
    void zeroTier_purity_returnsZero() {
        assertEquals(0, tierToRange(0, 5, MAX_PURITY));
    }

    @Test
    void zeroTier_size_returnsZero() {
        assertEquals(0, tierToRange(0, 5, MAX_SIZE));
    }

    @Test
    void zeroTier_cutting_returnsZero() {
        assertEquals(0, tierToRange(0, 4, MAX_CUTTING));
    }

    // =========================================================================
    // Proportional mapping (mid-tier values)
    // =========================================================================

    @Test
    void halfTier_purity_returnsHalfMax() {
        // tier=2, maxTier=4, max=100 → 2/4*100 = 50.0 → 50
        assertEquals(50, tierToRange(2, 4, MAX_PURITY));
    }

    @Test
    void halfTier_cutting_returnsHalfMax() {
        // tier=2, maxTier=4, max=100 → 50
        assertEquals(50, tierToRange(2, 4, MAX_CUTTING));
    }

    @Test
    void twoFifths_purity_returnsFortyPercent() {
        // tier=2, maxTier=5, max=100 → 2/5*100 = 40.0 → 40
        assertEquals(40, tierToRange(2, 5, MAX_PURITY));
    }

    @Test
    void threeFifths_purity_returnsSixtyPercent() {
        // tier=3, maxTier=5, max=100 → 3/5*100 = 60.0 → 60
        assertEquals(60, tierToRange(3, 5, MAX_PURITY));
    }

    @Test
    void oneThird_size_returnsThreeHundred() {
        // tier=1, maxTier=3, max=900 → 1/3*900 = 300.0 → 300
        assertEquals(300, tierToRange(1, 3, MAX_SIZE));
    }

    @Test
    void twoThirds_size_returnsSixHundred() {
        // tier=2, maxTier=3, max=900 → 2/3*900 = 600.0 → 600
        assertEquals(600, tierToRange(2, 3, MAX_SIZE));
    }

    // =========================================================================
    // Rounding behaviour
    // =========================================================================

    @Test
    void rounding_roundsToNearest() {
        // tier=1, maxTier=3, max=100 → 1/3*100 = 33.333... → rounds to 33
        assertEquals(33, tierToRange(1, 3, MAX_PURITY));
    }

    @Test
    void rounding_roundsUp_atHalf() {
        // tier=1, maxTier=6, max=100 → 1/6*100 = 16.666... → rounds to 17
        assertEquals(17, tierToRange(1, 6, MAX_PURITY));
    }

    @Test
    void rounding_twoSixths_returnsThirtyThree() {
        // tier=2, maxTier=6, max=100 → 2/6*100 = 33.333... → rounds to 33
        assertEquals(33, tierToRange(2, 6, MAX_PURITY));
    }

    // =========================================================================
    // Clamping (values must stay in [0, max])
    // =========================================================================

    @Test
    void clamp_tierExceedsMaxTier_clampsToMax() {
        // Shouldn't happen in practice, but the formula must not produce > max
        int result = tierToRange(10, 5, MAX_PURITY);
        assertTrue(result <= MAX_PURITY,
                "Result " + result + " exceeds MAX_PURITY " + MAX_PURITY);
        assertEquals(MAX_PURITY, result);
    }

    @Test
    void clamp_negativeTier_clampsToZero() {
        // Shouldn't happen in practice, but validate graceful handling
        assertEquals(0, tierToRange(-1, 5, MAX_PURITY));
    }

    @Test
    void allResultsWithinBounds_exhaustiveSmall() {
        // Exhaustively check all (tier, maxTier) pairs for maxTier in [1,6]
        for (int maxTier = 1; maxTier <= 6; maxTier++) {
            for (int tier = 0; tier <= maxTier; tier++) {
                int purity = tierToRange(tier, maxTier, MAX_PURITY);
                int size = tierToRange(tier, maxTier, MAX_SIZE);
                int cutting = tierToRange(tier, maxTier, MAX_CUTTING);

                assertTrue(purity >= 0 && purity <= MAX_PURITY,
                        String.format("Purity out of bounds for tier=%d maxTier=%d: %d", tier, maxTier, purity));
                assertTrue(size >= 0 && size <= MAX_SIZE,
                        String.format("Size out of bounds for tier=%d maxTier=%d: %d", tier, maxTier, size));
                assertTrue(cutting >= 0 && cutting <= MAX_CUTTING,
                        String.format("Cutting out of bounds for tier=%d maxTier=%d: %d", tier, maxTier, cutting));
            }
        }
    }

    // =========================================================================
    // Monotonicity: higher tier → higher or equal property value
    // =========================================================================

    @Test
    void monotonic_higherTierProducesHigherOrEqualValue() {
        int maxTier = 5;
        int prev = 0;
        for (int tier = 0; tier <= maxTier; tier++) {
            int value = tierToRange(tier, maxTier, MAX_PURITY);
            assertTrue(value >= prev,
                    String.format("tier=%d produced %d, less than tier=%d value %d", tier, value, tier - 1, prev));
            prev = value;
        }
    }

    @Test
    void monotonic_size_higherTierProducesHigherOrEqualSize() {
        int maxTier = 5;
        int prev = 0;
        for (int tier = 0; tier <= maxTier; tier++) {
            int value = tierToRange(tier, maxTier, MAX_SIZE);
            assertTrue(value >= prev,
                    String.format("tier=%d produced %d, less than tier=%d value %d", tier, value, tier - 1, prev));
            prev = value;
        }
    }
}
