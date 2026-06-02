package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.common.data.world.base.GlobalWorldData;
import hellfirepvp.astralsorcery.common.data.world.base.WorldCacheDomain;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Per-world storage network state (chalice fluid linking network).
 *
 * <p>The 1.16 storage network was simplified in the port to a vanilla capability
 * approach via {@link hellfirepvp.astralsorcery.common.auxiliary.StorageNetworkHelper}.
 * This stub satisfies the DataAS save key declaration; the capability approach
 * remains the primary mechanism for storage.</p>
 */
public class StorageNetworkBuffer extends GlobalWorldData {

    public StorageNetworkBuffer(@Nonnull WorldCacheDomain.SaveKey<?> key) {
        super(key);
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag tag) {
        // Simplified: storage handled via capability; buffer only tracks chalice links
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag tag) {
        // Simplified: storage handled via capability
    }
}
