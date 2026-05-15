package hellfirepvp.astralsorcery.common.item.tool;

import net.minecraft.world.item.ShovelItem;

/**
 * Crystal Shovel — high-durability shovel made from rock crystals.
 *
 * <p>1.16 -> 1.20 changes: Same as ItemCrystalPickaxe.</p>
 */
public class ItemCrystalShovel extends ShovelItem {

    public ItemCrystalShovel() {
        super(CrystalToolTier.INSTANCE, 1.5F, -3.0F, new Properties());
    }
}
