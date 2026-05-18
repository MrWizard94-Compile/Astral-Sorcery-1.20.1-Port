/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that extends melee and block interaction reach by 2.5 blocks.
 */
public class KeyEntityReach extends KeyPerk {

    public KeyEntityReach(int x, int y) {
        super(AstralSorcery.key("key_entity_reach"), x, y);
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_REACH.getKey(),
                ModifierType.ADDITION, 2.5f));
    }
}
