package hellfirepvp.astralsorcery.common.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Utility methods for dealing damage to entities.
 *
 * <p>1.16 -> 1.20 changes:
 * attackEntityFrom -> hurt,
 * hurtResistantTime -> invulnerableTime,
 * DamageSource system is now data-driven (see DamageSourceUtil)</p>
 */
public class DamageUtil {

    public static boolean attackEntityFrom(@Nonnull Entity attacked,
                                            @Nonnull DamageSource type,
                                            float amount) {
        return attacked.hurt(type, amount);
    }

    /**
     * Temporarily resets the entity's invulnerability time before applying damage.
     * Allows multiple damage applications in rapid succession (shotgun effect).
     */
    public static <T extends LivingEntity> void shotgunAttack(@Nonnull T targeted,
                                                               @Nonnull Consumer<T> fn) {
        int hurtTime = targeted.invulnerableTime;
        targeted.invulnerableTime = 0;
        try {
            fn.accept(targeted);
        } finally {
            targeted.invulnerableTime = hurtTime;
        }
    }
}
