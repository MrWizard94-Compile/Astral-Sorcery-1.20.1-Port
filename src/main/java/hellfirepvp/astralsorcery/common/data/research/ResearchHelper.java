package hellfirepvp.astralsorcery.common.data.research;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Facade for accessing player research data on both sides.
 *
 * <p>Client-side delegates to {@link PlayerProgressManager#getClientProgress()}.
 * Server-side reads the player capability directly.
 *
 * <p>1.16 → 1.20: removed file-I/O and UUID-map server cache —
 * server progress is now stored as a Forge capability on the player entity
 * and never held in a static map.</p>
 */
public final class ResearchHelper {

    private ResearchHelper() {}

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static PlayerProgress getClientProgress() {
        return PlayerProgressManager.getClientProgress();
    }

    @Nonnull
    public static PlayerProgress getProgress(@Nonnull ServerPlayer player) {
        PlayerProgress p = PlayerProgressManager.getProgress(player);
        return p != null ? p : new PlayerProgress();
    }
}
