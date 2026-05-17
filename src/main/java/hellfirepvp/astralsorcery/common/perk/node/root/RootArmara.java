/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.root;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;

/**
 * Root perk for the Armara constellation branch (defense).
 * Grants a flat +1.0 armor bonus as the entry point
 * into the defensive perk tree path.
 */
public class RootArmara extends RootPerk {

    public RootArmara(int x, int y) {
        super(AstralSorcery.key("root_armara"), x, y, AstralSorcery.key("armara"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR.getKey(),
                ModifierType.ADDITION, 1.0f));
    }
}
