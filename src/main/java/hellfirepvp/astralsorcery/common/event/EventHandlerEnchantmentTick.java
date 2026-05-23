/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.enchantment.EnchantmentPlayerTick;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drives per-player-tick callbacks for all {@link EnchantmentPlayerTick} enchantments.
 * Replaces the 1.16 EventHelperEnchantmentTick (which used the observerlib ITickHandler).
 * Registered on the Forge event bus in CommonProxy.
 */
public class EventHandlerEnchantmentTick {

    private Collection<EnchantmentPlayerTick> tickableEnchantments;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (tickableEnchantments == null) {
            tickableEnchantments = ForgeRegistries.ENCHANTMENTS.getValues().stream()
                    .filter(e -> e instanceof EnchantmentPlayerTick)
                    .map(e -> (EnchantmentPlayerTick) e)
                    .collect(Collectors.toList());
        }

        Player player = event.player;
        LogicalSide side = event.side;

        for (EnchantmentPlayerTick ench : tickableEnchantments) {
            int totalLevel = 0;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                int lvl = EnchantmentHelper.getItemEnchantmentLevel(ench, player.getItemBySlot(slot));
                if (lvl > totalLevel) totalLevel = lvl;
            }
            if (totalLevel > 0) {
                ench.tick(player, side, totalLevel);
            }
        }
    }
}
