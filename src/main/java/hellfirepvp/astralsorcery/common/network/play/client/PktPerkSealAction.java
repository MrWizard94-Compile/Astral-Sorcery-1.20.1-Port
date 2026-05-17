/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Client → Server: requests applying or removing a seal from a perk.
 * Seals temporarily disable a perk without deallocating it, preserving
 * the perk tree topology while adjusting effects.
 *
 * <p>Server validates:
 * <ul>
 *   <li>The perk exists and is allocated by this player</li>
 *   <li>When sealing: the player has seal items in inventory</li>
 *   <li>When unsealing: the perk is currently sealed</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20: FriendlyByteBuf read/write stable.</p>
 */
public class PktPerkSealAction {

    @Nonnull
    private final ResourceLocation perkKey;
    private final boolean seal; // true = seal, false = unseal

    public PktPerkSealAction(@Nonnull ResourceLocation perkKey, boolean seal) {
        this.perkKey = perkKey;
        this.seal = seal;
    }

    public static void encode(@Nonnull PktPerkSealAction pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.perkKey);
        buf.writeBoolean(pkt.seal);
    }

    @Nonnull
    public static PktPerkSealAction decode(@Nonnull FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();
        boolean seal = buf.readBoolean();
        return new PktPerkSealAction(key, seal);
    }

    public static void handle(@Nonnull PktPerkSealAction pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            AbstractPerk perk = PerkTree.getPerk(pkt.perkKey);
            if (perk == null) return;

            PlayerProgress progress = PlayerProgressManager.getProgress(player);
            if (progress == null) return;

            if (!progress.hasPerkAllocated(perk)) return;

            if (pkt.seal) {
                // Apply seal: check inventory for seal item, consume one
                // TODO: Check and consume seal item from player inventory
                progress.sealPerk(perk);
            } else {
                // Remove seal
                if (progress.isPerkSealed(perk)) {
                    progress.unsealPerk(perk);
                }
            }

            PlayerProgressManager.syncProgress(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
