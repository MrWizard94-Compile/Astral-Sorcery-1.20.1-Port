package hellfirepvp.astralsorcery.common.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * Three-argument functional interface, analogous to BiFunction.
 *
 * @param <S> first argument type
 * @param <T> second argument type
 * @param <U> third argument type
 * @param <R> return type
 */
public interface TriFunction<S, T, U, R> {

    R apply(S s, T t, U u);

    default <V> TriFunction<S, T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (S s, T t, U u) -> after.apply(apply(s, t, u));
    }
}
