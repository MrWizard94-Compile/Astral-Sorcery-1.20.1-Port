package hellfirepvp.astralsorcery.common.util;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;

import javax.annotation.Nonnull;

/**
 * Typed weighted random entry, wrapping a value with a weight.
 *
 * <p>1.16 → 1.20 change: WeightedRandom.Item → WeightedEntry.IntrusiveBase.
 * Now uses Weight object instead of raw int.</p>
 *
 * @param <T> the value type
 */
public class WRItemObject<T> extends WeightedEntry.IntrusiveBase {

    private final T object;

    public WRItemObject(int itemWeightIn, @Nonnull T value) {
        super(Weight.of(itemWeightIn));
        this.object = value;
    }

    @Nonnull
    public T getValue() {
        return object;
    }
}
