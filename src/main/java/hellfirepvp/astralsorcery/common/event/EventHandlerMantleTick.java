/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * Drives per-player-tick callbacks for all active {@link MantleEffect} instances.
 * Replaces the 1.16 observerlib ITickHandler registered per MantleEffect subclass.
 * Registered on the Forge event bus in CommonProxy.
 */
public class EventHandlerMantleTick {

    @SubscribeEvent
    public void onPlayerTick(@Nonnull TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !(chest.getItem() instanceof ItemMantle mantle)) return;

        MantleEffect effect = MantleEffectRegistry.getEffectForItem(mantle);
        if (effect == null || !effect.shouldTick()) return;

        effect.onServerTick(player);
    }
}
