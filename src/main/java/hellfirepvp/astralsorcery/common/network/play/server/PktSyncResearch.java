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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → Client: syncs the player's research/knowledge state.
 * Sent on login, when new research is unlocked, or when the journal
 * is opened to ensure the client display is up-to-date.
 *
 * <p>Contains the list of all research keys (resource locations) the
 * player has unlocked. The client uses these to determine which
 * journal pages, recipes, and knowledge entries to display.</p>
 *
 * <p>1.16 → 1.20: FriendlyByteBuf read/write stable.</p>
 */
public class PktSyncResearch {

    @Nonnull
    private final List<ResourceLocation> unlockedResearch;

    public PktSyncResearch(@Nonnull List<ResourceLocation> unlockedResearch) {
        this.unlockedResearch = unlockedResearch;
    }

    public static void encode(@Nonnull PktSyncResearch pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.unlockedResearch.size());
        for (ResourceLocation key : pkt.unlockedResearch) {
            buf.writeResourceLocation(key);
        }
    }

    @Nonnull
    public static PktSyncResearch decode(@Nonnull FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<ResourceLocation> keys = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keys.add(buf.readResourceLocation());
        }
        return new PktSyncResearch(keys);
    }

    public static void handle(@Nonnull PktSyncResearch pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: update local research state
            PlayerProgress progress = PlayerProgressManager.getClientProgress();
            if (progress != null) {
                progress.setUnlockedResearch(pkt.unlockedResearch);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
