/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that automatically pulls all item drops from player kills directly
 * into the player's inventory. Logic in EventHandlerPerkEffects (LivingDropsEvent).
 */
public class KeyMagnetDrops extends KeyPerk {

    public KeyMagnetDrops(int x, int y) {
        super(AstralSorcery.key("key_magnet_drops"), x, y);
    }
}
