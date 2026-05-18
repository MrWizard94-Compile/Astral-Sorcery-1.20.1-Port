/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that fells an entire tree when the player breaks any log block
 * while using an axe. Breaks up to 64 connected logs via flood-fill.
 * Logic in EventHandlerPerkEffects (BlockEvent.BreakEvent).
 */
public class KeyTreeConnector extends KeyPerk {

    public static final int MAX_LOGS = 64;

    public KeyTreeConnector(int x, int y) {
        super(AstralSorcery.key("key_tree_connector"), x, y);
    }
}
