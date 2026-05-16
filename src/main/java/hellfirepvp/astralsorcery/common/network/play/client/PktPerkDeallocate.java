/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Client → Server: Player requests to deallocate (refund) a perk.
 * Server validates connectivity constraint and applies it.
 */
public class PktPerkDeallocate {

    @Nonnull
    private final ResourceLocation perkKey;

    public PktPerkDeallocate(@Nonnull ResourceLocation perkKey) {
        this.perkKey = perkKey;
    }

    public static void encode(@Nonnull PktPerkDeallocate pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.perkKey);
    }

    @Nonnull
    public static PktPerkDeallocate decode(@Nonnull FriendlyByteBuf buf) {
        return new PktPerkDeallocate(buf.readResourceLocation());
    }

    public static void handle(@Nonnull PktPerkDeallocate pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // TODO: Validate and deallocate via capability
            AstralSorcery.log.debug("Perk deallocate request from {}: {}",
                    player.getName().getString(), pkt.perkKey);
        });
        ctx.get().setPacketHandled(true);
    }
}
