/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.lib.DamageTypesAS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Convenience factory for Astral Sorcery damage sources.
 * Each method creates a {@link DamageSource} by looking up the
 * corresponding {@link DamageTypesAS} key in the data-driven registry.
 */
public final class DamageSourceAS {

    private DamageSourceAS() {}

    @Nonnull
    public static DamageSource stellar(@Nonnull Level level) {
        return create(level, DamageTypesAS.STELLAR);
    }

    @Nonnull
    public static DamageSource constellation(@Nonnull Level level) {
        return create(level, DamageTypesAS.CONSTELLATION);
    }

    @Nonnull
    public static DamageSource holyStarlight(@Nonnull Level level) {
        return create(level, DamageTypesAS.HOLY_STARLIGHT);
    }

    @Nonnull
    public static DamageSource bleed(@Nonnull Level level) {
        return create(level, DamageTypesAS.BLEED);
    }

    @Nonnull
    private static DamageSource create(@Nonnull Level level, @Nonnull ResourceKey<DamageType> key) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key)
        );
    }
}
