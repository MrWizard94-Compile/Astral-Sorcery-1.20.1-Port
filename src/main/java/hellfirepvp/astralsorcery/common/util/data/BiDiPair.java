package hellfirepvp.astralsorcery.common.util.data;

import java.util.Objects;

/**
 * Bidirectional pair where equality is order-independent:
 * (A, B) equals (B, A).
 *
 * @param <K> left type
 * @param <V> right type
 */
public class BiDiPair<K, V> {

    private final K left;
    private final V right;

    public BiDiPair(K left, V right) {
        this.left = left;
        this.right = right;
    }

    public K getLeft() {
        return left;
    }

    public V getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiDiPair<?, ?> biDiPair = (BiDiPair<?, ?>) o;
        return (Objects.equals(left, biDiPair.left) && Objects.equals(right, biDiPair.right)) ||
                (Objects.equals(left, biDiPair.right) && Objects.equals(right, biDiPair.left));
    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }
}
