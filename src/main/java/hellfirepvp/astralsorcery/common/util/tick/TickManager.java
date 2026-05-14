package hellfirepvp.astralsorcery.common.util.tick;

import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Replacement for ObserverLib's TickManager.
 * Dispatches Forge tick events to registered ITickHandler instances.
 *
 * Register handlers via {@link #register(ITickHandler)}, then call
 * {@link #tick(TickEvent)} from the appropriate Forge event listener.
 */
public class TickManager {

    private final List<ITickHandler> handlers = new ArrayList<>();

    /**
     * Register a tick handler.
     *
     * @param handler the handler to register
     */
    public void register(@Nonnull ITickHandler handler) {
        this.handlers.add(handler);
    }

    /**
     * Dispatch a tick event to all registered handlers that match the type and phase.
     * Call this from a Forge TickEvent listener.
     *
     * @param event the Forge tick event
     */
    public void tick(@Nonnull TickEvent event) {
        TickEvent.Type type = resolveType(event);
        if (type == null) {
            return;
        }
        TickEvent.Phase phase = event.phase;

        for (ITickHandler handler : this.handlers) {
            EnumSet<TickEvent.Type> handled = handler.getHandledTypes();
            if (handled.contains(type) && handler.canFire(phase)) {
                handler.tick(type, extractContext(event));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static TickEvent.Type resolveType(@Nonnull TickEvent event) {
        if (event instanceof TickEvent.ServerTickEvent) {
            return TickEvent.Type.SERVER;
        } else if (event instanceof TickEvent.ClientTickEvent) {
            return TickEvent.Type.CLIENT;
        } else if (event instanceof TickEvent.LevelTickEvent) {
            return TickEvent.Type.LEVEL;
        } else if (event instanceof TickEvent.PlayerTickEvent) {
            return TickEvent.Type.PLAYER;
        } else if (event instanceof TickEvent.RenderTickEvent) {
            return TickEvent.Type.RENDER;
        }
        return null;
    }

    @Nonnull
    private static Object[] extractContext(@Nonnull TickEvent event) {
        if (event instanceof TickEvent.LevelTickEvent levelTick) {
            return new Object[]{ levelTick.level };
        } else if (event instanceof TickEvent.PlayerTickEvent playerTick) {
            return new Object[]{ playerTick.player };
        } else if (event instanceof TickEvent.RenderTickEvent renderTick) {
            return new Object[]{ renderTick.renderTickTime };
        }
        return new Object[0];
    }
}
