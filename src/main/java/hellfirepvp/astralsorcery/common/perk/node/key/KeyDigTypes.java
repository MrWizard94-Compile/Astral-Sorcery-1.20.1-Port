/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that lets a pickaxe mine shovel- and axe-type blocks at full speed.
 * Logic in EventHandlerPerkEffects (PlayerEvent.HarvestCheck + PlayerEvent.BreakSpeed).
 */
public class KeyDigTypes extends KeyPerk {

    public KeyDigTypes(int x, int y) {
        super(AstralSorcery.key("key_dig_types"), x, y);
    }
}
