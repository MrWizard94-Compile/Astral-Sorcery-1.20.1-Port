/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Server → Client: syncs the player's constellation attunement.
 * Sent when a player attunes to a constellation at the attunement altar,
 * or when their attunement changes for any reason.
 *
 * <p>The client uses this to update the local attunement display
 * (perk tree root, HUD indicator, and journal entries).</p>
 *
 * <p>1.16 → 1.20: FriendlyByteBuf read/write stable.</p>
 */
public class PktSyncAttunement {

    @Nullable
    private final ResourceLocation constellation;

    public PktSyncAttunement(@Nullable ResourceLocation constellation) {
        this.constellation = constellation;
    }

    public static void encode(@Nonnull PktSyncAttunement pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.constellation != null);
        if (pkt.constellation != null) {
            buf.writeResourceLocation(pkt.constellation);
        }
    }

    @Nonnull
    public static PktSyncAttunement decode(@Nonnull FriendlyByteBuf buf) {
        boolean hasAttunement = buf.readBoolean();
        ResourceLocation constellation = hasAttunement ? buf.readResourceLocation() : null;
        return new PktSyncAttunement(constellation);
    }

    public static void handle(@Nonnull PktSyncAttunement pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: update local player progress attunement
            PlayerProgress progress = PlayerProgressManager.getClientProgress();
            if (progress != null) {
                progress.setAttunedConstellation(pkt.constellation);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
