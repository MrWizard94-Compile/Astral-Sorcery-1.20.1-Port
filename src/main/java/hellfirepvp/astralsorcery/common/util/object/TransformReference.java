package hellfirepvp.astralsorcery.common.util.object;

import java.util.function.Function;

/**
 * Holds a reference and a transformation function,
 * lazily computing the transformed value on access.
 *
 * @param <T> the source type
 * @param <R> the transformed type
 */
public class TransformReference<T, R> {

    private final T object;
    private final Function<T, R> transform;

    public TransformReference(T object, Function<T, R> transform) {
        this.object = object;
        this.transform = transform;
    }

    public T getReference() {
        return object;
    }

    public R getValue() {
        return transform.apply(object);
    }
}
