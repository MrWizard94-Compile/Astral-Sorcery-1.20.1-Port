/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that gives melee attacks a 25% chance to inflict Weakness I
 * for 2 seconds on the target, simulating a bleed effect.
 * Logic in EventHandlerPerkEffects (LivingHurtEvent).
 */
public class KeyBleed extends KeyPerk {

    public static final float BLEED_CHANCE   = 0.25f;
    public static final int   BLEED_DURATION = 40;

    public KeyBleed(int x, int y) {
        super(AstralSorcery.key("key_bleed"), x, y);
    }
}
