/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that adds a sweep attack to all melee hits, dealing 50% damage
 * to entities within 2.5 blocks of the primary target.
 * Logic in EventHandlerPerkEffects (LivingHurtEvent, HIGH priority).
 */
public class KeyAreaOfEffect extends KeyPerk {

    public static final float SWEEP_PERCENT = 0.50f;
    public static final float SWEEP_RANGE   = 2.5f;

    public KeyAreaOfEffect(int x, int y) {
        super(AstralSorcery.key("key_area_of_effect"), x, y);
    }
}
