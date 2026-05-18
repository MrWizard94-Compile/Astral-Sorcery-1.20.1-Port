/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that enables mantle flight when wearing an attuned mantle.
 * Actual flight behavior is gated on the mantle item's tick logic — this perk
 * acts as the unlock condition checked by the mantle item.
 */
public class KeyMantleFlight extends KeyPerk {

    public KeyMantleFlight(int x, int y) {
        super(AstralSorcery.key("key_mantle_flight"), x, y);
    }
}
