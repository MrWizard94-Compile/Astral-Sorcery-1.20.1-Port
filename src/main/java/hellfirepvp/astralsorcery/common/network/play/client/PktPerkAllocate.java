/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.perk.AllocationStatus;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Client → Server: Player requests to allocate a perk.
 * Server validates the request (adjacency, points, tier) and applies it.
 * On success, syncs updated progress back to client.
 */
public class PktPerkAllocate {

    @Nonnull
    private final ResourceLocation perkKey;

    public PktPerkAllocate(@Nonnull ResourceLocation perkKey) {
        this.perkKey = perkKey;
    }

    // ---- Codec ----

    public static void encode(@Nonnull PktPerkAllocate pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.perkKey);
    }

    @Nonnull
    public static PktPerkAllocate decode(@Nonnull FriendlyByteBuf buf) {
        return new PktPerkAllocate(buf.readResourceLocation());
    }

    // ---- Handler ----

    public static void handle(@Nonnull PktPerkAllocate pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            PlayerProgress progress = PlayerProgressManager.getProgress(player);
            if (progress == null) return;

            AllocationStatus status = PerkTree.allocate(player, progress, pkt.perkKey);
            if (status == AllocationStatus.SUCCESS) {
                PerkAttributeHelper.applyVanillaModifiers(player, progress.getAllocatedPerks());
                PlayerProgressManager.syncProgress(player);
            } else {
                AstralSorcery.log.debug("Perk allocate denied for {} ({}): {}",
                        player.getName().getString(), status, pkt.perkKey);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
