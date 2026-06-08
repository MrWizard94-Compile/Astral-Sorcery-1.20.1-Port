/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.storage;

import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Represents a single storage network: a collection of core block-entity positions
 * each with an associated bounding box, plus an optional master core designation.
 *
 * <p>The master is the authoritative position used as the key in
 * {@link StorageNetworkBuffer}. If no master is explicitly set, the network is
 * treated as masterless until one is assigned.</p>
 *
 * <p>1.16 → 1.20: AxisAlignedBB → AABB, CompoundNBT/ListNBT → CompoundTag/ListTag,
 * Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND.</p>
 */
public class StorageNetwork {

    @Nullable
    private CoreArea master = null;
    private final Map<BlockPos, AABB> cores = Maps.newHashMap();

    /**
     * Designates a core as the network master. The position must already be
     * registered via {@link #addCore}. Passing null clears the master.
     *
     * @return true if successfully set (or cleared)
     */
    public boolean setMaster(@Nullable BlockPos pos) {
        if (pos == null) {
            this.master = null;
            return true;
        }
        AABB box = this.cores.get(pos);
        if (box != null) {
            this.master = new CoreArea(pos, box);
            return true;
        }
        return false;
    }

    @Nullable
    public CoreArea getMaster() {
        return master;
    }

    /**
     * Registers a core at {@code pos} with its associated influence {@code box}.
     *
     * @return true if this position was not already registered (no overwrite)
     */
    public boolean addCore(@Nonnull BlockPos pos, @Nonnull AABB box) {
        return this.cores.put(pos, box) == null;
    }

    /**
     * Removes a core registration.
     *
     * @return true if the position was present
     */
    public boolean removeCore(@Nonnull BlockPos pos) {
        return this.cores.remove(pos) != null;
    }

    /** Returns all registered cores as a list of {@link CoreArea} snapshots. */
    @Nonnull
    public List<CoreArea> getCores() {
        return MapStream.of(this.cores).toList(CoreArea::new);
    }

    public void writeToNBT(@Nonnull CompoundTag tag) {
        ListTag list = new ListTag();
        for (CoreArea coreData : this.getCores()) {
            CompoundTag coreTag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(coreData.getPos(), coreTag);
            NBTHelper.writeBoundingBox(coreData.getOffsetBox(), coreTag);
            list.add(coreTag);
        }
        tag.put("cores", list);

        CoreArea masterArea = getMaster();
        if (masterArea != null) {
            NBTHelper.setAsSubTag(tag, "master",
                    nbt -> NBTHelper.writeBlockPosToNBT(masterArea.getPos(), nbt));
        }
    }

    public void readFromNBT(@Nonnull CompoundTag tag) {
        this.cores.clear();

        ListTag list = tag.getList("cores", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag coreTag = list.getCompound(i);
            BlockPos pos = NBTHelper.readBlockPosFromNBT(coreTag);
            AABB box = NBTHelper.readBoundingBox(coreTag);
            this.addCore(pos, box);
        }

        this.setMaster(NBTHelper.readFromSubTag(tag, "master", NBTHelper::readBlockPosFromNBT));
    }

    /**
     * Snapshot of a single core's position and offset bounding box.
     * The "real" box is obtained by offsetting the stored box by the core position.
     */
    public static class CoreArea {

        private final BlockPos pos;
        private final AABB offsetBox;

        public CoreArea(@Nonnull BlockPos pos, @Nonnull AABB offsetBox) {
            this.pos = pos;
            this.offsetBox = offsetBox;
        }

        @Nonnull
        public BlockPos getPos() {
            return pos;
        }

        /** The bounding box relative to the core position. */
        @Nonnull
        public AABB getOffsetBox() {
            return offsetBox;
        }

        /** The bounding box in absolute world coordinates. */
        @Nonnull
        public AABB getRealBox() {
            return offsetBox.move(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
