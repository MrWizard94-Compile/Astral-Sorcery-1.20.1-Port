package hellfirepvp.astralsorcery.common.util;

import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Stream wrapper for key-value pair operations using Minecraft's Tuple.
 * Provides typed key/value filtering, mapping, and collection operations
 * on top of a standard Stream of Tuples.
 *
 * <p>No 1.16 → 1.20 changes needed — net.minecraft.util.Tuple is stable.</p>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class MapStream<K, V> implements Stream<Tuple<K, V>> {

    @Nonnull
    private final Stream<Tuple<K, V>> decorated;

    private MapStream(@Nonnull Stream<Tuple<K, V>> decorated) {
        this.decorated = decorated;
    }

    @Nonnull
    public static <K, V> MapStream<K, V> of(@Nonnull Map<K, V> map) {
        return new MapStream<>(map.entrySet()
                .stream()
                .map(e -> new Tuple<>(e.getKey(), e.getValue())));
    }

    @Nonnull
    public static <K, V> MapStream<K, V> of(@Nonnull Collection<Tuple<K, V>> tplCollection) {
        return new MapStream<>(tplCollection.stream());
    }

    @Nonnull
    public static <K, V> MapStream<K, V> of(@Nonnull Stream<Tuple<K, V>> tplStream) {
        return new MapStream<>(tplStream);
    }

    @Nonnull
    public static <K, V> MapStream<K, V> ofKeys(@Nonnull Collection<K> collection,
                                                 @Nonnull Function<K, V> valueProvider) {
        return ofKeys(collection.stream(), valueProvider);
    }

    @Nonnull
    public static <K, V> MapStream<K, V> ofKeys(@Nonnull Stream<K> stream,
                                                 @Nonnull Function<K, V> valueProvider) {
        return new MapStream<>(stream.map(k -> new Tuple<>(k, valueProvider.apply(k))));
    }

    @Nonnull
    public static <K, V> MapStream<K, V> ofValues(@Nonnull Collection<V> collection,
                                                   @Nonnull Function<V, K> keyProvider) {
        return ofValues(collection.stream(), keyProvider);
    }

    @Nonnull
    public static <K, V> MapStream<K, V> ofValues(@Nonnull Stream<V> stream,
                                                   @Nonnull Function<V, K> keyProvider) {
        return new MapStream<>(stream.map(v -> new Tuple<>(keyProvider.apply(v), v)));
    }

    public static <K, V> void forEach(@Nonnull Map<K, V> map,
                                      @Nonnull BiConsumer<K, V> forEachFn) {
        of(map).forEach(tpl -> forEachFn.accept(tpl.getA(), tpl.getB()));
    }

    @Nonnull
    public Map<K, V> toMap() {
        return decorated.collect(Collectors.toMap(Tuple::getA, Tuple::getB));
    }

    @Nonnull
    public <R> List<R> toList(@Nonnull BiFunction<K, V, R> flatFunction) {
        return decorated
                .map(tpl -> flatFunction.apply(tpl.getA(), tpl.getB()))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<Tuple<K, V>> toTupleList() {
        return decorated.collect(Collectors.toList());
    }

    @Nonnull
    public <R> MapStream<K, R> mapValue(@Nonnull Function<V, R> valueMapper) {
        return of(decorated.map(tpl -> new Tuple<>(tpl.getA(), valueMapper.apply(tpl.getB()))));
    }

    @Nonnull
    public <R> MapStream<R, V> mapKey(@Nonnull Function<K, R> keyMapper) {
        return of(decorated.map(tpl -> new Tuple<>(keyMapper.apply(tpl.getA()), tpl.getB())));
    }

    @Nonnull
    public <R> Stream<R> flatten(@Nonnull BiFunction<K, V, R> flatFunction) {
        return decorated.map(tpl -> flatFunction.apply(tpl.getA(), tpl.getB()));
    }

    @Nonnull
    public MapStream<K, V> filterKey(@Nonnull Predicate<K> predicate) {
        return of(decorated.filter(tpl -> predicate.test(tpl.getA())));
    }

    @Nonnull
    public MapStream<K, V> filterValue(@Nonnull Predicate<V> predicate) {
        return of(decorated.filter(tpl -> predicate.test(tpl.getB())));
    }

    @Nonnull
    public Stream<V> valueStream() {
        return decorated.map(Tuple::getB);
    }

    @Nonnull
    public Stream<K> keyStream() {
        return decorated.map(Tuple::getA);
    }

    public void forEach(@Nonnull BiConsumer<K, V> forEachFn) {
        decorated.forEach(tpl -> forEachFn.accept(tpl.getA(), tpl.getB()));
    }

    // ---- Stream<Tuple<K, V>> delegation ----

    @Override
    public Stream<Tuple<K, V>> filter(Predicate<? super Tuple<K, V>> predicate) {
        return decorated.filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super Tuple<K, V>, ? extends R> mapper) {
        return decorated.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super Tuple<K, V>> mapper) {
        return decorated.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super Tuple<K, V>> mapper) {
        return decorated.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super Tuple<K, V>> mapper) {
        return decorated.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super Tuple<K, V>, ? extends Stream<? extends R>> mapper) {
        return decorated.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super Tuple<K, V>, ? extends IntStream> mapper) {
        return decorated.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super Tuple<K, V>, ? extends LongStream> mapper) {
        return decorated.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super Tuple<K, V>, ? extends DoubleStream> mapper) {
        return decorated.flatMapToDouble(mapper);
    }

    @Override
    public Stream<Tuple<K, V>> distinct() {
        return decorated.distinct();
    }

    @Override
    public Stream<Tuple<K, V>> sorted() {
        return decorated.sorted();
    }

    @Override
    public Stream<Tuple<K, V>> sorted(Comparator<? super Tuple<K, V>> comparator) {
        return decorated.sorted(comparator);
    }

    @Override
    public Stream<Tuple<K, V>> peek(Consumer<? super Tuple<K, V>> action) {
        return decorated.peek(action);
    }

    @Override
    public Stream<Tuple<K, V>> limit(long maxSize) {
        return decorated.limit(maxSize);
    }

    @Override
    public Stream<Tuple<K, V>> skip(long n) {
        return decorated.skip(n);
    }

    @Override
    public void forEach(Consumer<? super Tuple<K, V>> action) {
        decorated.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super Tuple<K, V>> action) {
        decorated.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return decorated.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return decorated.toArray(generator);
    }

    @Override
    public Tuple<K, V> reduce(Tuple<K, V> identity, BinaryOperator<Tuple<K, V>> accumulator) {
        return decorated.reduce(identity, accumulator);
    }

    @Override
    public Optional<Tuple<K, V>> reduce(BinaryOperator<Tuple<K, V>> accumulator) {
        return decorated.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super Tuple<K, V>, U> accumulator,
                        BinaryOperator<U> combiner) {
        return decorated.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Tuple<K, V>> accumulator,
                         BiConsumer<R, R> combiner) {
        return decorated.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super Tuple<K, V>, A, R> collector) {
        return decorated.collect(collector);
    }

    @Override
    public Optional<Tuple<K, V>> min(Comparator<? super Tuple<K, V>> comparator) {
        return decorated.min(comparator);
    }

    @Override
    public Optional<Tuple<K, V>> max(Comparator<? super Tuple<K, V>> comparator) {
        return decorated.max(comparator);
    }

    @Override
    public long count() {
        return decorated.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super Tuple<K, V>> predicate) {
        return decorated.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super Tuple<K, V>> predicate) {
        return decorated.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super Tuple<K, V>> predicate) {
        return decorated.noneMatch(predicate);
    }

    @Override
    public Optional<Tuple<K, V>> findFirst() {
        return decorated.findFirst();
    }

    @Override
    public Optional<Tuple<K, V>> findAny() {
        return decorated.findAny();
    }

    @Override
    public Iterator<Tuple<K, V>> iterator() {
        return decorated.iterator();
    }

    @Override
    public Spliterator<Tuple<K, V>> spliterator() {
        return decorated.spliterator();
    }

    @Override
    public boolean isParallel() {
        return decorated.isParallel();
    }

    @Override
    public Stream<Tuple<K, V>> sequential() {
        return decorated.sequential();
    }

    @Override
    public Stream<Tuple<K, V>> parallel() {
        return decorated.parallel();
    }

    @Override
    public Stream<Tuple<K, V>> unordered() {
        return decorated.unordered();
    }

    @Override
    public Stream<Tuple<K, V>> onClose(Runnable closeHandler) {
        return decorated.onClose(closeHandler);
    }

    @Override
    public void close() {
        decorated.close();
    }
}
