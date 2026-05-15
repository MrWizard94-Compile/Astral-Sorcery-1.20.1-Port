package hellfirepvp.astralsorcery.common.item.tool;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import javax.annotation.Nonnull;

/**
 * Crystal Pickaxe — high-durability pickaxe made from rock crystals.
 * Crystal properties (size, purity, cutting) modify tool stats.
 *
 * <p>1.16 -> 1.20 changes:
 * ItemTier enum → Tiers enum,
 * PickaxeItem constructor uses (Tier, int attackDamage, float attackSpeed, Properties),
 * IItemTier → Tier interface,
 * Item.Properties.group removed (creative tab system)</p>
 */
public class ItemCrystalPickaxe extends PickaxeItem {

    public ItemCrystalPickaxe() {
        super(CrystalToolTier.INSTANCE, 1, -2.8F, new Properties());
    }

    // TODO: Override getDestroySpeed to apply crystal purity/cutting bonus
    // TODO: Override getMaxDamage to apply crystal size durability scaling
    // TODO: Crystal-specific enchantability from NBT properties
}
