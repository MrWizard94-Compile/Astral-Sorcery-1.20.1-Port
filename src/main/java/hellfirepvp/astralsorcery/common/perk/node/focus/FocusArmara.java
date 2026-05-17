/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.focus;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.FocusPerk;

/**
 * Focus perk for the Armara constellation (deep defense).
 * Grants +5% elemental resistance and +1 flat knockback resistance.
 */
public class FocusArmara extends FocusPerk {

    public FocusArmara(int x, int y) {
        super(AstralSorcery.key("focus_armara"), x, y, AstralSorcery.key("armara"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.05f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST.getKey(),
                ModifierType.ADDITION, 1.0f));
    }
}
