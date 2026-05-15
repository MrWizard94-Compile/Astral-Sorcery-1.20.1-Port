package hellfirepvp.astralsorcery.common.item.tool;

import net.minecraft.world.item.AxeItem;

/**
 * Crystal Axe — high-durability axe made from rock crystals.
 *
 * <p>1.16 -> 1.20 changes: Same as ItemCrystalPickaxe.</p>
 */
public class ItemCrystalAxe extends AxeItem {

    public ItemCrystalAxe() {
        super(CrystalToolTier.INSTANCE, 5.0F, -3.0F, new Properties());
    }
}
