package hellfirepvp.astralsorcery.common.util.world;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestSeed;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client-side cache of world seeds, requested from server via network packets.
 * Seeds are used for deterministic constellation placement calculations.
 *
 * <p>1.16 -> 1.20 changes:
 * RegistryKey&lt;World&gt; -> ResourceKey&lt;Level&gt;</p>
 */
public class WorldSeedCache {

    private static long lastServerQuery = 0L;
    private static int activeSession = 0;

    private static final Map<ResourceKey<Level>, Long> cacheSeedLookup = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void clearClient() {
        activeSession++;
        cacheSeedLookup.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateSeedCache(@Nonnull ResourceKey<Level> dim, int session, long seed) {
        if (activeSession == session) {
            cacheSeedLookup.put(dim, seed);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static Optional<Long> getSeedIfPresent(@Nullable ResourceKey<Level> dim) {
        if (dim == null) {
            return Optional.empty();
        }
        if (!cacheSeedLookup.containsKey(dim)) {
            long current = System.currentTimeMillis();
            if (current - lastServerQuery > 5_000) {
                lastServerQuery = current;
                activeSession++;
                PacketChannel.sendToServer(new PktRequestSeed(activeSession, dim));
            }
            return Optional.empty();
        }
        return Optional.of(cacheSeedLookup.get(dim));
    }

    public static int getActiveSession() {
        return activeSession;
    }
}
