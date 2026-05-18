/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that gives a 20% chance to double drops when mining stone-type
 * blocks (stone, andesite, diorite, granite, deepslate, cobblestone).
 * Logic in EventHandlerPerkEffects (BlockDropsEvent).
 */
public class KeyStoneEnrichment extends KeyPerk {

    public static final float DROP_BONUS_CHANCE = 0.20f;

    public KeyStoneEnrichment(int x, int y) {
        super(AstralSorcery.key("key_stone_enrichment"), x, y);
    }
}
