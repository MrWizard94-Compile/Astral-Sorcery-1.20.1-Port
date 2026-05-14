package hellfirepvp.astralsorcery.common.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Replacement for ObserverLib's AlternatingSet.
 * A collection that supports iteration with concurrent removal via a boolean return
 * from the forEach callback. If the callback returns false, the element is removed
 * during iteration.
 *
 * <p>Used by EffectHandler to tick and prune particle sources in a single pass.</p>
 *
 * @param <T> the element type
 */
public class AlternatingSet<T> {

    private final List<T> elements = new ArrayList<>();

    public AlternatingSet() {}

    /**
     * Add an element to the set.
     *
     * @param element the element to add
     */
    public void add(@Nonnull T element) {
        this.elements.add(element);
    }

    /**
     * Remove a specific element.
     *
     * @param element the element to remove
     * @return true if the element was present and removed
     */
    public boolean remove(@Nonnull T element) {
        return this.elements.remove(element);
    }

    /**
     * Iterate over all elements. If the function returns false for an element,
     * that element is removed from the set during iteration.
     *
     * @param fn called for each element; return true to keep, false to remove
     */
    public void forEach(@Nonnull Function<T, Boolean> fn) {
        Iterator<T> it = this.elements.iterator();
        while (it.hasNext()) {
            T element = it.next();
            if (!fn.apply(element)) {
                it.remove();
            }
        }
    }

    /**
     * Clear all elements.
     */
    public void clear() {
        this.elements.clear();
    }

    /**
     * Check if the set is empty.
     *
     * @return true if no elements
     */
    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    /**
     * Get the number of elements.
     *
     * @return the count
     */
    public int size() {
        return this.elements.size();
    }

    /**
     * Standard iteration (without removal logic).
     * For the tick-and-prune pattern, use {@link #forEach(Function)} instead.
     *
     * @param action called for each element
     */
    public void forEachElement(@Nonnull Consumer<T> action) {
        this.elements.forEach(action);
    }
}
