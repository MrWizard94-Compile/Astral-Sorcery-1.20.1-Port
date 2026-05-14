package hellfirepvp.astralsorcery.common.data.world.base;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's world data base classes.
 * Extends Forge/Minecraft's SavedData for persistence.
 * Subclasses implement readFromNBT/writeToNBT for serialization.
 */
public abstract class BaseWorldData extends SavedData {

    /**
     * Read this data from the given NBT compound.
     *
     * @param tag the saved NBT data
     */
    public abstract void readFromNBT(@Nonnull CompoundTag tag);

    /**
     * Write this data to the given NBT compound.
     *
     * @param tag the tag to write into
     */
    public abstract void writeToNBT(@Nonnull CompoundTag tag);

    /**
     * Called by Minecraft's SavedData system to load data.
     * Delegates to our readFromNBT method.
     */
    public void load(@Nonnull CompoundTag tag) {
        readFromNBT(tag);
    }

    /**
     * Called by Minecraft's SavedData system to save data.
     * Delegates to our writeToNBT method.
     */
    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag) {
        writeToNBT(tag);
        return tag;
    }

    /**
     * Mark this data as dirty so it gets saved on the next world save.
     */
    public void markDirtyFlag() {
        this.setDirty();
    }
}
