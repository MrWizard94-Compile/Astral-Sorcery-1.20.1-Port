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
 * Root perk for the Evorsio constellation branch (mining/destruction).
 * Grants a 10% mining speed bonus (ADDED_MULTIPLY 0.1) as the
 * entry point into the mining perk tree path.
 */
public class RootEvorsio extends RootPerk {

    public RootEvorsio(int x, int y) {
        super(AstralSorcery.key("root_evorsio"), x, y, AstralSorcery.key("evorsio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.1f));
    }
}
