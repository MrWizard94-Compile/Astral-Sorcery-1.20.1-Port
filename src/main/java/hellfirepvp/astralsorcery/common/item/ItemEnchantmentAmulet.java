package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;

/**
 * Enchantment Amulet — stores constellation-based enchantments and applies them to items
 * when the player interacts with the refraction table. Two altar recipes exist:
 * one to initialize and one to re-roll stored enchantments.
 * Full NBT storage and enchantment logic deferred to later phase.
 */
public class ItemEnchantmentAmulet extends ItemAS {

    public ItemEnchantmentAmulet() {
        super(defaultProperties().stacksTo(1));
    }
}
