package hellfirepvp.astralsorcery.common.data.world.base;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's WorldSection.
 * Represents a section of world data covering a grid cell.
 * Used by SectionWorldData to partition data spatially.
 */
public abstract class WorldSection {

    private final int sectionX;
    private final int sectionZ;

    protected WorldSection(int sectionX, int sectionZ) {
        this.sectionX = sectionX;
        this.sectionZ = sectionZ;
    }

    public int getSectionX() {
        return this.sectionX;
    }

    public int getSectionZ() {
        return this.sectionZ;
    }

    /**
     * Write this section's data to NBT.
     */
    public abstract void writeToNBT(@Nonnull CompoundTag tag);

    /**
     * Read this section's data from NBT.
     */
    public abstract void readFromNBT(@Nonnull CompoundTag tag);
}
