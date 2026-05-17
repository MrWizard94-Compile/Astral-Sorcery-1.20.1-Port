/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.world;

import hellfirepvp.astralsorcery.common.base.MoonPhase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for celestial event math.
 * Tests the time-of-day factors, weather penalties, moon phase bonuses,
 * and visibility slot calculations without requiring a Level instance.
 */
class CelestialHandlerTest {

    // ========================================================================
    // We mirror the CelestialHandler constants and logic here for pure testing
    // ========================================================================

    private static final long TICKS_PER_DAY = 24000L;
    private static final long NIGHT_START = 13000L;
    private static final long DAWN_START = 23000L;
    private static final long DAWN_END = 24000L;
    private static final long DUSK_START = 12000L;

    /** Mirrors CelestialHandler.getTimeOfDayFactor */
    private static float computeTimeOfDayFactor(long dayTime) {
        dayTime = dayTime % TICKS_PER_DAY;
        if (dayTime >= NIGHT_START && dayTime < DAWN_START) {
            return 1.0f;
        } else if (dayTime >= DUSK_START && dayTime < NIGHT_START) {
            float progress = (dayTime - DUSK_START) / (float) (NIGHT_START - DUSK_START);
            return 0.2f + progress * 0.8f;
        } else if (dayTime >= DAWN_START) {
            float progress = (dayTime - DAWN_START) / (float) (DAWN_END - DAWN_START);
            return 1.0f - progress * 0.8f;
        } else {
            return 0.2f;
        }
    }

    /** Mirrors CelestialHandler.getMoonPhaseFactor */
    private static float computeMoonPhaseFactor(MoonPhase phase) {
        return switch (phase) {
            case FULL -> 1.2f;
            case WANING_3_4, WAXING_3_4 -> 1.1f;
            case NEW -> 0.8f;
            case WANING_1_4, WAXING_1_4 -> 0.9f;
            default -> 1.0f;
        };
    }

    /** Mirrors visibility slot calculation */
    private static int computeWeakSlots(MoonPhase phase) {
        return switch (phase) {
            case FULL -> 4;
            case WANING_3_4, WAXING_3_4 -> 3;
            case WANING_1_2, WAXING_1_2 -> 2;
            case WANING_1_4, WAXING_1_4 -> 1;
            case NEW -> 0;
        };
    }

    private static int computeMinorSlots(MoonPhase phase) {
        return switch (phase) {
            case FULL -> 2;
            case WANING_3_4, WAXING_3_4 -> 2;
            case WANING_1_2, WAXING_1_2 -> 1;
            default -> 0;
        };
    }

    /** Night progress [0-1] */
    private static float computeNightProgress(long dayTime) {
        dayTime = dayTime % TICKS_PER_DAY;
        if (dayTime < NIGHT_START || dayTime >= DAWN_START) {
            return 0f;
        }
        return (dayTime - NIGHT_START) / (float) (DAWN_START - NIGHT_START);
    }

    // ========================================================================
    // Time of day factor tests
    // ========================================================================

    @Nested
    class TimeOfDayFactorTests {

        @Test
        void testFullDay() {
            // 6000 = noon, should be minimum (0.2)
            assertEquals(0.2f, computeTimeOfDayFactor(6000), 0.001f);
        }

        @Test
        void testSunrise() {
            // 0 = sunrise, still "day"
            assertEquals(0.2f, computeTimeOfDayFactor(0), 0.001f);
        }

        @Test
        void testFullNight() {
            // 18000 = midnight, should be maximum (1.0)
            assertEquals(1.0f, computeTimeOfDayFactor(18000), 0.001f);
        }

        @Test
        void testNightStart() {
            // 13000 = start of night
            assertEquals(1.0f, computeTimeOfDayFactor(13000), 0.001f);
        }

        @Test
        void testNightEnd() {
            // 22999 = last tick of full night
            assertEquals(1.0f, computeTimeOfDayFactor(22999), 0.001f);
        }

        @Test
        void testDuskStart() {
            // 12000 = dusk begins, should be at day level (0.2)
            assertEquals(0.2f, computeTimeOfDayFactor(12000), 0.001f);
        }

        @Test
        void testDuskMidpoint() {
            // 12500 = halfway through dusk
            // progress = 500/1000 = 0.5
            // factor = 0.2 + 0.5 * 0.8 = 0.6
            assertEquals(0.6f, computeTimeOfDayFactor(12500), 0.001f);
        }

        @Test
        void testDawnStart() {
            // 23000 = dawn begins, should start at night level (1.0)
            assertEquals(1.0f, computeTimeOfDayFactor(23000), 0.001f);
        }

