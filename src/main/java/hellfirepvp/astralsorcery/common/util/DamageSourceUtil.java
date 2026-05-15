package hellfirepvp.astralsorcery.common.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility for creating damage sources in the 1.20 data-driven damage type system.
 *
 * <p>1.16 -> 1.20 changes (MAJOR REWRITE):
 * In 1.16, DamageSource was created via constructor with string type and attributes
 * set programmatically (setFireDamage, setDamageBypassesArmor, etc).
 * In 1.20, damage types are data-driven via JSON in data/astralsorcery/damage_type/.
 * Properties like fire, bypass_armor, magic are defined in the JSON, not in code.
 * EntityDamageSource and IndirectEntityDamageSource were removed.
 * DamageSource constructor takes a Holder&lt;DamageType&gt; + optional entities.</p>
 */
public class DamageSourceUtil {

    /**
     * Creates a new DamageSource for the given damage type with no entity involvement.
     */
    @Nonnull
    public static DamageSource newType(@Nonnull Level level,
                                       @Nonnull ResourceKey<DamageType> damageType) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(damageType));
    }

    /**
     * Creates a new DamageSource with a direct entity source.
     */
    @Nonnull
    public static DamageSource withEntityDirect(@Nonnull Level level,
                                                @Nonnull ResourceKey<DamageType> damageType,
                                                @Nullable Entity source) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(damageType),
                source);
    }

    /**
     * Creates a new DamageSource with an indirect entity source (e.g., arrow shot by player).
     *
     * @param directSource  the direct entity causing damage (e.g., arrow)
     * @param causingEntity the true cause (e.g., the player who shot the arrow)
     */
    @Nonnull
    public static DamageSource withEntityIndirect(@Nonnull Level level,
                                                  @Nonnull ResourceKey<DamageType> damageType,
                                                  @Nullable Entity directSource,
                                                  @Nullable Entity causingEntity) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(damageType),
                directSource,
                causingEntity);
    }
}
