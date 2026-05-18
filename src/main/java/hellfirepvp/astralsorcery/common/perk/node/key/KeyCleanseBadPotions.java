/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that removes one random harmful potion effect each time the player
 * heals naturally. Higher heal amounts → higher cleanse chance.
 * Logic in EventHandlerPerkEffects (LivingHealEvent, LOW priority).
 */
public class KeyCleanseBadPotions extends KeyPerk {

    public KeyCleanseBadPotions(int x, int y) {
        super(AstralSorcery.key("key_cleanse_bad_potions"), x, y);
    }
}
