package hellfirepvp.astralsorcery.common.data.world.base;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Replacement for ObserverLib's SectionWorldData.
 * Partitions world data into grid sections for spatial access.
 * Thread-safe reads/writes via ReadWriteLock.
 *
 * Used for: RockCrystalBuffer, LightNetworkBuffer.
 *
 * @param <S> the section type
 */
public abstract class SectionWorldData<S extends WorldSection> extends BaseWorldData {

    private final int precision;
    private final Map<Long, S> sections = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected SectionWorldData(@Nonnull WorldCacheDomain.SaveKey<?> key, int precision) {
        this.precision = precision;
    }

    /**
     * Create a new empty section for the given grid coordinates.
     */
    @Nonnull
    protected abstract S createNewSection(int sectionX, int sectionZ);

    private int toSectionCoord(int blockCoord) {
        return Math.floorDiv(blockCoord, this.precision);
    }

    private long sectionKey(int sectionX, int sectionZ) {
        return ((long) sectionX << 32) | (sectionZ & 0xFFFFFFFFL);
    }

    /**
     * Get the section containing the given block position, or null if none exists.
     */
    @Nullable
    public S getSection(@Nonnull BlockPos pos) {
        int sx = toSectionCoord(pos.getX());
        int sz = toSectionCoord(pos.getZ());
        return this.sections.get(sectionKey(sx, sz));
    }

    /**
     * Get or create the section containing the given block position.
     */
    @Nonnull
    public S getOrCreateSection(@Nonnull BlockPos pos) {
        int sx = toSectionCoord(pos.getX());
        int sz = toSectionCoord(pos.getZ());
        long key = sectionKey(sx, sz);
        return this.sections.computeIfAbsent(key, k -> createNewSection(sx, sz));
    }

    /**
     * Execute a read operation under the read lock.
     */
    public void read(@Nonnull Runnable fn) {
        this.lock.readLock().lock();
        try {
            fn.run();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Execute a write operation under the write lock.
     */
    public void write(@Nonnull Runnable fn) {
        this.lock.writeLock().lock();
        try {
            fn.run();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Mark a section as dirty so the data gets saved.
     */
    public void markDirty(@Nonnull S section) {
        this.setDirty();
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag tag) {
        this.sections.clear();
        ListTag list = tag.getList("sections", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag sectionTag = list.getCompound(i);
            int sx = sectionTag.getInt("sX");
            int sz = sectionTag.getInt("sZ");
            S section = createNewSection(sx, sz);
            section.readFromNBT(sectionTag);
            this.sections.put(sectionKey(sx, sz), section);
        }
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag tag) {
        ListTag list = new ListTag();
        for (S section : this.sections.values()) {
            CompoundTag sectionTag = new CompoundTag();
            sectionTag.putInt("sX", section.getSectionX());
            sectionTag.putInt("sZ", section.getSectionZ());
            section.writeToNBT(sectionTag);
            list.add(sectionTag);
        }
        tag.put("sections", list);
    }
}
