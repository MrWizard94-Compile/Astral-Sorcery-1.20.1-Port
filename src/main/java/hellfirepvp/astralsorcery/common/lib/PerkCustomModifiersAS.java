package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;

import java.util.UUID;

/**
 * Static constants for special focus-constellation perk attribute modifiers.
 *
 * <p>These modifiers are applied when a player has specific focus constellation
 * key perks unlocked. They use deterministic UUIDs so they remain stable across
 * reloads and do not stack with themselves.</p>
 *
 * <p>Populated in {@link #init()} which must be called after
 * {@link PerkAttributeTypesAS#init()} during common setup.</p>
 */
public final class PerkCustomModifiersAS {

    private PerkCustomModifiersAS() {}

    /** Focus Gelu: converts a portion of movement speed to armor. */
    public static PerkAttributeModifier FOCUS_GELU;

    /** Focus Ulteria: bonus perk experience per perk effect trigger. */
    public static PerkAttributeModifier FOCUS_ULTERIA;

    /** Focus Vorux: converts a portion of armor to melee attack damage. */
    public static PerkAttributeModifier FOCUS_VORUX;

    public static void init() {
        FOCUS_GELU = new PerkAttributeModifier(
                UUID.fromString("a1b2c3d4-0001-0000-0000-000000000001"),
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR.getKey(),
                ModifierType.ADDED_MULTIPLY,
                0.15
        );

        FOCUS_ULTERIA = new PerkAttributeModifier(
                UUID.fromString("a1b2c3d4-0001-0000-0000-000000000002"),
                PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT.getKey(),
                ModifierType.ADDED_MULTIPLY,
                0.10
        );

        FOCUS_VORUX = new PerkAttributeModifier(
                UUID.fromString("a1b2c3d4-0001-0000-0000-000000000003"),
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE.getKey(),
                ModifierType.ADDED_MULTIPLY,
                0.20
        );
    }
}
