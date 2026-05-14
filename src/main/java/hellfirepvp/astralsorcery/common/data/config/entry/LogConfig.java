package hellfirepvp.astralsorcery.common.data.config.entry;

import hellfirepvp.astralsorcery.common.util.log.LogCategory;

import javax.annotation.Nonnull;

/**
 * Configuration entry for debug logging categories.
 * Stub for Phase 4 — will be expanded when the full config system is ported.
 */
public class LogConfig {

    public static final LogConfig CONFIG = new LogConfig();

    private LogConfig() {}

    /**
     * Check if logging is enabled for the given category.
     * Default: enabled in dev, disabled in prod. For now, always enabled.
     *
     * @param category the log category
     * @return true if logging for this category is active
     */
    public boolean isLoggingEnabled(@Nonnull LogCategory category) {
        // TODO: Wire to Forge config once config system is ported
        return true;
    }
}
