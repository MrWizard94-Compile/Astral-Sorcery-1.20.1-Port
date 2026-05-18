/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that reduces incoming damage by 30% when the player wears fewer
 * than 2 armor pieces. Logic in EventHandlerPerkEffects (LivingHurtEvent).
 */
public class KeyNoArmor extends KeyPerk {

    public static final float DAMAGE_MULTIPLIER = 0.70f;

    public KeyNoArmor(int x, int y) {
        super(AstralSorcery.key("key_no_armor"), x, y);
    }
}
