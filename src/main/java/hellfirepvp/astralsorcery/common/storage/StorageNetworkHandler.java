/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.storage;

import hellfirepvp.astralsorcery.common.data.world.StorageNetworkBuffer;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-dimension entry point into the storage network system. Maintains one
 * {@link NetworkHelper} per dimension, each backed by the dimension's
 * {@link StorageNetworkBuffer} (a {@link net.minecraft.world.level.saveddata.SavedData}
 * instance loaded via {@link DataAS#KEY_STORAGE_NETWORK}).
 *
 * <p>1.16 → 1.20: RegistryKey → ResourceKey, World → Level/ServerLevel,
 * DataAS domain access unchanged.</p>
 */
public class StorageNetworkHandler {

    private static final Map<ResourceKey<Level>, NetworkHelper> mappingHelpers = new HashMap<>();

    /**
     * Returns the {@link NetworkHelper} for the given level. Returns null for
     * client-side levels (storage network is server-authoritative).
     */
    @Nullable
    public static NetworkHelper getHandler(@Nullable Level level) {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return mappingHelpers.computeIfAbsent(
                serverLevel.dimension(),
                dim -> new NetworkHelper(serverLevel));
    }

    /**
     * Clears the cached handler for the given level. Call this when the level
     * unloads to avoid holding stale references.
     */
    public static void clearHandler(@Nonnull Level level) {
        clearHandler(level.dimension());
    }

    public static void clearHandler(@Nonnull ResourceKey<Level> dimKey) {
        mappingHelpers.remove(dimKey);
    }

    /** Thin wrapper over {@link StorageNetworkBuffer} for a specific dimension. */
    public static class NetworkHelper {

        private final StorageNetworkBuffer buffer;

        private NetworkHelper(@Nonnull ServerLevel level) {
            this.buffer = DataAS.DOMAIN_AS.getData(level, DataAS.KEY_STORAGE_NETWORK);
        }

        @Nullable
        public StorageNetwork getNetwork(@Nonnull BlockPos networkMasterPos) {
            return buffer.getNetwork(networkMasterPos);
        }
    }

    /**
     * Marker object carried to {@link IStorageNetworkTile#receiveMappingChange}
     * when the network topology changes. Carry additional diff data here as the
     * chalice/lightwell system is fleshed out.
     */
    public static class MappingChange {
    }
}
