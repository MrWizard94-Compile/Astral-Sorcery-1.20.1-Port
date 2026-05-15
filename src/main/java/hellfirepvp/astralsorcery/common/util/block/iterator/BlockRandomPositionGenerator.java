package hellfirepvp.astralsorcery.common.util.block.iterator;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Generates random block positions within a radius of the offset.
 *
 * <p>1.16 -> 1.20 changes: CompoundNBT -> CompoundTag</p>
 */
public class BlockRandomPositionGenerator extends BlockPositionGenerator {

    @Override
    @Nonnull
    protected BlockPos genNext(@Nonnull Vector3 offset, double radius) {
        if (radius <= 0) {
            return offset.toBlockPos();
        }
        return offset.clone().add(Vector3.random().multiply(radius)).toBlockPos();
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag nbt) {}

    @Override
    public void readFromNBT(@Nonnull CompoundTag nbt) {}
}
