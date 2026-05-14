package hellfirepvp.astralsorcery.common.util.block;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

/**
 * Interface for objects that have a BlockPos location.
 */
public interface ILocatable {

    @Nonnull
    BlockPos getLocationPos();

    @Nonnull
    static ILocatable fromPos(@Nonnull BlockPos pos) {
        return new PosLocatable(pos);
    }

    /**
     * Simple BlockPos-backed locatable.
     */
    class PosLocatable implements ILocatable {

        private final BlockPos pos;

        private PosLocatable(@Nonnull BlockPos pos) {
            this.pos = pos;
        }

        @Override
        @Nonnull
        public BlockPos getLocationPos() {
            return pos;
        }
    }
}
