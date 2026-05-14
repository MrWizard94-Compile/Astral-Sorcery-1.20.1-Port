package hellfirepvp.astralsorcery.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Replacement for ObserverLib's BlockChangeNotifier.
 * Dispatches block change events to registered listeners.
 * Used by EventHandlerAutoLink to react to block placement/removal
 * for auto-linking altars and crafting tables to the starlight network.
 *
 * <p>Listeners are registered during mod setup and notified when blocks change.
 * In the 1.20 port, this is triggered via Forge's BlockEvent or mixin hooks
 * rather than ObserverLib's chunk observation system.</p>
 */
public final class BlockChangeNotifier {

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private BlockChangeNotifier() {}

    /**
     * Register a listener that will be notified of block changes.
     *
     * @param listener the listener to add
     */
    public static void addListener(@Nonnull Listener listener) {
        LISTENERS.add(listener);
    }

    /**
     * Remove a previously registered listener.
     *
     * @param listener the listener to remove
     */
    public static void removeListener(@Nonnull Listener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Notify all registered listeners of a block change.
     * Called from Forge event handlers when a block state changes.
     *
     * @param level    the world where the change occurred
     * @param chunk    the chunk containing the changed block
     * @param pos      the position of the changed block
     * @param oldState the previous block state
     * @param newState the new block state
     */
    public static void notifyChange(@Nonnull Level level,
                                     @Nonnull LevelChunk chunk,
                                     @Nonnull BlockPos pos,
                                     @Nonnull BlockState oldState,
                                     @Nonnull BlockState newState) {
        for (Listener listener : LISTENERS) {
            listener.onChange(level, chunk, pos, oldState, newState);
        }
    }

    /**
     * Listener interface for block change events.
     * Implement this and register via {@link #addListener(Listener)}.
     */
    public interface Listener {

        /**
         * Called when a block changes in the world.
         *
         * @param level    the world
         * @param chunk    the chunk containing the change
         * @param pos      the block position
         * @param oldState the previous state
         * @param newState the new state
         */
        void onChange(@Nonnull Level level,
                      @Nonnull LevelChunk chunk,
                      @Nonnull BlockPos pos,
                      @Nonnull BlockState oldState,
                      @Nonnull BlockState newState);
    }
}
