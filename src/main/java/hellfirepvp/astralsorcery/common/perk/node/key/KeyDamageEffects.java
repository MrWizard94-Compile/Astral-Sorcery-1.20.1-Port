/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that applies Poison I for 3 seconds on targets hit by the player.
 * Logic in EventHandlerPerkEffects (LivingHurtEvent).
 */
public class KeyDamageEffects extends KeyPerk {

    public static final int POISON_DURATION = 60;

    public KeyDamageEffects(int x, int y) {
        super(AstralSorcery.key("key_damage_effects"), x, y);
    }
}
