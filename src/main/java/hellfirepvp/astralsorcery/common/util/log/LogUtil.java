package hellfirepvp.astralsorcery.common.util.log;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.config.entry.LogConfig;

import java.util.function.Supplier;

/**
 * Conditional debug logging. Messages are only evaluated if the
 * corresponding category is enabled in config.
 */
public class LogUtil {

    private static final String PREFIX = "[DEBUG-%s]: %s";

    public static void info(LogCategory category, Supplier<String> msgProvider) {
        if (LogConfig.CONFIG.isLoggingEnabled(category)) {
            AstralSorcery.log.info(String.format(PREFIX, category.name(), msgProvider.get()));
        }
    }

    public static void warn(LogCategory category, Supplier<String> msgProvider) {
        if (LogConfig.CONFIG.isLoggingEnabled(category)) {
            AstralSorcery.log.warn(String.format(PREFIX, category.name(), msgProvider.get()));
        }
    }
}
