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
 * Root perk for the Aevitas constellation branch (life/growth).
 * Grants a flat +2.0 max health bonus as the entry point
 * into the vitality perk tree path.
 */
public class RootAevitas extends RootPerk {

    public RootAevitas(int x, int y) {
        super(AstralSorcery.key("root_aevitas"), x, y, AstralSorcery.key("aevitas"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH.getKey(),
                ModifierType.ADDITION, 2.0f));
    }
}
