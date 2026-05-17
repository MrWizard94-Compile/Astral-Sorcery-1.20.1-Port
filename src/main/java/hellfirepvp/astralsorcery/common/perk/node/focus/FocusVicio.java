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
 * Focus perk for the Vicio constellation (deep movement).
 * Grants +5% movement speed (ADDED_MULTIPLY 0.05) and
 * +0.5 flat reach distance.
 */
public class FocusVicio extends FocusPerk {

    public FocusVicio(int x, int y) {
        super(AstralSorcery.key("focus_vicio"), x, y, AstralSorcery.key("vicio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.05f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_REACH.getKey(),
                ModifierType.ADDITION, 0.5f));
    }
}
