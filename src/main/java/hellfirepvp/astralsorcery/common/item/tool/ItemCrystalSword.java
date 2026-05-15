package hellfirepvp.astralsorcery.common.item.tool;

import net.minecraft.world.item.SwordItem;

/**
 * Crystal Sword — high-durability sword made from rock crystals.
 *
 * <p>1.16 -> 1.20 changes: Same as ItemCrystalPickaxe.</p>
 */
public class ItemCrystalSword extends SwordItem {

    public ItemCrystalSword() {
        super(CrystalToolTier.INSTANCE, 3, -2.4F, new Properties());
    }
}
