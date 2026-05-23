/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.fml.LogicalSide;

/**
 * Engraving-only helmet enchantment that continuously applies Night Vision.
 * Applied to the wearer's head slot once per player tick (server-side only).
 *
 * <p>1.16 → 1.20: EnchantmentType → EnchantmentCategory,
 * EquipmentSlotType → EquipmentSlot, PlayerEntity → Player,
 * Effects → MobEffects, addPotionEffect → addEffect,
 * EffectInstance → MobEffectInstance.</p>
 */
public class EnchantmentNightVision extends EnchantmentPlayerTick {

    public EnchantmentNightVision() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD,
                new EquipmentSlot[]{ EquipmentSlot.HEAD });
    }

    @Override
    public void tick(Player player, LogicalSide side, int level) {
        if (side.isServer()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, level - 1, true, false));
        }
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }
}
