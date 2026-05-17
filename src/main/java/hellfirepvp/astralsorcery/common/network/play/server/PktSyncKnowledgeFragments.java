/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → Client: syncs the list of knowledge fragments the player
 * has collected. Knowledge fragments are random loot drops that
 * unlock specific journal entries and recipe knowledge.
 *
 * <p>Distinct from PktSyncResearch — fragments are individual items
 * with specific discovery text, while research tracks broad
 * progression milestones.</p>
 *
 * <p>1.16 → 1.20: FriendlyByteBuf read/write stable.</p>
 */
public class PktSyncKnowledgeFragments {

    @Nonnull
    private final List<ResourceLocation> fragmentKeys;

    public PktSyncKnowledgeFragments(@Nonnull List<ResourceLocation> fragmentKeys) {
        this.fragmentKeys = fragmentKeys;
    }

    public static void encode(@Nonnull PktSyncKnowledgeFragments pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.fragmentKeys.size());
        for (ResourceLocation key : pkt.fragmentKeys) {
            buf.writeResourceLocation(key);
        }
    }

    @Nonnull
    public static PktSyncKnowledgeFragments decode(@Nonnull FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<ResourceLocation> keys = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keys.add(buf.readResourceLocation());
        }
        return new PktSyncKnowledgeFragments(keys);
    }

    public static void handle(@Nonnull PktSyncKnowledgeFragments pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: update local fragment knowledge
            // TODO: KnowledgeFragmentManager.setClientFragments(pkt.fragmentKeys);
        });
        ctx.get().setPacketHandled(true);
    }
}
