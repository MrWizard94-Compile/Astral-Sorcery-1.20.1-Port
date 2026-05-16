/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Client → Server: Player requests teleportation via a Celestial Gateway.
 * Sends the target gateway position. Server validates the source gateway,
 * checks if destination is a valid linked gateway, and teleports the player.
 */
public class PktGatewayTeleport {

    @Nonnull
    private final BlockPos targetPos;
    @Nonnull
    private final String targetDimension;

    public PktGatewayTeleport(@Nonnull BlockPos targetPos, @Nonnull String targetDimension) {
        this.targetPos = targetPos;
        this.targetDimension = targetDimension;
    }

    public static void encode(@Nonnull PktGatewayTeleport pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.targetPos);
        buf.writeUtf(pkt.targetDimension);
    }

    @Nonnull
    public static PktGatewayTeleport decode(@Nonnull FriendlyByteBuf buf) {
        return new PktGatewayTeleport(buf.readBlockPos(), buf.readUtf(256));
    }

    public static void handle(@Nonnull PktGatewayTeleport pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // TODO: Validate source gateway proximity, target gateway exists,
            // linked network membership, then teleport player.
            AstralSorcery.log.debug("Gateway teleport request from {} to {} ({})",
                    player.getName().getString(), pkt.targetPos, pkt.targetDimension);
        });
        ctx.get().setPacketHandled(true);
    }
}