        @Test
        void testDawnMidpoint() {
            // 23500 = halfway through dawn
            // progress = 500/1000 = 0.5
            // factor = 1.0 - 0.5 * 0.8 = 0.6
            assertEquals(0.6f, computeTimeOfDayFactor(23500), 0.001f);
        }

        @Test
        void testDawnEnd() {
            // 23999 = nearly end of dawn
            // progress ≈ 999/1000 = 0.999
            // factor ≈ 1.0 - 0.999 * 0.8 ≈ 0.2008
            float factor = computeTimeOfDayFactor(23999);
            assertTrue(factor > 0.19f && factor < 0.22f,
                    "Dawn end should be near 0.2, was " + factor);
        }

        @Test
        void testWrapsAcrossDayBoundary() {
            // 48000 = exactly 2 days, should be same as 0 (sunrise)
            assertEquals(computeTimeOfDayFactor(0), computeTimeOfDayFactor(48000), 0.001f);
        }

        @Test
        void testNeverNegative() {
            for (long t = 0; t < 24000; t += 100) {
                float factor = computeTimeOfDayFactor(t);
                assertTrue(factor >= 0f, "Factor should never be negative at t=" + t);
                assertTrue(factor <= 1.0f, "Factor should never exceed 1.0 at t=" + t);
            }
        }

        @Test
        void testMonotonicallyIncreasingDuringDusk() {
            float prev = computeTimeOfDayFactor(12000);
            for (long t = 12001; t < 13000; t += 50) {
                float curr = computeTimeOfDayFactor(t);
                assertTrue(curr >= prev,
                        "Factor should increase during dusk, t=" + t);
                prev = curr;
            }
        }

        @Test
        void testMonotonicallyDecreasingDuringDawn() {
            float prev = computeTimeOfDayFactor(23000);
            for (long t = 23001; t < 24000; t += 50) {
                float curr = computeTimeOfDayFactor(t);
                assertTrue(curr <= prev,
                        "Factor should decrease during dawn, t=" + t);
                prev = curr;
            }
        }
    }

    // ========================================================================
    // Moon phase factor tests
    // ========================================================================

    @Nested
    class MoonPhaseFactorTests {

        @Test
        void testFullMoonBonus() {
            assertEquals(1.2f, computeMoonPhaseFactor(MoonPhase.FULL), 0.001f);
        }

        @Test
        void testNewMoonPenalty() {
            assertEquals(0.8f, computeMoonPhaseFactor(MoonPhase.NEW), 0.001f);
        }

        @Test
        void testHalfMoonNeutral() {
            assertEquals(1.0f, computeMoonPhaseFactor(MoonPhase.WANING_1_2), 0.001f);
            assertEquals(1.0f, computeMoonPhaseFactor(MoonPhase.WAXING_1_2), 0.001f);
        }

        @Test
        void testThreeQuarterMoonBonus() {
            assertEquals(1.1f, computeMoonPhaseFactor(MoonPhase.WANING_3_4), 0.001f);
            assertEquals(1.1f, computeMoonPhaseFactor(MoonPhase.WAXING_3_4), 0.001f);
        }

        @Test
        void testQuarterMoonPenalty() {
            assertEquals(0.9f, computeMoonPhaseFactor(MoonPhase.WANING_1_4), 0.001f);
            assertEquals(0.9f, computeMoonPhaseFactor(MoonPhase.WAXING_1_4), 0.001f);
        }

        @Test
        void testAllPhasesPositive() {
            for (MoonPhase phase : MoonPhase.values()) {
                float factor = computeMoonPhaseFactor(phase);
                assertTrue(factor > 0, "Moon phase factor should always be positive: " + phase);
            }
        }
    }

    // ========================================================================
    // Visibility slot tests
    // ========================================================================

    @Nested
    class VisibilitySlotTests {

        @Test
        void testFullMoonMaxVisibility() {
            assertEquals(4, computeWeakSlots(MoonPhase.FULL));
            assertEquals(2, computeMinorSlots(MoonPhase.FULL));
        }

        @Test
        void testNewMoonNoVisibility() {
            assertEquals(0, computeWeakSlots(MoonPhase.NEW));
            assertEquals(0, computeMinorSlots(MoonPhase.NEW));
        }

        @Test
        void testThreeQuarterMoon() {
            assertEquals(3, computeWeakSlots(MoonPhase.WANING_3_4));
            assertEquals(2, computeMinorSlots(MoonPhase.WAXING_3_4));
        }

        @Test
        void testHalfMoon() {
            assertEquals(2, computeWeakSlots(MoonPhase.WANING_1_2));
            assertEquals(1, computeMinorSlots(MoonPhase.WAXING_1_2));
        }

