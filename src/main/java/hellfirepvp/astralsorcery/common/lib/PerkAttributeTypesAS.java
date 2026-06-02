/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeLimiter;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import javax.annotation.Nonnull;

/**
 * Registry constants for all built-in perk attribute types.
 *
 * <p>Types that map to vanilla {@link Attributes} are applied directly
 * to the player's attribute instances. Custom types (crit chance,
 * starlight collection, etc.) are queried programmatically by the
 * perk effect system.</p>
 *
 * <p>Call {@link #init()} during common setup to register all types.</p>
 */
@SuppressWarnings("null")
public final class PerkAttributeTypesAS {

    private PerkAttributeTypesAS() {}

    // ---- Vanilla-backed attribute types ----

    @Nonnull public static PerkAttributeType ATTR_TYPE_ARMOR;
    @Nonnull public static PerkAttributeType ATTR_TYPE_ARMOR_TOUGHNESS;
    @Nonnull public static PerkAttributeType ATTR_TYPE_ATTACK_DAMAGE;
    @Nonnull public static PerkAttributeType ATTR_TYPE_ATTACK_SPEED;
    @Nonnull public static PerkAttributeType ATTR_TYPE_MAX_HEALTH;
    @Nonnull public static PerkAttributeType ATTR_TYPE_MOVEMENT_SPEED;
    @Nonnull public static PerkAttributeType ATTR_TYPE_KNOCKBACK_RESIST;

    // ---- Custom attribute types (no vanilla backing) ----

    @Nonnull public static PerkAttributeType ATTR_TYPE_REACH;
    @Nonnull public static PerkAttributeType ATTR_TYPE_CRIT_CHANCE;
    @Nonnull public static PerkAttributeType ATTR_TYPE_CRIT_MULTIPLIER;
    @Nonnull public static PerkAttributeType ATTR_TYPE_ALL_ELEMENTAL_RESIST;
    @Nonnull public static PerkAttributeType ATTR_TYPE_MINING_SPEED;
    @Nonnull public static PerkAttributeType ATTR_TYPE_EXPERIENCE;
    @Nonnull public static PerkAttributeType ATTR_TYPE_LIFE_STEAL;
    @Nonnull public static PerkAttributeType ATTR_TYPE_STARLIGHT_COLLECTION;
    @Nonnull public static PerkAttributeType ATTR_TYPE_PERK_EFFECT;

    // ---- Alignment charge types ----

    @Nonnull public static PerkAttributeType ATTR_TYPE_ALIGNMENT_CHARGE_MAX;
    @Nonnull public static PerkAttributeType ATTR_TYPE_ALIGNMENT_CHARGE_REGEN;

    /**
     * Registers all built-in perk attribute types.
     * Must be called once during common mod initialization.
     */
    public static void init() {
        // Vanilla-backed types
        ATTR_TYPE_ARMOR = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.armor"),
                        () -> Attributes.ARMOR,
                        false));

        ATTR_TYPE_ARMOR_TOUGHNESS = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.armor_toughness"),
                        () -> Attributes.ARMOR_TOUGHNESS,
                        false));

        ATTR_TYPE_ATTACK_DAMAGE = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.attack_damage"),
                        () -> Attributes.ATTACK_DAMAGE,
                        false));

        ATTR_TYPE_ATTACK_SPEED = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.attack_speed"),
                        () -> Attributes.ATTACK_SPEED,
                        false));

        ATTR_TYPE_MAX_HEALTH = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.max_health"),
                        () -> Attributes.MAX_HEALTH,
                        false));

        ATTR_TYPE_MOVEMENT_SPEED = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.movement_speed"),
                        () -> Attributes.MOVEMENT_SPEED,
                        true));

        ATTR_TYPE_KNOCKBACK_RESIST = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.knockback_resistance"),
                        () -> Attributes.KNOCKBACK_RESISTANCE,
                        false));

        // Reach extends entity and block interaction range via Forge's reach attributes
        ATTR_TYPE_REACH = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.reach"),
                        () -> ForgeMod.ENTITY_REACH.get(),
                        false));

        ATTR_TYPE_CRIT_CHANCE = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.crit_chance")));

        ATTR_TYPE_CRIT_MULTIPLIER = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.crit_multiplier")));

        ATTR_TYPE_ALL_ELEMENTAL_RESIST = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.all_elemental_resist")));

        ATTR_TYPE_MINING_SPEED = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.mining_speed")));

        ATTR_TYPE_EXPERIENCE = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.experience")));

        ATTR_TYPE_LIFE_STEAL = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.life_steal")));

        ATTR_TYPE_STARLIGHT_COLLECTION = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.starlight_collection")));

        ATTR_TYPE_PERK_EFFECT = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.perk_effect")));

        ATTR_TYPE_ALIGNMENT_CHARGE_MAX = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.alignment_charge_max")));

        ATTR_TYPE_ALIGNMENT_CHARGE_REGEN = AttributeTypeRegistry.register(
                new PerkAttributeType(
                        AstralSorcery.key("perk.attr.alignment_charge_regen")));

        // Register value caps for dangerous custom attributes
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_CRIT_CHANCE.getKey(),             0.0, 0.75);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_CRIT_MULTIPLIER.getKey(),         1.0, 4.0);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_ALL_ELEMENTAL_RESIST.getKey(),    0.0, 0.60);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_LIFE_STEAL.getKey(),              0.0, 0.20);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_MINING_SPEED.getKey(),            0.0, 5.0);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_EXPERIENCE.getKey(),              0.0, 3.0);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_STARLIGHT_COLLECTION.getKey(),    0.0, 3.0);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_PERK_EFFECT.getKey(),             0.1, 3.0);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_ALIGNMENT_CHARGE_MAX.getKey(),    0.0, 10.0);
        PerkAttributeLimiter.registerLimit(ATTR_TYPE_ALIGNMENT_CHARGE_REGEN.getKey(), 0.0, 5.0);
    }
}
