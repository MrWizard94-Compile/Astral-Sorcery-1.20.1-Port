package hellfirepvp.astralsorcery.common.starlight;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Interface for starlight sources that persist their state independently
 * of whether their chunk is loaded. Used by the world network handler
 * to track source contribution even across chunk boundaries.
 *
 * <p>1.16 -> 1.20 changes: CompoundNBT -> CompoundTag</p>
 */
public interface IIndependentStarlightSource extends IStarlightSource {

    /**
     * Serialize this source's state for the world network data.
     */
    @Nonnull
    CompoundTag serializeSourceNBT();

    /**
     * Deserialize this source's state from world network data.
     */
    void deserializeSourceNBT(@Nonnull CompoundTag tag);

    /**
     * Whether this source should continue producing even when its chunk is unloaded.
     */
    boolean providesAutoLink();
}
