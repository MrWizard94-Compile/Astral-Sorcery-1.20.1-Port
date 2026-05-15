package hellfirepvp.astralsorcery.common.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper for applying and querying potion/mob effects.
 * Used extensively by constellation effects and perk modifiers.
 *
 * <p>1.16 -> 1.20 changes:
 * EffectInstance -> MobEffectInstance,
 * Effect -> MobEffect,
 * entity.getActivePotionEffect -> entity.getEffect,
 * entity.addPotionEffect -> entity.addEffect,
 * entity.removePotionEffect -> entity.removeEffect,
 * effect.getPotion() -> effect.getEffect()</p>
 */
public class EffectHelper {

    private EffectHelper() {}

    /**
     * Apply a potion effect to an entity, optionally replacing an existing effect
     * only if the new one is stronger or longer.
     *
     * @param entity    the target entity
     * @param effect    the mob effect type
     * @param duration  duration in ticks
     * @param amplifier amplifier (0-based)
     * @param ambient   whether the effect is ambient (beacon-like)
     * @param visible   whether particles are visible
     */
    public static void applyEffect(@Nonnull LivingEntity entity,
                                   @Nonnull MobEffect effect,
                                   int duration,
                                   int amplifier,
                                   boolean ambient,
                                   boolean visible) {
        MobEffectInstance existing = entity.getEffect(effect);
        if (existing != null) {
            if (existing.getAmplifier() > amplifier) {
                return; // Don't downgrade
            }
            if (existing.getAmplifier() == amplifier && existing.getDuration() > duration) {
                return; // Don't shorten same-level effect
            }
        }
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, ambient, visible));
    }

    /**
     * Apply a non-ambient, visible effect.
     */
    public static void applyEffect(@Nonnull LivingEntity entity,
                                   @Nonnull MobEffect effect,
                                   int duration,
                                   int amplifier) {
        applyEffect(entity, effect, duration, amplifier, false, true);
    }

    /**
     * Remove a potion effect from an entity.
     *
     * @param entity the entity
     * @param effect the effect to remove
     * @return true if the effect was present and removed
     */
    public static boolean removeEffect(@Nonnull LivingEntity entity, @Nonnull MobEffect effect) {
        if (entity.hasEffect(effect)) {
            entity.removeEffect(effect);
            return true;
        }
        return false;
    }

    /**
     * Get the amplifier of an active effect on an entity.
     *
     * @param entity the entity
     * @param effect the effect type
     * @return the amplifier (0-based), or -1 if not present
     */
    public static int getEffectAmplifier(@Nonnull LivingEntity entity, @Nonnull MobEffect effect) {
        MobEffectInstance instance = entity.getEffect(effect);
        return instance != null ? instance.getAmplifier() : -1;
    }

    /**
     * Check if an entity has an active effect at or above the given amplifier.
     *
     * @param entity       the entity
     * @param effect       the effect type
     * @param minAmplifier minimum amplifier required
     * @return true if the entity has the effect at or above the given level
     */
    public static boolean hasEffectAtLevel(@Nonnull LivingEntity entity,
                                           @Nonnull MobEffect effect,
                                           int minAmplifier) {
        MobEffectInstance instance = entity.getEffect(effect);
        return instance != null && instance.getAmplifier() >= minAmplifier;
    }
}
