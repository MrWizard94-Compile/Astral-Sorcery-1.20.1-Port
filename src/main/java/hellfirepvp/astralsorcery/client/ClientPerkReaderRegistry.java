package hellfirepvp.astralsorcery.client;

import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderAddedPercentage;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderBreakSpeed;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderFlatAttribute;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderVanillaAttribute;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Client-side reader registrations for the perk attribute display system.
 * Called once from {@link ClientProxy#onClientSetup}.
 *
 * <p>Each attribute type gets a matching {@link hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader}
 * so the perk tree UI can display formatted stat values.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPerkReaderRegistry {

    private ClientPerkReaderRegistry() {}

    public static void init() {
        // Vanilla-backed types — read directly from the player's attribute instance
        AttributeTypeRegistry.registerReader(new ReaderVanillaAttribute(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR,
                ForgeRegistries.ATTRIBUTES.getDelegateOrThrow(Attributes.ARMOR)));
        AttributeTypeRegistry.registerReader(new ReaderVanillaAttribute(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS,
                ForgeRegistries.ATTRIBUTES.getDelegateOrThrow(Attributes.ARMOR_TOUGHNESS)));
        AttributeTypeRegistry.registerReader(new ReaderVanillaAttribute(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE,
                ForgeRegistries.ATTRIBUTES.getDelegateOrThrow(Attributes.ATTACK_DAMAGE)).formatAsDecimal());
        AttributeTypeRegistry.registerReader(new ReaderVanillaAttribute(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED,
                ForgeRegistries.ATTRIBUTES.getDelegateOrThrow(Attributes.ATTACK_SPEED)).formatAsDecimal());
        AttributeTypeRegistry.registerReader(new ReaderVanillaAttribute(
                PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH,
                ForgeRegistries.ATTRIBUTES.getDelegateOrThrow(Attributes.MAX_HEALTH)).formatAsDecimal());
        AttributeTypeRegistry.registerReader(new ReaderVanillaAttribute(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED,
                ForgeRegistries.ATTRIBUTES.getDelegateOrThrow(Attributes.MOVEMENT_SPEED)).formatAsDecimal());
        AttributeTypeRegistry.registerReader(new ReaderVanillaAttribute(
                PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST,
                ForgeRegistries.ATTRIBUTES.getDelegateOrThrow(Attributes.KNOCKBACK_RESISTANCE)).formatAsDecimal());

        // Custom percentage types — display as signed percentages
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_STARLIGHT_COLLECTION));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_MAX));
        AttributeTypeRegistry.registerReader(
                new ReaderAddedPercentage(PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_REGEN));

        // Mining speed: derived from actual dig speed on cobblestone
        AttributeTypeRegistry.registerReader(
                new ReaderBreakSpeed(PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED));
    }
}
