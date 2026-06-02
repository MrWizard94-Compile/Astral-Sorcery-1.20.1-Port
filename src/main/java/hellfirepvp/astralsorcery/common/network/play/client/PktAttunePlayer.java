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
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Client -> Server packet requesting player attunement to a constellation.
 * The server validates the request (constellation exists, player has
 * required tier, etc.) before applying the attunement.
 *
 * <p>1.16 -> 1.20 changes:
 * PacketBuffer -> FriendlyByteBuf,
 * ServerPlayerEntity -> ServerPlayer,
 * ResourceLocation read/write unchanged</p>
 */
public class PktAttunePlayer {

    @Nonnull
    private final ResourceLocation constellation;

    /**
     * @param constellation the registry name of the constellation to attune to
     */
    public PktAttunePlayer(@Nonnull ResourceLocation constellation) {
        this.constellation = constellation;
    }

    @Nonnull
    public ResourceLocation getConstellation() {
        return constellation;
    }

    public static void encode(@Nonnull PktAttunePlayer msg, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.constellation);
    }

    @Nonnull
    public static PktAttunePlayer decode(@Nonnull FriendlyByteBuf buf) {
        ResourceLocation constellation = buf.readResourceLocation();
        return new PktAttunePlayer(constellation);
    }

    public static void handle(@Nonnull PktAttunePlayer msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            @Nullable ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                AstralSorcery.log.warn("Received PktAttunePlayer with null sender");
                return;
            }

            // Validate the constellation exists
            IConstellation target = ConstellationRegistry.getConstellation(msg.constellation);
            if (target == null) {
                AstralSorcery.log.warn("Player {} tried to attune to unknown constellation: {}",
                        sender.getName().getString(), msg.constellation);
                return;
            }

            // Validate player has the required progression tier
            PlayerProgress progress = PlayerProgressHelper.getProgress(sender);
            if (progress == null) {
                AstralSorcery.log.warn("Player {} missing progress capability on attunement request",
                        sender.getName().getString());
                return;
            }
            if (!progress.isAtLeast(ProgressionTier.ATTUNEMENT)) {
                AstralSorcery.log.warn("Player {} tried to attune without reaching ATTUNEMENT tier",
                        sender.getName().getString());
                return;
            }

            // Apply attunement (discovers constellation, grants tier, fires advancement trigger, syncs)
            ResearchManager.attuneTo(sender, msg.constellation);
            AstralSorcery.log.info("Player {} attuned to constellation {}",
                    sender.getName().getString(), msg.constellation);
        });
        ctx.setPacketHandled(true);
    }
}
