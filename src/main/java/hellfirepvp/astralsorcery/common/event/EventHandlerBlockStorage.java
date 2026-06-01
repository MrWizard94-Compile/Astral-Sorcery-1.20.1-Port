/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktClearBlockStorageStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;

/**
 * Clears the block storage container for items implementing
 * {@link ItemBlockStorage} (architect wand, exchange wand) when the
 * player left-clicks a block (server-side) or left-clicks empty space (client).
 *
 * <p>1.16 → 1.20: isRemote → isClientSide; PacketChannel.CHANNEL stable.</p>
 */
public final class EventHandlerBlockStorage {

    private EventHandlerBlockStorage() {}

    @SubscribeEvent
    public static void onLeftClickBlock(@Nonnull PlayerInteractEvent.LeftClickBlock event) {
        ItemStack held = event.getItemStack();
        if (held.isEmpty() || !(held.getItem() instanceof ItemBlockStorage)) return;
        if (!event.getLevel().isClientSide()) {
            ItemBlockStorage.clearContainerFor(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(@Nonnull PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack held = event.getItemStack();
        if (held.isEmpty() || !(held.getItem() instanceof ItemBlockStorage)) return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                PacketChannel.sendToServer(new PktClearBlockStorageStack()));
    }
}
