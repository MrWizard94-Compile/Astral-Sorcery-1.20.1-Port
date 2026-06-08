package hellfirepvp.astralsorcery.common.util;

import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Functional combinator utilities: partial application and null-returning wrappers.
 */
public class FunctionUtils {

    @Nonnull
    public static <T> Runnable apply(@Nonnull Consumer<T> func, @Nonnull Supplier<T> supply) {
        return () -> func.accept(supply.get());
    }

    @Nonnull
    public static <T, U> Consumer<T> apply(@Nonnull BiConsumer<T, U> func,
                                           @Nonnull Supplier<U> supply) {
        return (t) -> func.accept(t, supply.get());
    }

    @Nonnull
    public static <T, U, V> BiConsumer<T, U> apply(@Nonnull TriConsumer<T, U, V> func,
                                                    @Nonnull Supplier<V> supply) {
        return (t, u) -> func.accept(t, u, supply.get());
    }

    @Nonnull
    public static <T, R> Supplier<R> apply(@Nonnull Function<T, R> func,
                                           @Nonnull Supplier<T> supply) {
        return () -> func.apply(supply.get());
    }

    @Nonnull
    public static <T, P, R> Function<P, R> apply(@Nonnull BiFunction<T, P, R> func,
                                                  @Nonnull Supplier<T> supply) {
        return p -> func.apply(supply.get(), p);
    }

    @Nonnull
    public static <T, V> Function<T, V> nullFunction(@Nonnull Runnable run) {
        return nullFunction((v) -> run.run());
    }

    @Nonnull
    public static <T, V> Function<T, V> nullFunction(@Nonnull Consumer<T> run) {
        return (t) -> {
            run.accept(t);
            return null;
        };
    }

    @Nonnull
    public static <T> Supplier<T> nullSupplier(@Nonnull Runnable run) {
        return () -> {
            run.run();
            return null;
        };
    }
}
