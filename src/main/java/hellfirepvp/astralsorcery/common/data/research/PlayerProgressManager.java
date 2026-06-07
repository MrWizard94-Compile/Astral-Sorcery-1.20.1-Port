package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.common.capability.PlayerCapabilityProvider;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPlayerProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages access to player progress data. Provides server-side progress
 * lookup via capabilities and client-side cached progress storage.
 *
 * <p>Server-side: progress is stored as a Forge capability on the player.
 * Client-side: a cached copy is maintained, updated by sync packets.</p>
 *
 * <p>1.16 → 1.20: Capability access patterns unchanged.
 * Client cache stored as static field for dist-safe access.</p>
 */
public final class PlayerProgressManager {

    private PlayerProgressManager() {}

    /** Client-side cached progress, updated by sync packets. */
    @OnlyIn(Dist.CLIENT)
    private static PlayerProgress clientProgress;

    /**
     * Gets the progress for a server player via their capability.
     *
     * @param player the server player
     * @return the player's progress, or null if capability not attached
     */
    @Nullable
    public static PlayerProgress getProgress(@Nonnull ServerPlayer player) {
        return player.getCapability(PlayerCapabilityProvider.CAPABILITY)
                .resolve().orElse(null);
    }

    /**
     * Gets the client-side cached progress.
     * Only call from client-side code.
     *
     * @return the cached client progress
     */
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static PlayerProgress getClientProgress() {
        if (clientProgress == null) {
            clientProgress = new PlayerProgress();
        }
        return clientProgress;
    }

    /**
     * Replaces the client-side cached progress.
     * Called by sync packets when the server sends updated progress data.
     *
     * @param progress the new progress to cache
     */
    @OnlyIn(Dist.CLIENT)
    public static void setClientProgress(@Nonnull PlayerProgress progress) {
        clientProgress = progress;
    }

    /**
     * Syncs the server-side progress to the given player's client.
     * Sends a full progress sync packet.
     *
     * @param player the server player to sync
     */
    public static void syncProgress(@Nonnull ServerPlayer player) {
        PlayerProgress progress = getProgress(player);
        if (progress != null) {
            PacketChannel.sendToPlayer(new PktSyncPlayerProgress(progress), player);
        }
    }
}
