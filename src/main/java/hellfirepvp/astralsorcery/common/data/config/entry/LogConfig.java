package hellfirepvp.astralsorcery.common.data.config.entry;

import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration entry for debug logging categories.
 * Delegates to {@link CommonConfig#debugLogging} which is populated during config build.
 */
public class LogConfig {

    public static final LogConfig CONFIG = new LogConfig();

    private LogConfig() {}

    /**
     * Check if logging is enabled for the given category.
     * Reads from the Forge config; falls back to {@code false} if config is not yet loaded.
     */
    public boolean isLoggingEnabled(@Nonnull LogCategory category) {
        @Nullable ForgeConfigSpec.BooleanValue entry =
                CommonConfig.CONFIG.debugLogging.get(category);
        return entry != null && entry.get();
    }
}
