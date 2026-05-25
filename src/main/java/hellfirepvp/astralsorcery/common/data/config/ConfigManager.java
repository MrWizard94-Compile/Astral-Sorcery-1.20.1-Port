package hellfirepvp.astralsorcery.common.data.config;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Config sync entry point — now a no-op because {@code CommonConfig} is registered
 * as {@link net.minecraftforge.fml.config.ModConfig.Type#SERVER}, so Forge
 * automatically syncs it from server to client on player join.
 *
 * <p>{@link hellfirepvp.astralsorcery.common.network.play.server.PktSyncConfig}
 * is still registered for potential future use (e.g., live reload without reconnect),
 * but is not currently sent.</p>
 */
public final class ConfigManager {

    private ConfigManager() {}

    /**
     * Called when a {@code PktSyncConfig} packet arrives. Currently unused —
     * Forge SERVER-type config sync is the primary mechanism.
     */
    public static void applySyncedConfig(@Nonnull CompoundTag configData) {
        AstralSorcery.log.debug("PktSyncConfig received ({} keys) — Forge SERVER config sync is active", configData.size());
    }
}
