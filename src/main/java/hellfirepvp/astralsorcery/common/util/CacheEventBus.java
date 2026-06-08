package hellfirepvp.astralsorcery.common.util;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;

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
    public void register(Object target) {
        wrapped.register(target);
        registeredListeners.add(target);
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> consumer) {
        wrapped.addListener(consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority,
                                              Consumer<T> consumer) {
        wrapped.addListener(priority, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority,
                                              boolean receiveCancelled,
                                              Consumer<T> consumer) {
        wrapped.addListener(priority, receiveCancelled, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority,
                                              boolean receiveCancelled,
                                              Class<T> eventType,
                                              Consumer<T> consumer) {
        wrapped.addListener(priority, receiveCancelled, eventType, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            Class<F> genericClassFilter,
            Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            Class<F> genericClassFilter,
            EventPriority priority,
            Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, priority, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            Class<F> genericClassFilter,
            EventPriority priority,
            boolean receiveCancelled,
            Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, priority, receiveCancelled, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(
            Class<F> genericClassFilter,
            EventPriority priority,
            boolean receiveCancelled,
            Class<T> eventType,
            Consumer<T> consumer) {
        wrapped.addGenericListener(genericClassFilter, priority,
                receiveCancelled, eventType, consumer);
        registeredListeners.add(consumer);
    }

    @Override
    public void unregister(Object object) {
        wrapped.unregister(object);
        registeredListeners.remove(object);
    }

    @Override
    public boolean post(Event event) {
        return wrapped.post(event);
    }

    @Override
    public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {
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
