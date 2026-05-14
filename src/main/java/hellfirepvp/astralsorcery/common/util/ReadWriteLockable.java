package hellfirepvp.astralsorcery.common.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

/**
 * Interface providing convenient read/write lock operations.
 * Implementors provide the lock; default methods handle lock/unlock.
 */
public interface ReadWriteLockable {

    ReadWriteLock getLock();

    default <T> T write(Supplier<T> fn) {
        return this.lock(this.getLock()::writeLock, fn);
    }

    default void write(Runnable run) {
        this.lock(this.getLock()::writeLock, nullSupplier(run));
    }

    default <T> T read(Supplier<T> fn) {
        return this.lock(this.getLock()::readLock, fn);
    }

    default void read(Runnable run) {
        this.lock(this.getLock()::readLock, nullSupplier(run));
    }

    default <T> T lock(Supplier<Lock> lock, Supplier<T> fn) {
        lock.get().lock();
        try {
            return fn.get();
        } finally {
            lock.get().unlock();
        }
    }

    /**
     * Wraps a Runnable as a Supplier returning null.
     * Inlined from MiscUtils to avoid circular dependency.
     */
    private static <T> Supplier<T> nullSupplier(Runnable run) {
        return () -> {
            run.run();
            return null;
        };
    }
}
