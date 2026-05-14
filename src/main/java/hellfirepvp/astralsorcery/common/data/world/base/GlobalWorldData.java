package hellfirepvp.astralsorcery.common.data.world.base;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's GlobalWorldData.
 * A single global data instance per world (not sectioned).
 * Used for: GatewayCache, StorageNetworkBuffer.
 */
public abstract class GlobalWorldData extends BaseWorldData {

    private final WorldCacheDomain.SaveKey<?> key;

    protected GlobalWorldData(@Nonnull WorldCacheDomain.SaveKey<?> key) {
        this.key = key;
    }

    @Nonnull
    public WorldCacheDomain.SaveKey<?> getSaveKey() {
        return this.key;
    }

    /**
     * Convenience: marks this data as needing persistence.
     */
    @Override
    public void markDirtyFlag() {
        super.setDirty();
    }
}
