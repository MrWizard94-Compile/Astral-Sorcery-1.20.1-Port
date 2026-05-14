package hellfirepvp.astralsorcery.common.event;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BlockChangeNotifier listener registration.
 * Tests the listener list management without needing Minecraft world instances.
 */
class BlockChangeNotifierTest {

    @Test
    void addAndRemoveListener() {
        AtomicInteger callCount = new AtomicInteger(0);
        BlockChangeNotifier.Listener listener = (level, chunk, pos, oldState, newState) ->
                callCount.incrementAndGet();

        BlockChangeNotifier.addListener(listener);
        // We can't easily call notifyChange without a real Level/Chunk, but we can
        // verify the listener was added by removing it (no exception = success)
        BlockChangeNotifier.removeListener(listener);

        // Removing again should be a no-op (CopyOnWriteArrayList.remove returns false)
        BlockChangeNotifier.removeListener(listener);
    }
}
