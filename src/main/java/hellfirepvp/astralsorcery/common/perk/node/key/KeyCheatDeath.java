/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that cancels a fatal hit once, restoring the player to 4 hearts
 * and granting 10 seconds of Regeneration II. Has a 30-second cooldown.
 * Logic in EventHandlerPerkEffects (LivingDeathEvent, HIGHEST priority).
 */
public class KeyCheatDeath extends KeyPerk {

    public static final int COOLDOWN_TICKS   = 600;
    public static final int REGEN_DURATION   = 200;

    public KeyCheatDeath(int x, int y) {
        super(AstralSorcery.key("key_cheat_death"), x, y);
    }
}
