package hellfirepvp.astralsorcery.common.util.block.iterator;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

/**
 * Like {@link BlockRandomPositionGenerator} but biased toward positions
 * closer to the offset (picks the closer of two random candidates).
 *
 * <p>No 1.16 -> 1.20 changes beyond parent class.</p>
 */
public class BlockRandomProximityPositionGenerator extends BlockRandomPositionGenerator {

    @Override
    @Nonnull
    protected BlockPos genNext(@Nonnull Vector3 offset, double radius) {
        BlockPos next1 = super.genNext(offset, radius);
        BlockPos next2 = super.genNext(offset, radius);
        return offset.distance(next1) < offset.distance(next2) ? next1 : next2;
    }
}
