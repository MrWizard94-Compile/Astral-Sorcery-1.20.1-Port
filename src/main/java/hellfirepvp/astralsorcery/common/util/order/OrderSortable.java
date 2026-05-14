package hellfirepvp.astralsorcery.common.util.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base class for elements that declare ordering constraints
 * (before/after relationships) for topological sorting.
 */
public abstract class OrderSortable {

    private final List<Object> before = new ArrayList<>();
    private final List<Object> after = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public <E extends OrderSortable> E setBefore(Object... other) {
        return this.setBefore(Arrays.asList(other));
    }

    @SuppressWarnings("unchecked")
    public <E extends OrderSortable> E setBefore(Collection<Object> other) {
        this.before.addAll(other);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends OrderSortable> E setAfter(Object... other) {
        return this.setAfter(Arrays.asList(other));
    }

    @SuppressWarnings("unchecked")
    public <E extends OrderSortable> E setAfter(Collection<Object> other) {
        this.after.addAll(other);
        return (E) this;
    }

    public boolean isBefore(Object other) {
        return this.before.contains(other);
    }

    public boolean isAfter(Object other) {
        return this.after.contains(other);
    }
}
