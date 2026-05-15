package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for blocks that provide dynamic tint colors.
 * Registration with Minecraft's block color system is done client-side
 * during mod loading via {@code RegisterColorHandlersEvent.Block}.
 *
 * <p>1.16 -> 1.20 changes:
 * IBlockColor interface still exists but registered via event in 1.20.
 * ILightReader -> BlockAndTintGetter.</p>
 */
public interface BlockDynamicColor {

    /**
     * Get the tint color for this block at the given position and tint index.
     *
     * @param state      the block state
     * @param level      the level (may be null during item rendering)
     * @param pos        the position (may be null during item rendering)
     * @param tintIndex  the tint index (0-based, from model)
     * @return the ARGB color value
     */
    int getBlockColor(@Nonnull BlockState state, @Nullable BlockAndTintGetter level,
                      @Nullable BlockPos pos, int tintIndex);
}
