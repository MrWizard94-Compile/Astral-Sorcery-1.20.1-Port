package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Marker for constellations with special visibility conditions
 * beyond simple moon phase (e.g., Vorux requiring blood moon).
 *
 * <p>1.16 → 1.20 changes: World → Level,
 * GeneralConfig.CONFIG.dayLength → CommonConfig.CONFIG.dayLength</p>
 */
public interface IConstellationSpecialShowup extends IConstellation {

    boolean doesShowUp(@Nonnull Level level, long day);

    float getDistribution(@Nonnull Level level, long day, boolean showingUp);

    default long dayToWorldTime(long day) {
        return day * CommonConfig.CONFIG.dayLength.get();
    }
}
