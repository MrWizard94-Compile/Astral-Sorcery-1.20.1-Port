/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.world;

import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Static utility for common day/time queries.
 * Wraps Minecraft's time system with named helpers that make
 * intent clear in caller code.
 *
 * <p>Minecraft day cycle:
 * <ul>
 *   <li>0: sunrise (start of day)</li>
 *   <li>6000: noon</li>
 *   <li>12000: sunset (dusk begins)</li>
 *   <li>13000: night (stars visible)</li>
 *   <li>18000: midnight (peak starlight)</li>
 *   <li>23000: dawn (stars fading)</li>
 *   <li>24000/0: sunrise (next day)</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20 changes: World → Level,
 * world.getDayTime() → level.getDayTime(),
 * world.getGameTime() → level.getGameTime()</p>
 */
public final class DayTimeHelper {

    private static final long TICKS_PER_DAY = 24000L;
    private static final long SUNRISE = 0L;
    private static final long SUNSET = 12000L;
    private static final long NIGHT_START = 13000L;
    private static final long DAWN_START = 23000L;

    private DayTimeHelper() {}

    /**
     * Gets the time of day [0, 24000).
     */
    public static long getTimeOfDay(@Nonnull Level level) {
        return level.getDayTime() % TICKS_PER_DAY;
    }

    /**
     * Gets the absolute day number (how many full days have elapsed).
     */
    public static long getCurrentDay(@Nonnull Level level) {
        return level.getDayTime() / TICKS_PER_DAY;
    }

    /**
     * Whether the level is currently in the night phase (stars visible).
     */
    public static boolean isNight(@Nonnull Level level) {
        long time = getTimeOfDay(level);
        return time >= NIGHT_START && time < DAWN_START;
    }

    /**
     * Whether the level is currently in the day phase (full sunlight).
     */
    public static boolean isDay(@Nonnull Level level) {
        long time = getTimeOfDay(level);
        return time >= SUNRISE && time < SUNSET;
    }

    /**
     * Whether the level is in dusk transition (12000-13000).
     */
    public static boolean isDusk(@Nonnull Level level) {
        long time = getTimeOfDay(level);
        return time >= SUNSET && time < NIGHT_START;
    }

    /**
     * Whether the level is in dawn transition (23000-24000).
     */
    public static boolean isDawn(@Nonnull Level level) {
        long time = getTimeOfDay(level);
        return time >= DAWN_START;
    }

    /**
     * Progress through the night [0.0 = dusk start, 1.0 = dawn end].
     * Returns 0 during daytime.
     */
    public static float getNightProgress(@Nonnull Level level) {
        long time = getTimeOfDay(level);
        if (time < NIGHT_START || time >= DAWN_START) {
            return 0f;
        }
        return (time - NIGHT_START) / (float) (DAWN_START - NIGHT_START);
    }

    /**
     * Progress through the day [0.0 = sunrise, 1.0 = sunset].
     * Returns 0 during nighttime.
     */
    public static float getDayProgress(@Nonnull Level level) {
        long time = getTimeOfDay(level);
        if (time >= SUNSET) {
            return 0f;
        }
        return time / (float) SUNSET;
    }

    /**
     * Angular position of the sun/moon [0.0 - 1.0] wrapping.
     * 0.0 = sunrise, 0.25 = noon, 0.5 = sunset, 0.75 = midnight.
     */
    public static float getCelestialAngle(@Nonnull Level level) {
        return getTimeOfDay(level) / (float) TICKS_PER_DAY;
    }

    /**
     * Whether the sky is visible (can see stars) considering weather.
     */
    public static boolean canSeeSky(@Nonnull Level level) {
        return isNight(level) && !level.isRaining() && !level.isThundering();
    }

    /**
     * Gets a unique seed for the current night, useful for deterministic
     * constellation selection.
     */
    public static long getNightSeed(@Nonnull Level level) {
        long day = getCurrentDay(level);
        return day * 31L + 7L;
    }
}
