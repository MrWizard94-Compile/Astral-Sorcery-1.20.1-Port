/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Interface for constellation effects that can report a status
 * (e.g. ritual pedestal active/inactive state) to callers.
 *
 * <p>1.16 → 1.20: World → Level, BlockPos unchanged</p>
 */
public interface ConstellationEffectStatus {

    boolean runStatusEffect(Level level, BlockPos pos, int mirrorAmount,
                            ConstellationEffectProperties modified,
                            @Nullable IMinorConstellation possibleTraitEffect);
}
