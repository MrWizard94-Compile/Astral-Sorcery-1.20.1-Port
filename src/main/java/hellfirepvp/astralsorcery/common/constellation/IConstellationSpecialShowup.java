package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Marker for constellations with special visibility conditions
 * beyond simple moon phase (e.g., Vorux requiring blood moon).
 *
 * <p>1.16 → 1.20 changes: World → Level,
 * GeneralConfig day length reference deferred</p>
 */
public interface IConstellationSpecialShowup extends IConstellation {

    boolean doesShowUp(@Nonnull Level level, long day);

    float getDistribution(@Nonnull Level level, long day, boolean showingUp);

    default long dayToWorldTime(long day) {
        // Default: 24000 ticks per day (standard MC day length)
        // Will be replaced with GeneralConfig.CONFIG.dayLength.get() when config is ported
        return day * 24000L;
    }
}
