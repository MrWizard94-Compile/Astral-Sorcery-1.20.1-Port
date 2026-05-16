/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

/**
 * All Astral Sorcery damage type resource keys.
 * Actual damage type properties are defined in data-driven JSON files
 * under {@code data/astralsorcery/damage_type/}.
 *
 * <p>1.16 -> 1.20 changes:
 * In 1.16, damage sources were constructed via {@code new DamageSource(name)}
 * with programmatic attributes. In 1.20, damage types are data-driven
 * (JSON files) and accessed via registry lookup with ResourceKey.</p>
 */
public final class DamageTypesAS {

    private DamageTypesAS() {}

    /**
     * Stellar damage — dealt by starlight overcharge and collector crystal beams.
     * Bypasses armor, is classified as magic damage.
     */
    public static final ResourceKey<DamageType> STELLAR =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(AstralSorcery.MODID, "stellar"));

    /**
     * Constellation damage — dealt by certain constellation effects
     * (Discidia focus, ritual offensive effects).
     * Standard physical damage with magic flag.
     */
    public static final ResourceKey<DamageType> CONSTELLATION =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(AstralSorcery.MODID, "constellation"));

    /**
     * Holy starlight damage — deals extra damage to undead entities.
     * Used by Lucerna and Aevitas constellation effects.
     * Bypasses enchantment protection.
     */
    public static final ResourceKey<DamageType> HOLY_STARLIGHT =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(AstralSorcery.MODID, "holy_starlight"));

    /**
     * Bleed damage — applied by certain perk effects (Discidia bleeds).
     * Bypasses armor, not classified as magic.
     */
    public static final ResourceKey<DamageType> BLEED =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(AstralSorcery.MODID, "bleed"));
}
