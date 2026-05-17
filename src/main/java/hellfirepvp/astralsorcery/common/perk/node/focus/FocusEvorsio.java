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
 * Focus perk for the Evorsio constellation (deep mining).
 * Grants +15% mining speed (ADDED_MULTIPLY 0.15) and
 * +10% experience bonus (ADDED_MULTIPLY 0.10).
 */
public class FocusEvorsio extends FocusPerk {

    public FocusEvorsio(int x, int y) {
        super(AstralSorcery.key("focus_evorsio"), x, y, AstralSorcery.key("evorsio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
    }
}
