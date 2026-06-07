/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.data.world.GatewayHandler;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncGatewayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Client → Server: requests the full gateway network list.
 * Sent when the player opens the gateway teleportation UI
 * by interacting with a celestial gateway block.
 *
 * <p>The server responds with a {@link PktSyncGatewayList} containing
 * all registered gateway entries across all dimensions.</p>
 *
 * <p>1.16 → 1.20: Packet is empty (no payload needed). Format stable.</p>
 */
public class PktRequestGatewayList {

    public PktRequestGatewayList() {}

    public static void encode(@Nonnull PktRequestGatewayList pkt, @Nonnull FriendlyByteBuf buf) {
        // No data needed — server knows which gateways exist
    }

    @Nonnull
    public static PktRequestGatewayList decode(@Nonnull FriendlyByteBuf buf) {
        return new PktRequestGatewayList();
    }

    public static void handle(@Nonnull PktRequestGatewayList pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            @Nullable ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server == null) return;
            GatewayHandler handler = GatewayHandler.get(server);
            if (handler != null) {
                PacketChannel.sendToPlayer(
                        new PktSyncGatewayList(new ArrayList<>(handler.getAllGateways())), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
