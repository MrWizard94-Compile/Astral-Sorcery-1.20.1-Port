/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of starlight network topology received from the server
 * via {@link hellfirepvp.astralsorcery.common.network.play.server.PktSyncStarlightNetwork}.
 *
 * <p>The client renderer reads from this cache to draw transmission beams
 * between source and target positions. Thread-safe since rendering and
 * network packet handling can run on different threads.</p>
 *
 * <p>This is a pure data cache with no game logic — the server's
 * {@link WorldNetworkHandler} is the source of truth.</p>
 */
public final class ClientStarlightNetworkCache {

    private ClientStarlightNetworkCache() {}

    /**
     * Source position -> (list of reachable target positions, current starlight level).
     */
    private static final Map<BlockPos, SourceRenderData> CACHE = new ConcurrentHashMap<>();

    /**
     * Updates the cache entry for a source. Called by the sync packet handler.
     *
     * @param source    the starlight source position
     * @param targets   all positions reachable from this source
     * @param starlight current starlight production level
     */
    public static void updateSource(@Nonnull BlockPos source,
                                    @Nonnull List<BlockPos> targets,
                                    double starlight) {
        // Debug: log cache updates to trace server->client sync
        AstralSorcery.log.debug("Client cache update for source {}: targets={}, starlight={}",
                source.toShortString(), targets.size(), starlight);
        if (targets.isEmpty() && starlight <= 0) {
            CACHE.remove(source);
        } else {
            CACHE.put(source.immutable(), new SourceRenderData(
                    List.copyOf(targets), starlight));
        }
    }

    /**
     * Gets the render data for a source, or null if not cached.
     */
    @Nullable
    public static SourceRenderData getSourceData(@Nonnull BlockPos source) {
        return CACHE.get(source);
    }

    /**
     * Gets all cached source positions.
     */
    @Nonnull
    public static Map<BlockPos, SourceRenderData> getAllSources() {
        return Collections.unmodifiableMap(CACHE);
    }

    /**
     * Clears the entire cache. Called on dimension change or disconnect.
     */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * Removes a specific source from the cache.
     */
    public static void removeSource(@Nonnull BlockPos source) {
        CACHE.remove(source);
    }

    /**
     * Immutable render data for a single source.
     *
     * @param targets   positions reachable from this source
     * @param starlight current production level (for beam brightness)
     */
    public record SourceRenderData(
            @Nonnull List<BlockPos> targets,
            double starlight
    ) {}
}
