/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Engraving-only tool enchantment. Incompatible with Silk Touch.
 * Cannot be applied at an enchanting table — engraving only.
 *
 * <p>1.16 → 1.20: EnchantmentType → EnchantmentCategory,
 * EquipmentSlotType → EquipmentSlot,
 * canApplyTogether → checkCompatibility.</p>
 */
public class EnchantmentScorchingHeat extends Enchantment {

    public EnchantmentScorchingHeat() {
        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER,
                new EquipmentSlot[]{ EquipmentSlot.MAINHAND });
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != Enchantments.SILK_TOUCH;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }
}
