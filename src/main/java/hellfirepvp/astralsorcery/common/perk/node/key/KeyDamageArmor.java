/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that damages one random armor piece on the target with each hit.
 * Logic in EventHandlerPerkEffects (LivingHurtEvent).
 */
public class KeyDamageArmor extends KeyPerk {

    public KeyDamageArmor(int x, int y) {
        super(AstralSorcery.key("key_damage_armor"), x, y);
    }
}
