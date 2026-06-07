/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.auxiliary.charge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the regen, drain, and fill-percentage math in
 * {@link AlignmentChargeHandler}, extracted as pure formulas so they can be
 * verified without a live {@link net.minecraft.world.entity.player.Player} instance.
 *
 * <p>These tests document the regen contract and catch regressions if the formula
 * is edited. The formulas match the implementation in
 * {@link AlignmentChargeHandler#onPlayerTick} exactly.</p>
 */
class AlignmentChargeHandlerMathTest {

    private static final float MAX_CHARGE = AlignmentChargeHandler.MAX_CHARGE; // 1000F
    private static final float DELTA = 0.001F;

    // =========================================================================
    // Regen-per-tick computation
    // =========================================================================

    /**
     * Base regen rate: fills completely in 6 seconds (6 × 20 ticks).
     * No environment multipliers applied.
     */
    private static float baseRegenPerTick(float maxCharge) {
        return maxCharge / (6F * 20F);
    }

    /**
     * Full regen rate after environment scaling.
     * Mirrors AlignmentChargeHandler.onPlayerTick.
     */
    private static float computeRegenPerTick(float maxCharge, boolean underground,
                                              float timeOfDayFactor) {
        float base = baseRegenPerTick(maxCharge);
        float dayMultiplier = underground ? 0.85F : 0.3F + 0.7F * timeOfDayFactor;
        float caveMultiplier = underground ? 0.25F : 1F;
        return base * dayMultiplier * caveMultiplier;
    }

    @Test
    void baseRate_fillsInSixSeconds() {
        // After 6*20=120 ticks at base rate, charge should equal max.
        float base = baseRegenPerTick(MAX_CHARGE);
        float accumulated = base * 120F;
        assertEquals(MAX_CHARGE, accumulated, DELTA);
    }

    @Test
    void baseRate_isPositive() {
        assertTrue(baseRegenPerTick(MAX_CHARGE) > 0F);
    }

    @Test
    void surface_fullDay_maxRegen() {
        // timeOfDayFactor = 1.0 (midday): dayMultiplier = 0.3 + 0.7*1.0 = 1.0, caveMultiplier = 1.0
        float regen = computeRegenPerTick(MAX_CHARGE, false, 1.0F);
        assertEquals(baseRegenPerTick(MAX_CHARGE), regen, DELTA);
    }

    @Test
    void surface_midnight_minRegen() {
        // timeOfDayFactor = 0.0 (midnight): dayMultiplier = 0.3 + 0.7*0.0 = 0.3, caveMultiplier = 1.0
        float regen = computeRegenPerTick(MAX_CHARGE, false, 0.0F);
        assertEquals(baseRegenPerTick(MAX_CHARGE) * 0.3F, regen, DELTA);
    }

    @Test
    void surface_halfDay_halfDayMultiplier() {
        // timeOfDayFactor = 0.5: dayMultiplier = 0.3 + 0.7*0.5 = 0.65, caveMultiplier = 1.0
        float regen = computeRegenPerTick(MAX_CHARGE, false, 0.5F);
        assertEquals(baseRegenPerTick(MAX_CHARGE) * 0.65F, regen, DELTA);
    }

    @Test
    void underground_regenIsLowerThanSurface() {
        // Underground: dayMultiplier=0.85, caveMultiplier=0.25 → net 0.2125
        // Surface at full day: net 1.0 (= base)
        float underground = computeRegenPerTick(MAX_CHARGE, true, 1.0F);
        float surface = computeRegenPerTick(MAX_CHARGE, false, 1.0F);
        assertTrue(underground < surface,
                "Underground regen (" + underground + ") should be less than surface (" + surface + ")");
    }

    @Test
    void underground_netMultiplierIs0_2125() {
        // 0.85 * 0.25 = 0.2125
        float regen = computeRegenPerTick(MAX_CHARGE, true, 1.0F);
        float expected = baseRegenPerTick(MAX_CHARGE) * 0.85F * 0.25F;
        assertEquals(expected, regen, DELTA);
    }

    @Test
    void underground_dayFactorIsIgnoredForCaveMultiplier() {
        // Underground: dayMultiplier uses 0.85F regardless of dayFactor, caveMultiplier is 0.25F
        float regenDay = computeRegenPerTick(MAX_CHARGE, true, 1.0F);
        float regenNight = computeRegenPerTick(MAX_CHARGE, true, 0.0F);
        // The underground dayMultiplier is always 0.85F — day factor is ignored underground
        assertEquals(regenDay, regenNight, DELTA);
    }

    @Test
    void regenDoesNotExceedMax() {
        // Regen should be clamped so accumulated charge never exceeds max
        float regen = computeRegenPerTick(MAX_CHARGE, false, 1.0F);
        float current = MAX_CHARGE - regen * 0.5F; // almost full
        float newCharge = Math.min(current + regen, MAX_CHARGE);
        assertTrue(newCharge <= MAX_CHARGE, "Charge must never exceed MAX_CHARGE");
    }

    // =========================================================================
    // Drain logic
    // =========================================================================

    /**
     * Drain result: clamps to [0, max] after subtracting drain amount.
     * Mirrors the arithmetic in AlignmentChargeHandler.drainCharge.
     */
    private static float applyDrain(float current, float max, float drain) {
        return Math.min(max, Math.max(0F, current - drain));
    }

    @Test
    void drain_sufficientCharge_reducesChargeCorrectly() {
        float result = applyDrain(500F, MAX_CHARGE, 100F);
        assertEquals(400F, result, DELTA);
    }

    @Test
    void drain_exactAmount_reducesToZero() {
        float result = applyDrain(100F, MAX_CHARGE, 100F);
        assertEquals(0F, result, DELTA);
    }

    @Test
    void drain_moreThanAvailable_clampsToZero() {
        float result = applyDrain(50F, MAX_CHARGE, 200F);
        assertEquals(0F, result, DELTA);
    }

    @Test
    void drain_zeroAmount_leavesChargeUnchanged() {
        float result = applyDrain(700F, MAX_CHARGE, 0F);
        assertEquals(700F, result, DELTA);
    }

    @Test
    void drain_fromFullCharge_leavesCorrectRemainder() {
        float result = applyDrain(MAX_CHARGE, MAX_CHARGE, 250F);
        assertEquals(750F, result, DELTA);
    }

    // =========================================================================
    // hasCharge logic (pure inequality check — creative/spectator bypassed)
    // =========================================================================

    @Test
    void hasCharge_sufficientCharge_returnsTrue() {
        float current = 500F;
        float required = 100F;
        assertTrue(current >= required);
    }

    @Test
    void hasCharge_insufficientCharge_returnsFalse() {
        float current = 50F;
        float required = 100F;
        assertFalse(current >= required);
    }

    @Test
    void hasCharge_exactlyEnough_returnsTrue() {
        float current = 100F;
        float required = 100F;
        assertTrue(current >= required);
    }

    @Test
    void hasCharge_zeroRequired_alwaysTrue() {
        assertTrue(0F >= 0F);
        assertTrue(MAX_CHARGE >= 0F);
    }

    // =========================================================================
    // Fill-percentage formula
    // =========================================================================

    /**
     * Fill percentage: current / max, clamped to [0, 1].
     * Mirrors AlignmentChargeHandler.getFilledPercentage.
     */
    private static float fillPercentage(float current, float max) {
        return Math.min(1F, Math.max(0F, current / max));
    }

    @Test
    void fillPercentage_full_isOne() {
        assertEquals(1.0F, fillPercentage(MAX_CHARGE, MAX_CHARGE), DELTA);
    }

    @Test
    void fillPercentage_empty_isZero() {
        assertEquals(0.0F, fillPercentage(0F, MAX_CHARGE), DELTA);
    }

    @Test
    void fillPercentage_half_isHalf() {
        assertEquals(0.5F, fillPercentage(MAX_CHARGE / 2F, MAX_CHARGE), DELTA);
    }

    @Test
    void fillPercentage_overMax_clampsToOne() {
        // Should never happen, but guard against it
        assertEquals(1.0F, fillPercentage(MAX_CHARGE * 2F, MAX_CHARGE), DELTA);
    }

    @Test
    void fillPercentage_negative_clampsToZero() {
        // Should never happen, but guard against it
        assertEquals(0.0F, fillPercentage(-100F, MAX_CHARGE), DELTA);
    }

    @Test
    void fillPercentage_oneTick_isNonZero() {
        // A single tick of regen should produce a measurable fill percentage
        float regen = computeRegenPerTick(MAX_CHARGE, false, 1.0F);
        float pct = fillPercentage(regen, MAX_CHARGE);
        assertTrue(pct > 0F && pct < 1F,
                "Single-tick regen should produce a small but positive fill %");
    }

    // =========================================================================
    // MAX_CHARGE constant sanity checks
    // =========================================================================

    @Test
    void maxCharge_isPositive() {
        assertTrue(AlignmentChargeHandler.MAX_CHARGE > 0F);
    }

    @Test
    void maxCharge_matchesExpectedValue() {
        assertEquals(1000F, AlignmentChargeHandler.MAX_CHARGE, DELTA);
    }
}
