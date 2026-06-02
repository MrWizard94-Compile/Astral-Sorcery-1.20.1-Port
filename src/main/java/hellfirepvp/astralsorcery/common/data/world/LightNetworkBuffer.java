package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.common.data.world.base.GlobalWorldData;
import hellfirepvp.astralsorcery.common.data.world.base.WorldCacheDomain;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Stores the per-world starlight transmission network state.
 *
 * <p>This is the SavedData backing for the starlight network:
 * sources, receivers, transmissions, and link topology.
 * The full implementation is in Phase C (starlight network port).
 * This stub allows DataAS and the WorldCacheDomain system to compile.</p>
 */
public class LightNetworkBuffer extends GlobalWorldData {

    public LightNetworkBuffer(@Nonnull WorldCacheDomain.SaveKey<?> key) {
        super(key);
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag tag) {
        // Phase C: deserialize network topology
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag tag) {
        // Phase C: serialize network topology
    }
}
