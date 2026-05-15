/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPlayerProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Client -> Server signal packet requesting a full progress sync.
 * Sent by the client on login, dimension change, or when the client
 * detects its local progress may be stale.
 *
 * <p>This packet has no fields -- it is a pure signal. The server
 * responds with a {@link PktSyncPlayerProgress} packet.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * PacketBuffer -> FriendlyByteBuf,
 * ServerPlayerEntity -> ServerPlayer</p>
 */
public class PktRequestProgress {

    public PktRequestProgress() {}

    public static void encode(@Nonnull PktRequestProgress msg, @Nonnull FriendlyByteBuf buf) {
        // No fields to write -- signal packet
    }

    @Nonnull
    public static PktRequestProgress decode(@Nonnull FriendlyByteBuf buf) {
        // No fields to read -- signal packet
        return new PktRequestProgress();
    }

    public static void handle(@Nonnull PktRequestProgress msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            @Nullable ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                AstralSorcery.log.warn("Received PktRequestProgress with null sender");
                return;
            }
            PlayerProgress progress = PlayerProgressHelper.getProgress(sender);
            if (progress == null) {
                AstralSorcery.log.warn("Player {} missing progress capability on progress request",
                        sender.getName().getString());
                return;
            }
            PacketChannel.sendToPlayer(new PktSyncPlayerProgress(progress), sender);
            AstralSorcery.log.debug("Sent progress sync to {}", sender.getName().getString());
        });
        ctx.setPacketHandled(true);
    }
}
