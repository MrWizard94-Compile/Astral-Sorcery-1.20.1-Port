package hellfirepvp.astralsorcery.common.perk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * Holds min/max clamp ranges for custom perk attribute types.
 *
 * <p>Registered types are clamped in {@link hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper#computeValue}
 * so gameplay-critical custom attributes (crit chance, elemental resist, life steal, etc.)
 * cannot exceed safe bounds even when many perks are stacked.</p>
 *
 * <p>Vanilla-backed attribute types (armor, speed, etc.) are not clamped here —
 * they go through vanilla's attribute pipeline which has its own inherent limits.</p>
 */
public final class PerkAttributeLimiter {

    private static final Map<ResourceLocation, LimitRange> LIMITS = new HashMap<>();

    private PerkAttributeLimiter() {}

    /**
     * Registers a min/max clamp range for a perk attribute type.
     *
     * @param typeKey the attribute type key (from {@link hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType#getKey()})
     * @param min     supplier for the minimum allowed value
     * @param max     supplier for the maximum allowed value
     */
    public static void registerLimit(@Nonnull ResourceLocation typeKey,
                                     @Nonnull DoubleSupplier min,
                                     @Nonnull DoubleSupplier max) {
        LIMITS.put(typeKey, new LimitRange(min, max));
    }

    /**
     * Registers a min/max clamp range with fixed values.
     *
     * @param typeKey the attribute type key
     * @param min     fixed minimum
     * @param max     fixed maximum
     */
    public static void registerLimit(@Nonnull ResourceLocation typeKey, double min, double max) {
        registerLimit(typeKey, () -> min, () -> max);
    }

    /**
     * Returns whether a limit is registered for the given type.
     */
    public static boolean hasLimit(@Nonnull ResourceLocation typeKey) {
        return LIMITS.containsKey(typeKey);
    }

    /**
     * Clamps a computed value to the registered range for the attribute type.
     * If no limit is registered, returns the value unchanged.
     *
     * @param typeKey the attribute type key
     * @param value   the computed value to clamp
     * @return the clamped value
     */
    public static double clamp(@Nonnull ResourceLocation typeKey, double value) {
        LimitRange range = LIMITS.get(typeKey);
        if (range == null) {
            return value;
        }
        return Mth.clamp(value, range.min.getAsDouble(), range.max.getAsDouble());
    }

    /**
     * Returns the registered maximum for a type, or {@link Double#MAX_VALUE} if none.
     */
    public static double getMax(@Nonnull ResourceLocation typeKey) {
        LimitRange range = LIMITS.get(typeKey);
        return range == null ? Double.MAX_VALUE : range.max.getAsDouble();
    }

    /**
     * Returns the registered minimum for a type, or {@link Double#MIN_VALUE} if none.
     */
    public static double getMin(@Nonnull ResourceLocation typeKey) {
        LimitRange range = LIMITS.get(typeKey);
        return range == null ? Double.MIN_VALUE : range.min.getAsDouble();
    }

    private record LimitRange(DoubleSupplier min, DoubleSupplier max) {}
}
