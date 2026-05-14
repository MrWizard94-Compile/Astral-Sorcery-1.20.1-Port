package hellfirepvp.astralsorcery.common.util.log;

import hellfirepvp.astralsorcery.common.data.config.entry.LogConfig;

import java.util.function.Supplier;

/**
 * Debug logging categories for Astral Sorcery.
 * Each category can be independently enabled/disabled via config.
 */
public enum LogCategory {

    PERKS,
    UNINTENDED_CHUNK_LOADING,
    STRUCTURE_MATCH,
    GATEWAY_CACHE;

    public boolean isEnabled() {
        return LogConfig.CONFIG.isLoggingEnabled(this);
    }

    public void info(Supplier<String> message) {
        LogUtil.info(this, message);
    }

    public void warn(Supplier<String> message) {
        LogUtil.warn(this, message);
    }
}
