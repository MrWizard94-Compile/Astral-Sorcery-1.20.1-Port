/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.entity.EntityObservatoryHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Client → Server: updates the player's observed direction in the observatory.
 * Sent periodically while the player is mounted on an EntityObservatoryHelper
 * and moving their camera in the observatory UI.
 *
 * <p>1.16 → 1.20: Packet format unchanged. Entity ID-based lookup stable.</p>
 */
public class PktObservatoryUpdate {

    private final int entityId;
    private final float pitch;
    private final float yaw;

    public PktObservatoryUpdate(int entityId, float pitch, float yaw) {
        this.entityId = entityId;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public static void encode(@Nonnull PktObservatoryUpdate pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.entityId);
        buf.writeFloat(pkt.pitch);
        buf.writeFloat(pkt.yaw);
    }

    @Nonnull
    public static PktObservatoryUpdate decode(@Nonnull FriendlyByteBuf buf) {
        return new PktObservatoryUpdate(buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(@Nonnull PktObservatoryUpdate pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            @Nullable ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Entity entity = player.level().getEntity(pkt.entityId);
            if (entity instanceof EntityObservatoryHelper helper) {
                // Validate the player is actually riding this helper
                if (helper.getPassengers().contains(player)) {
                    helper.updateObservedDirection(pkt.pitch, pkt.yaw);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
