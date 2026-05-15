/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server -> Client packet that notifies the client about a constellation
 * discovery or removal. Triggers client-side VFX and updates local
 * progress data.
 *
 * <p>1.16 -> 1.20 changes:
 * PacketBuffer -> FriendlyByteBuf,
 * ResourceLocation read/write unchanged</p>
 */
public class PktSyncConstellation {

    @Nonnull
    private final ResourceLocation constellation;
    private final boolean discovered;

    /**
     * @param constellation the constellation registry name
     * @param discovered    true if newly discovered, false if removed
     */
    public PktSyncConstellation(@Nonnull ResourceLocation constellation, boolean discovered) {
        this.constellation = constellation;
        this.discovered = discovered;
    }

    @Nonnull
    public ResourceLocation getConstellation() {
        return constellation;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public static void encode(@Nonnull PktSyncConstellation msg, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.constellation);
        buf.writeBoolean(msg.discovered);
    }

    @Nonnull
    public static PktSyncConstellation decode(@Nonnull FriendlyByteBuf buf) {
        ResourceLocation constellation = buf.readResourceLocation();
        boolean discovered = buf.readBoolean();
        return new PktSyncConstellation(constellation, discovered);
    }

    public static void handle(@Nonnull PktSyncConstellation msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg))
        );
        ctx.setPacketHandled(true);
    }

    private static void handleClient(@Nonnull PktSyncConstellation msg) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.getCapability(PlayerCapabilityProvider.CAPABILITY).ifPresent(progress -> {
            if (msg.discovered) {
                progress.discoverConstellation(msg.constellation);
                AstralSorcery.log.debug("Constellation discovered: {}", msg.constellation);
            } else {
                // Removal requires clearing and re-adding all except the target.
                // For now, log — full removal support added when research system is complete.
                AstralSorcery.log.debug("Constellation removal requested: {}", msg.constellation);
            }
        });
        // TODO: Trigger constellation discovery VFX on client
    }
}
