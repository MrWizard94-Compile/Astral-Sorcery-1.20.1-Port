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
 * Root perk for the Discidia constellation branch (offense).
 * Grants a flat +1.0 attack damage bonus as the entry point
 * into the offensive perk tree path.
 */
public class RootDiscidia extends RootPerk {

    public RootDiscidia(int x, int y) {
        super(AstralSorcery.key("root_discidia"), x, y, AstralSorcery.key("discidia"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE.getKey(),
                ModifierType.ADDITION, 1.0f));
    }
}
