package hellfirepvp.astralsorcery.common.util;

import javax.annotation.Nullable;

/**
 * Callback interface for server start/stop lifecycle events.
 */
public interface ServerLifecycleListener {

    void onServerStart();

    void onServerStop();

    static ServerLifecycleListener stop(Runnable onStop) {
        return wrap(null, onStop);
    }

    static ServerLifecycleListener start(Runnable onStart) {
        return wrap(onStart, null);
    }

    static ServerLifecycleListener wrap(@Nullable Runnable onStart, @Nullable Runnable onStop) {
        return new ServerLifecycleListener() {
            @Override
            public void onServerStart() {
                if (onStart != null) {
                    onStart.run();
                }
            }

            @Override
            public void onServerStop() {
                if (onStop != null) {
                    onStop.run();
                }
            }
        };
    }
}
