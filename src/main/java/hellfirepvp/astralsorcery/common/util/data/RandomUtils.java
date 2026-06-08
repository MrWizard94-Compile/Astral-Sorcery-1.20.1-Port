package hellfirepvp.astralsorcery.common.util.data;

import com.google.common.collect.Iterables;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Random selection and enum-index utilities.
 */
public class RandomUtils {

    private static final Logger LOG = LogManager.getLogger();

    @Nullable
    public static <T> T getRandomEntry(@Nullable Collection<T> collection,
                                       @Nonnull Random rand) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int index = rand.nextInt(collection.size());
        return Iterables.get(collection, index);
    }

    @Nullable
    public static <T> T getRandomEntry(@Nullable T[] array, @Nonnull Random rand) {
        if (array == null || array.length <= 0) {
            return null;
        }
        return array[rand.nextInt(array.length)];
    }

    @Nonnull
    public static <T> T getEnumEntry(@Nonnull Class<T> enumClazz, int index) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException(
                    "Called getEnumEntry on class " + enumClazz.getName() + " which isn't an enum.");
        }
        T[] values = enumClazz.getEnumConstants();
        if (values.length == 0) {
            throw new IllegalArgumentException(enumClazz.getName() + " has no enum constants.");
        }
        return values[Mth.clamp(index, 0, values.length - 1)];
    }

    /**
     * Weighted random selection. Replaces 1.16 WeightedRandom.getRandomItem to avoid
     * WeightedEntry/RandomSource API changes in 1.20.
     */
    @Nullable
    public static <T> T getWeightedRandomEntry(@Nonnull Collection<T> list,
                                               @Nonnull Random rand,
                                               @Nonnull Function<T, Integer> getWeightFunction) {
        if (list.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (T e : list) {
            totalWeight += getWeightFunction.apply(e);
        }
        if (totalWeight <= 0) {
            return null;
        }
        int roll = rand.nextInt(totalWeight);
        for (T e : list) {
            roll -= getWeightFunction.apply(e);
            if (roll < 0) {
                return e;
            }
        }
        return null;
    }

    @SafeVarargs
    @Nullable
    public static <T> T eitherOf(@Nonnull Random r, @Nonnull T... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)];
    }

    @SafeVarargs
    @Nullable
    public static <T> T eitherOf(@Nonnull Random r, @Nonnull Supplier<T>... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)].get();
    }

    @SafeVarargs
    @Nonnull
    public static <T> Optional<T> tryMultiple(@Nonnull Supplier<T>... suppliers) {
        for (Supplier<T> supplier : suppliers) {
            try {
                return Optional.ofNullable(supplier.get());
            } catch (Exception exc) {
                LOG.error("tryMultiple failed for one supplier", exc);
            }
        }
        return Optional.empty();
    }
}
