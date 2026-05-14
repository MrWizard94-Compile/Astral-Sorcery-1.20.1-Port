package hellfirepvp.astralsorcery.common.structure.observer;

import hellfirepvp.astralsorcery.common.lib.structure.PatternBlockArray;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replacement for ObserverLib's ChangeSubscriber.
 * Tracks a structure observation at a specific world position.
 * Checks whether the expected multiblock structure pattern is valid (matched)
 * against the actual world state.
 *
 * <p>Used by TileEntityTick to track whether its required multiblock is formed.</p>
 *
 * @param <T> the observer type (typically {@link ChangeObserverStructure})
 */
public class ChangeSubscriber<T> {

    private final T observer;
    private final BlockPos origin;
    @Nullable
    private final PatternBlockArray pattern;

    public ChangeSubscriber(@Nonnull T observer, @Nonnull BlockPos origin,
                            @Nullable PatternBlockArray pattern) {
        this.observer = observer;
        this.origin = origin;
        this.pattern = pattern;
    }

    /**
     * Check whether the observed structure is valid (all blocks match the pattern).
     *
     * @param level the world to check against
     * @return true if the structure pattern matches the world state at the origin
     */
    public boolean isValid(@Nonnull Level level) {
        if (this.pattern == null) {
            return false;
        }
        return this.pattern.matches(level, this.origin);
    }

    /**
     * Get the observer associated with this subscription.
     *
     * @return the observer instance
     */
    @Nonnull
    public T getObserver() {
        return this.observer;
    }

    /**
     * Get the origin position this subscriber is observing.
     *
     * @return the block position
     */
    @Nonnull
    public BlockPos getOrigin() {
        return this.origin;
    }
}
