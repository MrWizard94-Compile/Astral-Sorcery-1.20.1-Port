package hellfirepvp.astralsorcery.common.util;

import net.minecraftforge.eventbus.api.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An IEventBus wrapper that tracks registered listeners for bulk unregistration.
 * Used to temporarily listen for events during a specific lifecycle
 * (e.g., recipe loading, data reload) then clean up all listeners at once.
 *
 * <p>No 1.16 → 1.20 changes — Forge event bus API is stable.</p>
 */
public class CacheEventBus implements IEventBus {

    private final List<Object> registeredListeners = new ArrayList<>();
    @Nonnull
    private final IEventBus wrapped;

    private CacheEventBus(@Nonnull IEventBus wrapped) {
        this.wrapped = wrapped;
    }

    @Nonnull
    public static CacheEventBus of(@Nonnull IEventBus bus) {
        return new CacheEventBus(bus);
    }

    public void unregisterAll() {
        registeredListeners.forEach(wrapped::unregister);
        registeredListeners.clear();
    }

    @Override
    public void register(@Nonnull Object target) {
        wrapped.register(target);
        registeredListeners.add(target);
    }

    @Override
    public <T extends Event> void addListener(@Nonnull Consumer<T> consumer) {
        wrapped.addListener(consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(@Nonnull EventPriority priority,
                                              @Nonnull Consumer<T> consumer) {
        wrapped.addListener(priority, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(@Nonnull EventPriority priority,
                                              boolean receiveCancelled,
                                              @Nonnull Consumer<T> consumer) {
        wrapped.addListener(priority, receiveCancelled, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(@Nonnull EventPriority priority,
                                              boolean receiveCancelled,
                                              @Nonnull Class<T> eventType,
                                              @Nonnull Consumer<T> consumer) {
        wrapped.addListener(priority, receiveCancelled, eventType, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            @Nonnull Class<F> genericClassFilter,
            @Nonnull Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            @Nonnull Class<F> genericClassFilter,
            @Nonnull EventPriority priority,
            @Nonnull Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, priority, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            @Nonnull Class<F> genericClassFilter,
            @Nonnull EventPriority priority,
            boolean receiveCancelled,
            @Nonnull Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, priority, receiveCancelled, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            @Nonnull Class<F> genericClassFilter,
            @Nonnull EventPriority priority,
            boolean receiveCancelled,
            @Nonnull Class<T> eventType,
            @Nonnull Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, priority,
                receiveCancelled, eventType, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public void unregister(@Nonnull Object object) {
        wrapped.unregister(object);
        registeredListeners.remove(object);
    }

    @Override
    public boolean post(@Nonnull Event event) {
        return wrapped.post(event);
    }

    @Override
    public boolean post(@Nonnull Event event, @Nonnull IEventBusInvokeDispatcher wrapper) {
        return wrapped.post(event, wrapper);
    }

    @Override
    public void shutdown() {
        wrapped.shutdown();
    }

    @Override
    public void start() {
        wrapped.start();
    }
}
