package hellfirepvp.astralsorcery.common.lib.structure;

import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's MatchableState.
 * Represents a block state that can be matched against in multiblock patterns,
 * with support for animated/descriptive states (e.g., rotating pillar types).
 */
public interface MatchableState {

    /**
     * Returns the block state to display/match for a given tick.
     * For static blocks, just return the default state.
     * For animated/alternating displays (e.g., journal rendering),
     * cycle through states based on tick.
     *
     * @param tick the current animation tick
     * @return the block state to match/display
     */
    @Nonnull
    BlockState getDescriptiveState(long tick);

    /**
     * Tests whether the given world block state matches this pattern entry.
     *
     * @param state the block state found in the world
     * @return true if it matches this pattern position
     */
    default boolean matches(@Nonnull BlockState state) {
        return state.getBlock() == getDescriptiveState(0).getBlock();
    }
}
