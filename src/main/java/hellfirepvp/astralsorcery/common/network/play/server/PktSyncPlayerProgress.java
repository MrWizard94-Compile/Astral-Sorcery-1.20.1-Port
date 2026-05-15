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
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Server -> Client packet that syncs the full {@link PlayerProgress} data
 * to the client player. Sent on login, dimension change, or when the server
 * modifies progression data.
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag,
 * PacketBuffer -> FriendlyByteBuf,
 * NetworkEvent.Context handler pattern unchanged</p>
 */
public class PktSyncPlayerProgress {

    @Nullable
    private final CompoundTag progressData;

    /**
     * Creates a sync packet from the given player's current progress.
     *
     * @param progress the player progress to serialize, or null for an empty sync
     */
    public PktSyncPlayerProgress(@Nullable PlayerProgress progress) {
        this.progressData = progress != null ? progress.writeToNBT() : null;
    }

    private PktSyncPlayerProgress(@Nullable CompoundTag progressData) {
        this.progressData = progressData;
    }

    public static void encode(@Nonnull PktSyncPlayerProgress msg, @Nonnull FriendlyByteBuf buf) {
        buf.writeBoolean(msg.progressData != null);
        if (msg.progressData != null) {
            buf.writeNbt(msg.progressData);
        }
    }

    @Nonnull
    public static PktSyncPlayerProgress decode(@Nonnull FriendlyByteBuf buf) {
        boolean hasData = buf.readBoolean();
        CompoundTag tag = hasData ? buf.readNbt() : null;
        return new PktSyncPlayerProgress(tag);
    }

    public static void handle(@Nonnull PktSyncPlayerProgress msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg))
        );
        ctx.setPacketHandled(true);
    }

    private static void handleClient(@Nonnull PktSyncPlayerProgress msg) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (msg.progressData == null) {
            AstralSorcery.log.warn("Received empty progress sync packet");
            return;
        }
        player.getCapability(PlayerCapabilityProvider.CAPABILITY).ifPresent(progress -> {
            progress.readFromNBT(msg.progressData);
            AstralSorcery.log.debug("Synced player progress from server");
        });
    }
}
