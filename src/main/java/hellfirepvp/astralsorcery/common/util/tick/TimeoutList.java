package hellfirepvp.astralsorcery.common.util.tick;

import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A map of elements with per-element tick-based timeouts.
 * Elements are removed and a delegate is notified when they expire.
 * Implements ITickHandler so it can be registered with TickManager.
 *
 * <p>Backed by a {@link LinkedHashMap} to avoid a private inner class
 * which triggers a ModuleClassLoader bug in securejarhandler when accessed
 * via invokedynamic lambdas.</p>
 *
 * @param <V> the element type
 */
public class TimeoutList<V> implements ITickHandler, Iterable<V> {

    @Nullable
    private final TimeoutDelegate<V> delegate;
    private final EnumSet<TickEvent.Type> tickTypes;

    // Key = element, Value = remaining ticks (0 = expired this tick)
    private final LinkedHashMap<V, Integer> tickEntries = new LinkedHashMap<>();

    public TimeoutList(@Nullable TimeoutDelegate<V> delegate, TickEvent.Type... types) {
        this.delegate = delegate;
        this.tickTypes = EnumSet.noneOf(TickEvent.Type.class);
        for (TickEvent.Type type : types) {
            if (type != null) {
                this.tickTypes.add(type);
            }
        }
    }

    public void add(@Nonnull V value) {
        this.add(0, value);
    }

    public void add(int timeout, @Nonnull V value) {
        tickEntries.put(value, timeout);
    }

    public boolean setTimeout(int timeout, @Nonnull V value) {
        if (tickEntries.containsKey(value)) {
            tickEntries.put(value, timeout);
            return true;
        }
        return false;
    }

    public boolean setOrAddTimeout(int timeout, @Nonnull V value) {
        if (!contains(value)) {
            add(timeout, value);
            return true;
        } else {
            return setTimeout(timeout, value);
        }
    }

    public boolean contains(@Nullable V value) {
        return value != null && tickEntries.containsKey(value);
    }

    public boolean remove(@Nonnull V key) {
        return tickEntries.remove(key) != null;
    }

    public boolean removeIf(@Nonnull Predicate<V> test) {
        return tickEntries.keySet().removeIf(test);
    }

    public int getTimeout(@Nonnull V value) {
        return tickEntries.getOrDefault(value, -1);
    }

    public void addAll(@Nullable TimeoutList<V> entries) {
        if (entries == null) return;
        entries.tickEntries.forEach((v, timeout) -> setOrAddTimeout(timeout, v));
    }

    public boolean isEmpty() {
        return tickEntries.isEmpty();
    }

    @Override
    public void tick(@Nonnull TickEvent.Type type, @Nonnull Object... context) {
        Iterator<Map.Entry<V, Integer>> iterator = tickEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<V, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                if (delegate != null) {
                    delegate.onTimeout(entry.getKey());
                }
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    public void clear() {
        TimeoutDelegate<V> d = delegate;
        if (d != null) {
            tickEntries.keySet().forEach(d::onTimeout);
        }
        tickEntries.clear();
    }

    @Override
    @Nonnull
    public Iterator<V> iterator() {
        return tickEntries.keySet().iterator();
    }

    @Override
    @Nonnull
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return tickTypes;
    }

    @Override
    public boolean canFire(@Nonnull TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    @Nonnull
    public String getName() {
        return "TimeoutList";
    }

    /**
     * Callback for when an element's timeout expires.
     *
     * @param <V> the element type
     */
    public interface TimeoutDelegate<V> {
        void onTimeout(@Nonnull V object);
    }
}
