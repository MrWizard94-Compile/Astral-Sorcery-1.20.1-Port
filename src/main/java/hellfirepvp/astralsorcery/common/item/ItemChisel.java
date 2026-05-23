package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nonnull;

/**
 * Chisel — used on blocks in-world to transmute them (applies block transmutation recipes).
 * Can accept the Fortune enchantment at an enchanting table.
 *
 * <p>1.16 -> 1.20 changes:
 * Item.Properties.maxDamage -> durability,
 * canApplyAtEnchantingTable -> override still available in 1.20.</p>
 */
public class ItemChisel extends ItemAS {

    public ItemChisel() {
        super(new Properties().durability(72));
    }

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull net.minecraft.world.item.ItemStack stack,
                                             @Nonnull Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment)
                || enchantment == Enchantments.BLOCK_FORTUNE;
    }

    @Override
    public int getEnchantmentValue() {
        return 3;
    }
}
