/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.util.tick;

import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Manages a set of {@link TimeoutList} instances keyed by a group key {@code K}.
 * Each list holds typed values {@code V} with individual tick timeouts.
 * Implements {@link ITickHandler} so it can be registered with {@link TickManager}.
 *
 * <p>1.16 → 1.20: replaced ObserverLib ITickHandler with port's own interface.</p>
 *
 * @param <K> the group key type (e.g. player UUID)
 * @param <V> the value type stored in each list (e.g. perk ResourceLocation)
 */
public class TimeoutListContainer<K, V> implements ITickHandler {

    private final EnumSet<TickEvent.Type> tickTypes;
    @Nullable
    private final ContainerTimeoutDelegate<K, V> delegate;
    private final Map<K, TimeoutList<V>> map = new HashMap<>();

    public TimeoutListContainer(@Nullable ContainerTimeoutDelegate<K, V> delegate,
                                 TickEvent.Type... types) {
        this.delegate = delegate;
        this.tickTypes = EnumSet.noneOf(TickEvent.Type.class);
        for (TickEvent.Type t : types) {
            if (t != null) tickTypes.add(t);
        }
    }

    public boolean hasList(@Nonnull K key) {
        return map.containsKey(key);
    }

    @Nullable
    public TimeoutList<V> removeList(@Nonnull K key) {
        TimeoutList<V> list = map.remove(key);
        if (list != null && delegate != null) {
            list.forEach(v -> delegate.onContainerTimeout(key, v));
        }
        return list;
    }

    public boolean removeList(@Nonnull Predicate<V> valueTest) {
        boolean removed = false;
        for (Map.Entry<K, TimeoutList<V>> entry : map.entrySet()) {
            Iterator<V> it = entry.getValue().iterator();
            while (it.hasNext()) {
                V v = it.next();
                if (valueTest.test(v)) {
                    if (delegate != null) delegate.onContainerTimeout(entry.getKey(), v);
                    it.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Nonnull
    public TimeoutList<V> getOrCreateList(@Nonnull K key) {
        return map.computeIfAbsent(key, k ->
                new TimeoutList<>(delegate == null ? null : v -> delegate.onContainerTimeout(k, v),
                        tickTypes.toArray(new TickEvent.Type[0])));
    }

    public void clear() {
        List<K> keys = new ArrayList<>(map.keySet());
        keys.forEach(this::removeList);
    }

    // =========================================================================
    // ITickHandler
    // =========================================================================

    @Override
    public void tick(@Nonnull TickEvent.Type type, @Nonnull Object... context) {
        Iterator<Map.Entry<K, TimeoutList<V>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, TimeoutList<V>> entry = it.next();
            entry.getValue().tick(type, context);
            if (entry.getValue().isEmpty()) {
                it.remove();
            }
        }
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
        return "TimeoutListContainer";
    }

    // =========================================================================
    // Delegate interface
    // =========================================================================

    public interface ContainerTimeoutDelegate<K, V> {
        void onContainerTimeout(K key, V timedOut);
    }
}
