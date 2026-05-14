package hellfirepvp.astralsorcery.common.util.order;

import com.google.common.collect.Lists;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Topological sort for OrderSortable elements using Forge's sort utility.
 *
 * @param <T> the element type
 */
@SuppressWarnings("UnstableApiUsage") // Guava MutableGraph is @Beta
public class DependencySorter<T> {

    private final MutableGraph<T> graph;

    private DependencySorter(Collection<T> itemsToSort) {
        this.graph = GraphBuilder.directed().build();
        itemsToSort.forEach(this.graph::addNode);
    }

    public static <T extends OrderSortable> List<T> getSorted(Collection<T> itemsToSort) {
        return getSorted(itemsToSort, OrderSortable::isBefore, OrderSortable::isAfter);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getSorted(Collection<T> itemsToSort,
                                         BiPredicate<T, Object> isBefore,
                                         BiPredicate<T, Object> isAfter) {
        DependencySorter<T> sorter = new DependencySorter<>(itemsToSort);
        for (T item : itemsToSort) {
            List<T> before = Lists.newArrayList();
            List<T> after = Lists.newArrayList();
            for (Object otherItem : itemsToSort) {
                if (item == otherItem) {
                    continue;
                }

                if (isBefore.test(item, otherItem)) {
                    before.add((T) otherItem);
                }
                if (isAfter.test(item, otherItem)) {
                    after.add((T) otherItem);
                }
            }
            sorter.setOrdering(item, before, after);
        }
        return sorter.getSorted();
    }

    private void setOrdering(T node, Collection<T> before, Collection<T> after) {
        before.forEach(n -> this.graph.putEdge(node, n));
        after.forEach(n -> this.graph.putEdge(n, node));
    }

    private List<T> getSorted() {
        return TopologicalSort.topologicalSort(this.graph, null);
    }
}
