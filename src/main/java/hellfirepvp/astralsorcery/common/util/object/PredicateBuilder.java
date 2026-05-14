package hellfirepvp.astralsorcery.common.util.object;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Builder for composing predicates via AND/OR chains.
 *
 * @param <T> the predicate input type
 */
public class PredicateBuilder<T> {

    private Predicate<T> predicate;

    private PredicateBuilder(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    public static <T> Predicate<T> joinOr(Collection<? extends Predicate<T>> collection) {
        PredicateBuilder<T> builder = startOr();
        collection.forEach(builder::or);
        return builder.build();
    }

    public static <T> Predicate<T> joinAnd(Collection<? extends Predicate<T>> collection) {
        PredicateBuilder<T> builder = startAnd();
        collection.forEach(builder::and);
        return builder.build();
    }

    public static <T> PredicateBuilder<T> startOr() {
        return new PredicateBuilder<>(el -> false);
    }

    public static <T> PredicateBuilder<T> startAnd() {
        return new PredicateBuilder<>(el -> true);
    }

    public PredicateBuilder<T> or(Predicate<T> predicate) {
        this.predicate = this.predicate.or(predicate);
        return this;
    }

    public PredicateBuilder<T> and(Predicate<T> predicate) {
        this.predicate = this.predicate.and(predicate);
        return this;
    }

    public Predicate<T> build() {
        return this.predicate;
    }
}
