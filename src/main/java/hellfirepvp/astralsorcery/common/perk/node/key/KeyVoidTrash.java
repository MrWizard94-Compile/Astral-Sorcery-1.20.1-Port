/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.item.Items;

import java.util.Set;

/**
 * Key perk that automatically voids common trash items (cobblestone, dirt,
 * gravel, etc.) from entity drops when the player kills a mob.
 * Logic in EventHandlerPerkEffects (LivingDropsEvent).
 */
public class KeyVoidTrash extends KeyPerk {

    public static final Set<net.minecraft.world.item.Item> TRASH_ITEMS = Set.of(
            Items.DIRT,
            Items.COBBLESTONE,
            Items.ANDESITE,
            Items.DIORITE,
            Items.GRANITE,
            Items.STONE,
            Items.GRAVEL,
            Items.COBBLED_DEEPSLATE
    );

    public KeyVoidTrash(int x, int y) {
        super(AstralSorcery.key("key_void_trash"), x, y);
    }
}
