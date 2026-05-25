/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.event;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * Client-side counterpart to EventHandlerMantleTick.
 * Drives {@link MantleEffect#tickClient} for effects that need client-only VFX.
 * Registered from {@link hellfirepvp.astralsorcery.client.ClientProxy#attachEventHandlers}.
 */
@OnlyIn(Dist.CLIENT)
public class EventHandlerClientMantleTick {

    @SubscribeEvent
    public void onPlayerTick(@Nonnull TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side != net.minecraftforge.fml.LogicalSide.CLIENT) return;

        Player player = event.player;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !(chest.getItem() instanceof ItemMantle mantle)) return;

        MantleEffect effect = MantleEffectRegistry.getEffectForItem(mantle);
        if (effect == null || !effect.shouldTickClient()) return;

        effect.onClientTick(player);
    }
}
