package hellfirepvp.astralsorcery.common.lib.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's SimpleMatchableBlock.
 * Matches any block state of a given block type.
 * Override {@link #getDescriptiveState(long)} for state-specific display.
 */
public class SimpleMatchableBlock implements MatchableState {

    private final Block block;

    public SimpleMatchableBlock(@Nonnull Block block) {
        this.block = block;
    }

    @Nonnull
    public Block getBlock() {
        return this.block;
    }

    @Override
    @Nonnull
    public BlockState getDescriptiveState(long tick) {
        return this.block.defaultBlockState();
    }

    @Override
    public boolean matches(@Nonnull BlockState state) {
        return state.getBlock() == this.block;
    }
}
