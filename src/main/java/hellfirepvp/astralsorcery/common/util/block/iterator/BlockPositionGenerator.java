package hellfirepvp.astralsorcery.common.util.block.iterator;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Base class for block position generators used by constellation effects.
 * Generates positions within a radius of an offset, with optional filtering.
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag, BlockPos unchanged</p>
 */
public abstract class BlockPositionGenerator {

    @Nonnull
    private BiPredicate<BlockPos, Double> posFilter = (pos, radius) -> true;

    @Nonnull
    public final BlockPositionGenerator andFilter(@Nonnull Predicate<BlockPos> filter) {
        return this.andFilter((pos, radius) -> filter.test(pos));
    }

    @Nonnull
    public final BlockPositionGenerator andFilter(@Nonnull BiPredicate<BlockPos, Double> filter) {
        this.posFilter = this.posFilter.and(filter);
        return this;
    }

    @Nonnull
    public final BlockPositionGenerator copyFilterFrom(@Nonnull BlockPositionGenerator other) {
        other.andFilter(this.posFilter);
        return this;
    }

    @Nonnull
    public final BlockPos generateNextPosition(@Nonnull Vector3 offset, double selectionRadius) {
        BlockPos next;
        do {
            next = genNext(offset, selectionRadius);
        } while (!posFilter.test(next, selectionRadius));
        return next;
    }

    @Nonnull
    protected abstract BlockPos genNext(@Nonnull Vector3 offset, double radius);

    public abstract void writeToNBT(@Nonnull CompoundTag nbt);

    public abstract void readFromNBT(@Nonnull CompoundTag nbt);
}
