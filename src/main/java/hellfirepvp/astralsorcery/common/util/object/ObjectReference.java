package hellfirepvp.astralsorcery.common.util.object;

import java.util.Objects;

/**
 * Mutable single-object holder. Used where lambdas need a mutable reference.
 *
 * @param <T> the held type
 */
public class ObjectReference<T> {

    private T object;

    public ObjectReference(T object) {
        this.object = object;
    }

    public ObjectReference() {}

    public T get() {
        return object;
    }

    public void set(T object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectReference<?> that = (ObjectReference<?>) o;
        return Objects.equals(object, that.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object);
    }
}
