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
 * Root perk for the Vicio constellation branch (movement).
 * Grants a 3% movement speed bonus (ADDED_MULTIPLY 0.03) as the
 * entry point into the mobility perk tree path.
 */
public class RootVicio extends RootPerk {

    public RootVicio(int x, int y) {
        super(AstralSorcery.key("root_vicio"), x, y, AstralSorcery.key("vicio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.03f));
    }
}
