/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server → Client: Notifies the client that the player has discovered
 * a new constellation. Triggers the discovery animation and updates
 * the journal's discovered constellation list.
 */
public class PktDiscoveryUpdate {

    @Nonnull
    private final ResourceLocation constellationKey;
    private final boolean showAnimation;

    public PktDiscoveryUpdate(@Nonnull ResourceLocation constellationKey, boolean showAnimation) {
        this.constellationKey = constellationKey;
        this.showAnimation = showAnimation;
    }

    public static void encode(@Nonnull PktDiscoveryUpdate pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.constellationKey);
        buf.writeBoolean(pkt.showAnimation);
    }

    @Nonnull
    public static PktDiscoveryUpdate decode(@Nonnull FriendlyByteBuf buf) {
        return new PktDiscoveryUpdate(buf.readResourceLocation(), buf.readBoolean());
    }

    public static void handle(@Nonnull PktDiscoveryUpdate pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(pkt));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktDiscoveryUpdate pkt) {
        PlayerProgressManager.getClientProgress().discoverConstellation(pkt.constellationKey);
        // Discovery animation deferred — VFX system not yet ported (Phase 12)
    }

    @Nonnull
    public ResourceLocation getConstellationKey() {
        return constellationKey;
    }

    public boolean shouldShowAnimation() {
        return showAnimation;
    }
}
