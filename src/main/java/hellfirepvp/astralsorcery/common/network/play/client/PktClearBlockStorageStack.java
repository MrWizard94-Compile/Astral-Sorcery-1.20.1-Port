/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Client → Server: clears the stored block reference in an ItemBlockStorage
 * item (architect wand, exchange wand) when the player left-clicks empty space.
 *
 * <p>No fields — pure signal. Server clears the container for the sending player.</p>
 */
public class PktClearBlockStorageStack {

    public static void encode(@Nonnull PktClearBlockStorageStack pkt, @Nonnull FriendlyByteBuf buf) {
        // No fields to encode
    }

    @Nonnull
    public static PktClearBlockStorageStack decode(@Nonnull FriendlyByteBuf buf) {
        return new PktClearBlockStorageStack();
    }

    public static void handle(@Nonnull PktClearBlockStorageStack pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            @Nullable ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemBlockStorage.clearContainerFor(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
