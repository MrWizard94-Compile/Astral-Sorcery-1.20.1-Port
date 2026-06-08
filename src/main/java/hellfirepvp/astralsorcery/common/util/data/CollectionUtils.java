package hellfirepvp.astralsorcery.common.util.data;

import hellfirepvp.astralsorcery.common.util.MapStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Collection transformation, search, copy, and comparison utilities.
 */
public class CollectionUtils {

    @Nonnull
    public static <T, V> List<V> transformList(@Nonnull List<T> list,
                                               @Nonnull Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    @Nonnull
    public static <T, V> Set<V> transformSet(@Nonnull Set<T> set,
                                             @Nonnull Function<T, V> map) {
        return set.stream().map(map).collect(Collectors.toSet());
    }

    @Nonnull
    public static <T, V> Collection<V> transformCollection(@Nonnull Collection<T> list,
                                                           @Nonnull Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    @Nonnull
    public static <K, V, N> Map<K, N> remap(@Nonnull Map<K, V> map,
                                            @Nonnull Function<V, N> remapFct) {
        return MapStream.of(map).mapValue(remapFct).toMap();
    }

    public static <T> void mergeList(@Nonnull Collection<T> src, @Nonnull List<T> dst) {
        for (T element : src) {
            if (!dst.contains(element)) {
                dst.add(element);
            }
        }
    }

    public static <T> void cutList(@Nonnull Collection<? extends T> toRemove,
                                   @Nonnull List<T> from) {
        for (T element : toRemove) {
            from.remove(element);
        }
    }

    @Nonnull
    public static <T> List<T> copyList(@Nonnull List<T> list) {
        return new ArrayList<>(list);
    }

    @Nonnull
    public static <T> Set<T> copySet(@Nonnull Set<T> set) {
        return new HashSet<>(set);
    }

    @Nullable
    public static <T> T iterativeSearch(@Nonnull Collection<T> collection,
                                        @Nonnull Predicate<T> matchingFct) {
        for (T element : collection) {
            if (matchingFct.test(element)) {
                return element;
            }
        }
        return null;
    }

    public static <T> boolean contains(@Nonnull Collection<T> collection,
                                       @Nonnull Predicate<T> matchingFct) {
        return iterativeSearch(collection, matchingFct) != null;
    }

    public static <T> boolean matchesAny(@Nonnull T element,
                                         @Nonnull Collection<Predicate<T>> tests) {
        for (Predicate<T> test : tests) {
            if (test.test(element)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static <T, V extends Comparable<V>> V getMaxEntry(@Nonnull Collection<T> elements,
                                                             @Nonnull Function<T, V> valueFunction) {
        return getMaxEntry(transformCollection(elements, valueFunction));
    }

    @Nullable
    public static <T extends Comparable<T>> T getMaxEntry(@Nonnull Collection<T> elements) {
        T maxElement = null;
        for (T element : elements) {
            if (maxElement == null || maxElement.compareTo(element) < 0) {
                maxElement = element;
            }
        }
        return maxElement;
    }

    @Nullable
    public static <T, V extends Comparable<V>> V getMinEntry(@Nonnull Collection<T> elements,
                                                             @Nonnull Function<T, V> valueFunction) {
        return getMinEntry(transformCollection(elements, valueFunction));
    }

    @Nullable
    public static <T extends Comparable<T>> T getMinEntry(@Nonnull Collection<T> elements) {
        T minElement = null;
        for (T element : elements) {
            if (minElement == null || minElement.compareTo(element) > 0) {
                minElement = element;
            }
        }
        return minElement;
    }
}
