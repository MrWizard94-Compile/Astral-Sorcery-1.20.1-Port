/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that gives melee hits a 20% chance to knock the weapon from a mob's
 * main hand, dropping it as an item. Logic in EventHandlerPerkEffects (LivingHurtEvent).
 */
public class KeyDisarm extends KeyPerk {

    public static final float DISARM_CHANCE = 0.20f;

    public KeyDisarm(int x, int y) {
        super(AstralSorcery.key("key_disarm"), x, y);
    }
}
