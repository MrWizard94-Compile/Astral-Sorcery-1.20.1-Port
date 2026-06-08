/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.data.world.base.GlobalWorldData;
import hellfirepvp.astralsorcery.common.data.world.base.WorldCacheDomain;
import hellfirepvp.astralsorcery.common.storage.StorageNetwork;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Per-world persistent storage for the AS storage network (chalice/lightwell linking).
 *
 * <p>Stores a map of master-pos → {@link StorageNetwork} and a chunk-indexed lookup
 * map for fast spatial queries. The chunk map is rebuilt from the master map on load
 * and whenever the topology changes.</p>
 *
 * <p>1.16 → 1.20: ObserverLib GlobalWorldData → port's own GlobalWorldData,
 * AxisAlignedBB → AABB, CompoundNBT/ListNBT → CompoundTag/ListTag,
 * Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND. The {@code rand} field that was
 * provided by ObserverLib's base class is replaced by a local {@link Random}.</p>
 */
public class StorageNetworkBuffer extends GlobalWorldData {

    private final Map<BlockPos, StorageNetwork> rawNetworks = Maps.newHashMap();
    private final Map<ChunkPos, List<StorageNetwork>> availableNetworks = Maps.newHashMap();

    private final Random rand = new Random();

    public StorageNetworkBuffer(@Nonnull WorldCacheDomain.SaveKey<?> key) {
        super(key);
    }

    @Nullable
    public StorageNetwork getNetwork(@Nonnull BlockPos masterPos) {
        return this.rawNetworks.get(masterPos);
    }

    /**
     * Rebuilds the chunk-indexed lookup from the current set of raw networks.
     * Called after any topology change or on load.
     */
    private void rebuildAccessContext() {
        this.availableNetworks.clear();

        for (StorageNetwork network : this.rawNetworks.values()) {
            for (StorageNetwork.CoreArea core : network.getCores()) {
                AABB box = core.getRealBox();
                ChunkPos from = Vector3.getMin(box).toChunkPos();
                ChunkPos to   = Vector3.getMax(box).toChunkPos();

                for (int chX = from.x; chX <= to.x; chX++) {
                    for (int chZ = from.z; chZ <= to.z; chZ++) {
                        this.availableNetworks
                                .computeIfAbsent(new ChunkPos(chX, chZ),
                                        pos -> Lists.newArrayList())
                                .add(network);
                    }
                }
            }
        }
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag compound) {
        ListTag networks = new ListTag();
        for (StorageNetwork network : this.rawNetworks.values()) {
            CompoundTag tag = new CompoundTag();
            network.writeToNBT(tag);
            networks.add(tag);
        }
        compound.put("networks", networks);
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag compound) {
        this.rawNetworks.clear();

        ListTag networks = compound.getList("networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networks.size(); i++) {
            CompoundTag tag = networks.getCompound(i);
            StorageNetwork net = new StorageNetwork();
            net.readFromNBT(tag);
            if (net.getCores().isEmpty()) {
                continue;
            }
            StorageNetwork.CoreArea master = net.getMaster();
            if (master == null) {
                master = MiscUtils.getRandomEntry(net.getCores(), rand);
            }
            if (master != null) {
                this.rawNetworks.put(master.getPos(), net);
            }
        }

        this.rebuildAccessContext();
    }
}
