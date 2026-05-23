package hellfirepvp.astralsorcery.common.data.config;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Manages server→client config synchronization.
 * The server serializes selected config values to NBT in
 * {@link hellfirepvp.astralsorcery.common.network.play.server.PktSyncConfig}
 * and the client applies them here so both sides use identical values.
 *
 * <p>Currently a stub — concrete value reads/writes are added when
 * server-authoritative config keys are identified.</p>
 */
public final class ConfigManager {

    private ConfigManager() {}

    /**
     * Applies server-provided config overrides to the client's local config state.
     * Called on the client when a {@code PktSyncConfig} is received.
     *
     * @param configData the NBT compound from the server
     */
    public static void applySyncedConfig(@Nonnull CompoundTag configData) {
        // No server-authoritative overrides defined yet.
        // Log at debug level so we know the packet arrived.
        AstralSorcery.log.debug("PktSyncConfig received ({} keys)", configData.size());
    }
}
