/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Base class for key perks that grant dynamic enchantment-like effects.
 * Subclasses define which enchantment behaviour to emulate and at what level.
 *
 * <p>This is a simplified port of the 1.16 KeyAddEnchantment system.
 * The enchantment registration and JEI display hooks are deferred to Phase 13.</p>
 */
public class KeyAddEnchantment extends KeyPerk {

    public KeyAddEnchantment(int x, int y) {
        super(AstralSorcery.key("key_add_enchantment"), x, y);
    }
}
