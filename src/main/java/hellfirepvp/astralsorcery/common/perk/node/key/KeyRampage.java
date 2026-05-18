/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that grants Speed II + Haste II + Strength I for 5 seconds when
 * the player kills a mob. Logic in EventHandlerPerkEffects (LivingDeathEvent).
 */
public class KeyRampage extends KeyPerk {

    public static final int RAMPAGE_TICKS = 100;

    public KeyRampage(int x, int y) {
        super(AstralSorcery.key("key_rampage"), x, y);
    }
}
