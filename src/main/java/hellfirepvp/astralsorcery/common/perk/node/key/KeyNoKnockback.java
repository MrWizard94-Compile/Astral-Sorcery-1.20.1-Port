/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that prevents knockback from being applied to the player.
 * Logic is handled in EventHandlerPerkEffects (LivingKnockBackEvent).
 */
public class KeyNoKnockback extends KeyPerk {

    public KeyNoKnockback(int x, int y) {
        super(AstralSorcery.key("key_no_knockback"), x, y);
    }
}
