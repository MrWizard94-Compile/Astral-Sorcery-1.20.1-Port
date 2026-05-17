package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;

/**
 * Perk Seal — consumable item that temporarily disables an allocated perk
 * without deallocating it from the perk tree. This preserves tree connectivity
 * while removing the perk's effects.
 *
 * <p>Applied by right-clicking a perk in the perk tree UI while holding a seal.
 * Consumed on use. Unsealing is free.</p>
 */
public class ItemPerkSeal extends ItemAS {

    public ItemPerkSeal() {
        super(defaultProperties().stacksTo(16));
    }
}
