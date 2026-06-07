package hellfirepvp.astralsorcery.common.network.play.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server â†’ Client: notifies the client that the local player's attunement
 * ceremony has started or stopped. The client uses this to run the camera
 * orbit effect during the 800-tick attunement.
 */
public class PktAttunementActive {

    private final boolean active;
    private final BlockPos altarPos;

    public PktAttunementActive(boolean active, @Nonnull BlockPos altarPos) {
        this.active = active;
        this.altarPos = altarPos;
    }

    public static void encode(@Nonnull PktAttunementActive pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.active);
        buf.writeBlockPos(pkt.altarPos);
    }

    @Nonnull
    public static PktAttunementActive decode(@Nonnull FriendlyByteBuf buf) {
        boolean active = buf.readBoolean();
        BlockPos pos = buf.readBlockPos();
        return new PktAttunementActive(active, pos);
    }

    public static void handle(@Nonnull PktAttunementActive pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (pkt.active) {
                    hellfirepvp.astralsorcery.client.effect.AttunementCameraEffect
                            .INSTANCE.startAttunement(pkt.altarPos);
                } else {
                    hellfirepvp.astralsorcery.client.effect.AttunementCameraEffect
                            .INSTANCE.stopAttunement();
                }
            }));
        ctx.get().setPacketHandled(true);
    }
}
