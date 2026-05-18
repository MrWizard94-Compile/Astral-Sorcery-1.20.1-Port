/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.item.Item;

/**
 * Abstract base class for all crystal items (rock crystal, celestial crystal, attuned variants).
 * Crystal properties (size, purity, cutting) are stored as NBT via the CrystalAttributes system
 * (ported separately in the crystal package).
 */
public abstract class ItemCrystalBase extends ItemAS {

    public ItemCrystalBase(Item.Properties properties) {
        super(properties);
    }
}
