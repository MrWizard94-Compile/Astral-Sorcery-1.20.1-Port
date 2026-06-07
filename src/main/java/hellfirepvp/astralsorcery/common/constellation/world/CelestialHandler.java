/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.world;

import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Central handler for celestial events — determines which constellations
 * are visible in the sky on any given night, and provides the overall
 * starlight distribution multiplier.
 *
 * <p>Each night, a subset of constellations becomes "active" based on:
 * <ul>
 *   <li>Moon phase (fuller = more visible, new = fewer)</li>
 *   <li>Day number seed (ensures deterministic rotation)</li>
 *   <li>Constellation tier (major always visible, weak/minor rotate)</li>
 * </ul></p>
 *
 * <p>The starlight distribution factor varies:
 * <ul>
 *   <li>Night: 1.0 (full collection)</li>
 *   <li>Day: 0.2 (reduced ambient collection)</li>
 *   <li>Twilight: linear interpolation 0.2-1.0</li>
 *   <li>Rain/storm: -30% penalty</li>
 *   <li>Full moon: +20% bonus</li>
 *   <li>New moon: -20% penalty</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20 changes:
 * ServerWorld → ServerLevel,
 * world.getDayTime() → level.getDayTime(),
 * world.isRaining() → level.isRaining(),
 * world.isThundering() → level.isThundering()</p>
 */
public class CelestialHandler {

    /** Ticks in a full Minecraft day. */
    private static final long TICKS_PER_DAY = 24000L;

    /** Tick when night starts (13000). */
    private static final long NIGHT_START = 13000L;

    /** Twilight zones: dusk 12000-13000, dawn 23000-24000 */
    private static final long DUSK_START = 12000L;
    private static final long DAWN_START = 23000L;
    private static final long DAWN_END = 24000L;

    /** Maximum constellations visible per night (weak + minor). */
    private static final int MAX_VISIBLE_WEAK = 4;
    private static final int MAX_VISIBLE_MINOR = 2;

    // ========================================================================
    // Starlight distribution factor
    // ========================================================================

    /**
     * Calculates the overall starlight distribution factor for the given level.
     * Accounts for time of day, weather, and moon phase.
     *
     * @param level the level to calculate for
     * @return starlight multiplier [0.0 - 1.4] (can exceed 1.0 on full moon clear nights)
     */
    public static float getStarlightDistributionFactor(@Nonnull Level level) {
        float timeFactor = getTimeOfDayFactor(level);
        float weatherFactor = getWeatherFactor(level);
        float moonFactor = getMoonPhaseFactor(level);

        return timeFactor * weatherFactor * moonFactor;
    }

    /**
     * Base multiplier from time of day.
     * Night=1.0, Day=0.2, Twilight=interpolated.
     */
    public static float getTimeOfDayFactor(@Nonnull Level level) {
        long dayTime = level.getDayTime() % TICKS_PER_DAY;

        if (dayTime >= NIGHT_START && dayTime < DAWN_START) {
            // Full night
            return 1.0f;
        } else if (dayTime >= DUSK_START && dayTime < NIGHT_START) {
            // Dusk transition: 12000-13000
            float progress = (dayTime - DUSK_START) / (float) (NIGHT_START - DUSK_START);
            return 0.2f + progress * 0.8f;
        } else if (dayTime >= DAWN_START) {
            // Dawn transition: 23000-24000
            float progress = (dayTime - DAWN_START) / (float) (DAWN_END - DAWN_START);
            return 1.0f - progress * 0.8f;
        } else {
            // Full day
            return 0.2f;
        }
    }

    /**
     * Weather penalty factor.
     * Clear=1.0, Rain=-30%, Thunder=-50%.
     */
    public static float getWeatherFactor(@Nonnull Level level) {
        if (level.isThundering()) {
            return 0.5f;
        } else if (level.isRaining()) {
            return 0.7f;
        }
        return 1.0f;
    }

    /**
     * Moon phase bonus/penalty.
     * Full moon=1.2, New moon=0.8, others=1.0.
     */
    public static float getMoonPhaseFactor(@Nonnull Level level) {
        MoonPhase phase = MoonPhase.fromWorld(level);
        return switch (phase) {
            case FULL -> 1.2f;
            case WANING_3_4, WAXING_3_4 -> 1.1f;
            case NEW -> 0.8f;
            case WANING_1_4, WAXING_1_4 -> 0.9f;
            default -> 1.0f;
        };
    }

    /**
     * Whether it is currently night in the given level.
     */
    public static boolean isNight(@Nonnull Level level) {
        long dayTime = level.getDayTime() % TICKS_PER_DAY;
        return dayTime >= NIGHT_START && dayTime < DAWN_START;
    }

    /**
     * Whether the sky is clear enough for optimal starlight collection.
     */
    public static boolean isClearSky(@Nonnull Level level) {
        return !level.isRaining() && !level.isThundering();
    }

    // ========================================================================
    // Constellation visibility
    // ========================================================================

