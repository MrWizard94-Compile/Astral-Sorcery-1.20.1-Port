/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Server → Client: Full synchronization of the player's perk allocation state.
 * Sent on login, dimension change, and after any allocation/deallocation.
 * Contains the full set of allocated perk keys and current perk exp.
 */
public class PktSyncPerkData {

    @Nonnull
    private final Set<ResourceLocation> allocatedPerks;
    private final long perkExp;
    private final int availablePoints;

    public PktSyncPerkData(@Nonnull Set<ResourceLocation> allocatedPerks,
                           long perkExp, int availablePoints) {
        this.allocatedPerks = allocatedPerks;
        this.perkExp = perkExp;
        this.availablePoints = availablePoints;
    }

    public static void encode(@Nonnull PktSyncPerkData pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.allocatedPerks.size());
        for (ResourceLocation key : pkt.allocatedPerks) {
            buf.writeResourceLocation(key);
        }
        buf.writeVarLong(pkt.perkExp);
        buf.writeVarInt(pkt.availablePoints);
    }

    @Nonnull
    public static PktSyncPerkData decode(@Nonnull FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Set<ResourceLocation> perks = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            perks.add(buf.readResourceLocation());
        }
        long exp = buf.readVarLong();
        int points = buf.readVarInt();
        return new PktSyncPerkData(perks, exp, points);
    }

    public static void handle(@Nonnull PktSyncPerkData pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(pkt));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktSyncPerkData pkt) {
        PlayerProgress progress = PlayerProgressManager.getClientProgress();
        progress.clearAllocatedPerks();
        pkt.allocatedPerks.forEach(progress::allocatePerk);
        progress.setPerkExp(pkt.perkExp);
        progress.setPerkPoints(pkt.availablePoints);
    }

    @Nonnull
    public Set<ResourceLocation> getAllocatedPerks() {
        return allocatedPerks;
    }

    public long getPerkExp() {
        return perkExp;
    }

    public int getAvailablePoints() {
        return availablePoints;
    }
}
