/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that balances alignment charge regeneration across all
 * constellations equally. Actual balancing logic lives in the charge handler.
 */
public class KeyChargeBalancing extends KeyPerk {

    public KeyChargeBalancing(int x, int y) {
        super(AstralSorcery.key("key_charge_balancing"), x, y);
    }
}
