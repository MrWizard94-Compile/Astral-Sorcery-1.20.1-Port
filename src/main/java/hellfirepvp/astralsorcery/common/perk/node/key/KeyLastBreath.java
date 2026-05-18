/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that scales attack damage and dig speed based on missing health.
 * At 0% health remaining the bonuses are at maximum (3× damage, 1.5× dig speed).
 * Logic in EventHandlerPerkEffects (LivingHurtEvent + PlayerEvent.BreakSpeed).
 */
public class KeyLastBreath extends KeyPerk {

    public static final float DAMAGE_MULTIPLIER = 3.0f;
    public static final float DIG_MULTIPLIER    = 1.5f;

    public KeyLastBreath(int x, int y) {
        super(AstralSorcery.key("key_last_breath"), x, y);
    }
}
