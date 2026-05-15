package hellfirepvp.astralsorcery.common.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Utility class for crop-related block interactions.
 * Used by constellation effects (Aevitas) and perk growth modifiers.
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level, ServerWorld -> ServerLevel,
 * IGrowable -> BonemealableBlock,
 * CropsBlock -> CropBlock,
 * BlockState.get(property) -> BlockState.getValue(property),
 * BlockState.with(property, val) -> BlockState.setValue(property, val)</p>
 */
public class CropHelper {

    private CropHelper() {}

    /**
     * Attempt to grow the crop at the given position by one stage.
     *
     * @param level the server level
     * @param pos   the crop position
     * @return true if the crop was successfully grown
     */
    public static boolean tryGrowCrop(@Nonnull ServerLevel level, @Nonnull BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            if (crop.isMaxAge(state)) {
                return false;
            }
            int currentAge = crop.getAge(state);
            int maxAge = crop.getMaxAge();
            if (currentAge < maxAge) {
                level.setBlock(pos, crop.getStateForAge(currentAge + 1), Block.UPDATE_ALL);
                return true;
            }
        } else if (block instanceof BonemealableBlock growable) {
            if (growable.isValidBonemealTarget(level, pos, state, false)) {
                if (growable.isBonemealSuccess(level, level.random, pos, state)) {
                    growable.performBonemeal(level, level.random, pos, state);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the block at the given position is a growable crop that is not yet fully grown.
     *
     * @param level the level
     * @param pos   the position to check
     * @return true if the block is a growable crop below max age
     */
    public static boolean isGrowableCrop(@Nonnull Level level, @Nonnull BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            return !crop.isMaxAge(state);
        }
        if (block instanceof BonemealableBlock growable) {
            return growable.isValidBonemealTarget(level, pos, state, level.isClientSide());
        }
        return false;
    }

    /**
     * Get the growth progress of a crop block as a fraction [0, 1].
     *
     * @param level the level
     * @param pos   the crop position
     * @return the growth fraction, or empty if not a crop
     */
    @Nonnull
    public static Optional<Float> getGrowthProgress(@Nonnull Level level, @Nonnull BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            int currentAge = crop.getAge(state);
            int maxAge = crop.getMaxAge();
            if (maxAge <= 0) {
                return Optional.of(1.0F);
            }
            return Optional.of((float) currentAge / (float) maxAge);
        }
        return Optional.empty();
    }

    /**
     * Check if the block at the given position is a fully grown crop.
     *
     * @param level the level
     * @param pos   the position
     * @return true if the block is a crop at maximum age
     */
    public static boolean isFullyGrown(@Nonnull Level level, @Nonnull BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        return false;
    }
}