        @Test
        void testQuarterMoon() {
            assertEquals(1, computeWeakSlots(MoonPhase.WANING_1_4));
            assertEquals(0, computeMinorSlots(MoonPhase.WAXING_1_4));
        }

        @Test
        void testVisibilitySlotsNeverNegative() {
            for (MoonPhase phase : MoonPhase.values()) {
                assertTrue(computeWeakSlots(phase) >= 0,
                        "Weak slots should not be negative: " + phase);
                assertTrue(computeMinorSlots(phase) >= 0,
                        "Minor slots should not be negative: " + phase);
            }
        }

        @Test
        void testVisibilitySlotsNeverExceedMax() {
            for (MoonPhase phase : MoonPhase.values()) {
                assertTrue(computeWeakSlots(phase) <= 4,
                        "Weak slots should not exceed 4: " + phase);
                assertTrue(computeMinorSlots(phase) <= 2,
                        "Minor slots should not exceed 2: " + phase);
            }
        }
    }

    // ========================================================================
    // Night progress tests
    // ========================================================================

    @Nested
    class NightProgressTests {

        @Test
        void testDayReturnsZero() {
            assertEquals(0f, computeNightProgress(6000), 0.001f);
        }

        @Test
        void testNightStartReturnsZero() {
            assertEquals(0f, computeNightProgress(13000), 0.001f);
        }

        @Test
        void testMidnightReturnsHalf() {
            // 18000 = midpoint of 13000-23000
            // progress = (18000 - 13000) / (23000 - 13000) = 5000/10000 = 0.5
            assertEquals(0.5f, computeNightProgress(18000), 0.001f);
        }

        @Test
        void testBeforeDawnNearOne() {
            // 22999 = one tick before dawn
            float progress = computeNightProgress(22999);
            assertTrue(progress > 0.99f && progress <= 1.0f,
                    "Should be near 1.0, was " + progress);
        }

        @Test
        void testDawnReturnsZero() {
            assertEquals(0f, computeNightProgress(23000), 0.001f);
        }

        @Test
        void testProgressMonotonicallyIncreases() {
            float prev = 0f;
            for (long t = 13001; t < 23000; t += 500) {
                float curr = computeNightProgress(t);
                assertTrue(curr > prev,
                        "Night progress should increase at t=" + t);
                prev = curr;
            }
        }
    }

    // ========================================================================
    // Combined distribution factor tests
    // ========================================================================

    @Nested
    class DistributionFactorTests {

        /**
         * Simulates the combined factor: time * weather * moon.
         */
        private float computeCombined(long dayTime, boolean raining, boolean thundering,
                                      MoonPhase phase) {
            float time = computeTimeOfDayFactor(dayTime);
            float weather = thundering ? 0.5f : (raining ? 0.7f : 1.0f);
            float moon = computeMoonPhaseFactor(phase);
            return time * weather * moon;
        }

        @Test
        void testPerfectNight() {
            // Midnight, clear sky, full moon
            float factor = computeCombined(18000, false, false, MoonPhase.FULL);
            assertEquals(1.2f, factor, 0.001f);
        }

        @Test
        void testWorstCase() {
            // Day, thunderstorm, new moon
            float factor = computeCombined(6000, false, true, MoonPhase.NEW);
            // 0.2 * 0.5 * 0.8 = 0.08
            assertEquals(0.08f, factor, 0.001f);
        }

        @Test
        void testRainyNight() {
            // Night, rain, normal moon
            float factor = computeCombined(18000, true, false, MoonPhase.WANING_1_2);
            // 1.0 * 0.7 * 1.0 = 0.7
            assertEquals(0.7f, factor, 0.001f);
        }

        @Test
        void testThunderNight() {
            // Night, thunder, full moon
            float factor = computeCombined(18000, false, true, MoonPhase.FULL);
            // 1.0 * 0.5 * 1.2 = 0.6
            assertEquals(0.6f, factor, 0.001f);
        }

        @Test
        void testDuskRainFullMoon() {
            // Dusk midpoint, rain, full moon
            float factor = computeCombined(12500, true, false, MoonPhase.FULL);
            // 0.6 * 0.7 * 1.2 = 0.504
            assertEquals(0.504f, factor, 0.001f);
        }

        @Test
        void testFactorAlwaysPositive() {
            MoonPhase[] phases = MoonPhase.values();
            for (long t = 0; t < 24000; t += 1000) {
                for (MoonPhase phase : phases) {
                    float f = computeCombined(t, false, false, phase);
                    assertTrue(f > 0, "Factor must be positive at t=" + t + ", phase=" + phase);
                }
            }
        }
    }
}
