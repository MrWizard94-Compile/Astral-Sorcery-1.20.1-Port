package hellfirepvp.astralsorcery.common.util.tick;

import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A list of elements with per-element tick-based timeouts.
 * Elements are removed and a delegate is notified when they expire.
 * Implements ITickHandler so it can be registered with TickManager.
 *
 * @param <V> the element type
 */
public class TimeoutList<V> implements ITickHandler, Iterable<V> {

    @Nullable
    private final TimeoutDelegate<V> delegate;
    private final EnumSet<TickEvent.Type> tickTypes;

    private final List<TimeoutEntry<V>> tickEntries = new LinkedList<>();

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
        this.tickEntries.add(new TimeoutEntry<>(timeout, value));
    }

    public boolean setTimeout(int timeout, @Nonnull V value) {
        for (TimeoutEntry<V> entry : tickEntries) {
            if (entry.value.equals(value)) {
                entry.timeout = timeout;
                return true;
            }
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
        if (value == null) return false;
        for (TimeoutEntry<V> entry : tickEntries) {
            if (entry.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean remove(@Nonnull V key) {
        return this.removeIf(key::equals);
    }

    public boolean removeIf(@Nonnull Predicate<V> test) {
        return this.tickEntries.removeIf(e -> test.test(e.value));
    }

    public int getTimeout(@Nonnull V value) {
        for (TimeoutEntry<V> entry : tickEntries) {
            if (entry.value.equals(value)) {
                return entry.timeout;
            }
        }
        return -1;
    }

    public void addAll(@Nullable TimeoutList<V> entries) {
        if (entries == null) return;
        for (TimeoutEntry<V> entry : entries.tickEntries) {
            setOrAddTimeout(entry.timeout, entry.value);
        }
    }

    public boolean isEmpty() {
        return tickEntries.isEmpty();
    }

    @Override
    public void tick(@Nonnull TickEvent.Type type, @Nonnull Object... context) {
        Iterator<TimeoutEntry<V>> iterator = tickEntries.iterator();
        while (iterator.hasNext()) {
            TimeoutEntry<V> entry = iterator.next();
            entry.timeout--;
            if (entry.timeout <= 0) {
                if (delegate != null) {
                    delegate.onTimeout(entry.value);
                }
                iterator.remove();
            }
        }
    }

    public void clear() {
        if (delegate != null) {
            tickEntries.forEach(entry -> delegate.onTimeout(entry.value));
        }
        tickEntries.clear();
    }

    @Override
    @Nonnull
    public Iterator<V> iterator() {
        Iterator<TimeoutEntry<V>> entryIterator = tickEntries.iterator();
        return new Iterator<V>() {
            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public V next() {
                return entryIterator.next().value;
            }

            @Override
            public void remove() {
                entryIterator.remove();
            }
        };
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

    private static class TimeoutEntry<V> {
        @Nonnull
        private final V value;
        private int timeout;

        private TimeoutEntry(int timeout, @Nonnull V value) {
            this.timeout = timeout;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeoutEntry<?> that = (TimeoutEntry<?>) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
