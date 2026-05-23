/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.fml.LogicalSide;

/**
 * Base class for enchantments that need a per-player tick callback.
 * Subclasses are discovered at runtime by {@link hellfirepvp.astralsorcery.common.event.EventHandlerEnchantmentTick}.
 *
 * <p>1.16 → 1.20: EnchantmentType → EnchantmentCategory,
 * EquipmentSlotType → EquipmentSlot, PlayerEntity → Player.</p>
 */
public abstract class EnchantmentPlayerTick extends Enchantment {

    protected EnchantmentPlayerTick(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
        super(rarity, category, slots);
    }

    public abstract void tick(Player player, LogicalSide side, int level);
}