    /**
     * Determines which constellations are visible on the current night.
     * Major constellations are always visible. Weak and minor rotate.
     *
     * @param level the server level
     * @return set of visible constellations for the current night
     */
    @Nonnull
    public static Set<IConstellation> getVisibleConstellations(@Nonnull Level level) {
        Set<IConstellation> visible = new LinkedHashSet<>();

        // Major constellations are always visible at night
        if (isNight(level)) {
            visible.addAll(ConstellationRegistry.getMajorConstellations());
        }

        // Weak constellations rotate based on day number and moon phase
        long dayNumber = level.getDayTime() / TICKS_PER_DAY;
        MoonPhase phase = MoonPhase.fromWorld(level);
        int weakSlots = getWeakVisibilitySlots(phase);
        int minorSlots = getMinorVisibilitySlots(phase);

        List<IWeakConstellation> weakPool = new ArrayList<>(ConstellationRegistry.getWeakConstellations());
        // Remove major constellations from weak pool (they're already added)
        weakPool.removeIf(c -> c instanceof IMajorConstellation);

        List<IMinorConstellation> minorPool = new ArrayList<>(ConstellationRegistry.getMinorConstellations());

        // Deterministic selection based on day number
        Random dayRng = new Random(dayNumber * 31L + level.dimension().location().hashCode());
        Collections.shuffle(weakPool, dayRng);
        Collections.shuffle(minorPool, dayRng);

        // Add visible weak constellations
        for (int i = 0; i < Math.min(weakSlots, weakPool.size()); i++) {
            visible.add(weakPool.get(i));
        }

        // Add visible minor constellations
        for (int i = 0; i < Math.min(minorSlots, minorPool.size()); i++) {
            visible.add(minorPool.get(i));
        }

        // Check special show-up constellations
        for (IConstellationSpecialShowup special : ConstellationRegistry.getSpecialShowupConstellations()) {
            if (special.doesShowUp(level, dayNumber)) {
                visible.add(special);
            }
        }

        return visible;
    }

    /**
     * Gets the number of weak constellation visibility slots for the given moon phase.
     * Fuller moon = more visible constellations.
     */
    private static int getWeakVisibilitySlots(@Nonnull MoonPhase phase) {
        return switch (phase) {
            case FULL -> MAX_VISIBLE_WEAK;
            case WANING_3_4, WAXING_3_4 -> MAX_VISIBLE_WEAK - 1;
            case WANING_1_2, WAXING_1_2 -> MAX_VISIBLE_WEAK - 2;
            case WANING_1_4, WAXING_1_4 -> 1;
            case NEW -> 0;
        };
    }

    /**
     * Gets the number of minor constellation visibility slots for the given moon phase.
     */
    private static int getMinorVisibilitySlots(@Nonnull MoonPhase phase) {
        return switch (phase) {
            case FULL -> MAX_VISIBLE_MINOR;
            case WANING_3_4, WAXING_3_4 -> MAX_VISIBLE_MINOR;
            case WANING_1_2, WAXING_1_2 -> 1;
            default -> 0;
        };
    }

    /**
     * Checks if a specific constellation is visible on the current night.
     *
     * @param level         the level
     * @param constellation the constellation to check
     * @return true if the constellation is currently visible
     */
    public static boolean isConstellationVisible(@Nonnull Level level,
                                                  @Nonnull IConstellation constellation) {
        if (!isNight(level)) {
            return false;
        }
        return getVisibleConstellations(level).contains(constellation);
    }

    /**
     * Gets the visibility intensity of a constellation for rendering.
     * Takes into account twilight fading and weather.
     *
     * @return visibility intensity [0.0 - 1.0]
     */
    public static float getConstellationVisibilityIntensity(@Nonnull Level level,
                                                             @Nonnull IConstellation constellation) {
        if (!isConstellationVisible(level, constellation)) {
            return 0.0f;
        }

        float timeIntensity = getTimeOfDayFactor(level);
        float weatherDim = getWeatherFactor(level);

        // Stars are fully bright once it's night, fading in at dusk and out at dawn
        return Math.min(1.0f, timeIntensity * weatherDim);
    }

    // ========================================================================
    // Constellation-specific starlight bonus
    // ========================================================================

    /**
     * Calculates the starlight bonus multiplier when a collector crystal is
     * attuned to a constellation that is currently visible.
     * Active constellation = 1.5x, inactive = 1.0x (no bonus).
     *
     * @param level                the level
     * @param attunedConstellation the crystal's attuned constellation, or null
     * @return bonus multiplier [1.0 - 1.5]
     */
    public static float getAttunementBonus(@Nonnull Level level,
                                            @Nullable ResourceLocation attunedConstellation) {
        if (attunedConstellation == null) {
            return 1.0f;
        }
        IConstellation constellation = ConstellationRegistry.getConstellation(attunedConstellation);
        if (constellation == null) {
            return 1.0f;
        }
        if (isConstellationVisible(level, constellation)) {
            return 1.5f;
        }
        return 1.0f;
    }

    /**
     * Gets the current day number for the level (how many full days have passed).
     */
    public static long getDayNumber(@Nonnull Level level) {
        return level.getDayTime() / TICKS_PER_DAY;
    }

    /**
     * Gets the progression within the current night [0.0 - 1.0].
     * Returns 0 if it's currently day.
     */
    public static float getNightProgress(@Nonnull Level level) {
        long dayTime = level.getDayTime() % TICKS_PER_DAY;
        if (dayTime < NIGHT_START || dayTime >= DAWN_START) {
            return 0f;
        }
        return (dayTime - NIGHT_START) / (float) (DAWN_START - NIGHT_START);
    }
}
