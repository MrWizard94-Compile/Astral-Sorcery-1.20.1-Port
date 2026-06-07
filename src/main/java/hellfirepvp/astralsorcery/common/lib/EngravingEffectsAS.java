package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffectRegistry;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * All constellation engraving effects, registered into {@link EngravingEffectRegistry}.
 * Called during FMLCommonSetupEvent after {@link ConstellationsAS#init()} has run.
 *
 * <p>1.16 → 1.20 changes:
 * EnchantmentType → EnchantmentCategory;
 * Effects → MobEffects;
 * effect field names updated to match port's PerkAttributeTypesAS;
 * ModifierEffects whose attribute type is not yet in PerkAttributeTypesAS are omitted;
 * Forge registry replaced by EngravingEffectRegistry map.</p>
 */
public final class EngravingEffectsAS {

    private EngravingEffectsAS() {}

    public static void init() {
        // Major constellations
        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.AEVITAS.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.REGENERATION, 0, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.MENDING, 1, 1))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH,
                        ModifierType.ADDITION, 1F, 3F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR)
                        .formatResultAsInteger())
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL,
                        ModifierType.ADDITION, 2F, 4F)
                        .addApplicableCategory(EnchantmentCategory.WEAPON)
                        .addApplicableCategory(EnchantmentCategory.BOW)
                        .addApplicableCategory(EnchantmentCategory.CROSSBOW)
                        .addApplicableCategory(EnchantmentCategory.TRIDENT)
                        .formatResultAsInteger()));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.ARMARA.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DAMAGE_RESISTANCE, 0, 0))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.ALL_DAMAGE_PROTECTION, 2, 5))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_ARMOR,
                        ModifierType.STACKING_MULTIPLY, 1.05F, 1.1F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.DISCIDIA.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DAMAGE_BOOST, 0, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.SHARPNESS, 3, 7))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.POWER_ARROWS, 3, 7))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.CHANNELING, 1, 1)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE,
                        ModifierType.ADDITION, 3F, 6F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE,
                        ModifierType.STACKING_MULTIPLY, 1.05F, 1.2F)
                        .addApplicableCategory(EnchantmentCategory.WEAPON)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.EVORSIO.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DIG_SPEED, 1, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.BLOCK_EFFICIENCY, 3, 6))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED,
                        ModifierType.ADDED_MULTIPLY, 0.1F, 0.25F)
                        .addApplicableCategory(EnchantmentCategory.DIGGER)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.VICIO.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.MOVEMENT_SPEED, 1, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FALL_PROTECTION, 1, 4))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.RIPTIDE, 1, 5))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED,
                        ModifierType.ADDED_MULTIPLY, 0.15F, 0.25F)
                        .addApplicableCategory(EnchantmentCategory.WEAPON)
                        .addApplicableCategory(EnchantmentCategory.DIGGER)
                        .addApplicableCategory(EnchantmentCategory.BOW)
                        .addApplicableCategory(EnchantmentCategory.CROSSBOW)
                        .addApplicableCategory(EnchantmentCategory.TRIDENT))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED,
                        ModifierType.STACKING_MULTIPLY, 1.05F, 1.1F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR_FEET)));

        // Weak constellations
        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.BOOTES.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.SATURATION, 1, 5))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.SILK_TOUCH, 1, 1))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.LOYALTY, 2, 4))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH,
                        ModifierType.ADDITION, 1F, 2F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR)
                        .formatResultAsInteger())
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_REACH,
                        ModifierType.ADDED_MULTIPLY, 0.05F, 0.1F)
                        .addApplicableCategory(EnchantmentCategory.DIGGER)
                        .addApplicableCategory(EnchantmentCategory.WEAPON)
                        .addApplicableCategory(EnchantmentCategory.TRIDENT)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.FORNAX.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.FIRE_RESISTANCE, 0, 0))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FIRE_ASPECT, 1, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FLAMING_ARROWS, 1, 2))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> EnchantmentsAS.SCORCHING_HEAT.get(), 1, 1))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE,
                        ModifierType.ADDED_MULTIPLY, 0.1F, 0.2F)
                        .addApplicableCategory(EnchantmentCategory.WEAPON)
                        .addApplicableCategory(EnchantmentCategory.BOW)
                        .addApplicableCategory(EnchantmentCategory.CROSSBOW)
                        .addApplicableCategory(EnchantmentCategory.TRIDENT))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER,
                        ModifierType.ADDED_MULTIPLY, 0.1F, 0.2F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.HOROLOGIUM.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.LUCK, 3, 5))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.MOVEMENT_SPEED, 1, 2))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DIG_SPEED, 2, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.BLOCK_FORTUNE, 4, 6))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.MOB_LOOTING, 3, 6))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE,
                        ModifierType.STACKING_MULTIPLY, 1.1F, 1.2F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR_HEAD))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT,
                        ModifierType.STACKING_MULTIPLY, 1.04F, 1.08F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR_FEET)
                        .addApplicableCategory(EnchantmentCategory.ARMOR_CHEST)
                        .addApplicableCategory(EnchantmentCategory.ARMOR_LEGS)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.LUCERNA.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.NIGHT_VISION, 0, 0))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> EnchantmentsAS.NIGHT_VISION.get(), 1, 1))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.RESPIRATION, 1, 1)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.MINERALIS.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DIG_SPEED, 0, 2))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.BLOCK_FORTUNE, 1, 3))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED,
                        ModifierType.ADDED_MULTIPLY, 0.1F, 0.2F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.OCTANS.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.WATER_BREATHING, 1, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.RESPIRATION, 2, 4)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.PELOTRIO.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.ABSORPTION, 1, 4))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.REGENERATION, 2, 4))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FISHING_SPEED, 3, 5))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.INFINITY_ARROWS, 1, 1)));

        // Minor constellations
        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.ALCARA.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.LUCK, 3, 6))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.INVISIBILITY, 0, 1))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.MOVEMENT_SPEED, 1, 2))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.SWEEPING_EDGE, 3, 6))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FISHING_SPEED, 2, 5)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FISHING_LUCK, 3, 6)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.SILK_TOUCH, 1, 1))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.QUICK_CHARGE, 1, 4)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.GELU.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DAMAGE_RESISTANCE, 1, 2))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.FIRE_RESISTANCE, 0, 0))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.MOVEMENT_SLOWDOWN, 0, 1))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FROST_WALKER, 1, 1))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FALL_PROTECTION, 1, 1))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.UNBREAKING, 2, 4))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_REACH,
                        ModifierType.ADDITION, 1F, 2F)
                        .addApplicableCategory(EnchantmentCategory.DIGGER)
                        .formatResultAsInteger())
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER,
                        ModifierType.STACKING_MULTIPLY, 1.1F, 1.2F)
                        .addApplicableCategory(EnchantmentCategory.WEAPON)
                        .addApplicableCategory(EnchantmentCategory.TRIDENT)
                        .addApplicableCategory(EnchantmentCategory.BOW)
                        .addApplicableCategory(EnchantmentCategory.CROSSBOW)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.ULTERIA.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.ABSORPTION, 0, 4))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.REGENERATION, 1, 3))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.WEAKNESS, 1, 2))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.UNBREAKING, 2, 3)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.FIRE_PROTECTION, 4, 6)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.BLAST_PROTECTION, 4, 6)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.PROJECTILE_PROTECTION, 4, 6)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.MULTISHOT, 1, 1))
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST,
                        ModifierType.ADDED_MULTIPLY, 0.05F, 0.15F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR)));

        EngravingEffectRegistry.register(new EngravingEffect(ConstellationsAS.VORUX.getRegistryName())
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DAMAGE_BOOST, 2, 3))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DAMAGE_RESISTANCE, 0, 1))
                .addEffect(new EngravingEffect.PotionEffect(() -> MobEffects.DIG_SLOWDOWN, 1, 3))
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.SMITE, 4, 7)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.BANE_OF_ARTHROPODS, 4, 7)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.SHARPNESS, 3, 4)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.POWER_ARROWS, 3, 4)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.CHANNELING, 3, 4)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.EnchantmentEffect(() -> Enchantments.PIERCING, 3, 6)
                        .setIgnoreCompatibility())
                .addEffect(new EngravingEffect.ModifierEffect(() -> PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED,
                        ModifierType.ADDED_MULTIPLY, 0.05F, 0.1F)
                        .addApplicableCategory(EnchantmentCategory.ARMOR)));
    }
}
