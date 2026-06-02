package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.common.data.world.base.GlobalWorldData;
import hellfirepvp.astralsorcery.common.data.world.base.WorldCacheDomain;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks rock crystal cluster positions per world for dungeon/shrine loot generation.
 *
 * <p>When a rock crystal cluster is placed (by worldgen), its position is registered here
 * so that shrine generation can seed crystals at valid positions.</p>
 */
public class RockCrystalBuffer extends GlobalWorldData {

    private static final String TAG_POSITIONS = "positions";

    private final Set<BlockPos> crystalPositions = new HashSet<>();

    public RockCrystalBuffer(@Nonnull WorldCacheDomain.SaveKey<?> key) {
        super(key);
    }

    public void addPosition(@Nonnull BlockPos pos) {
        if (crystalPositions.add(pos)) {
            markDirtyFlag();
        }
    }

    public void removePosition(@Nonnull BlockPos pos) {
        if (crystalPositions.remove(pos)) {
            markDirtyFlag();
        }
    }

    @Nonnull
    public Set<BlockPos> getPositions() {
        return crystalPositions;
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag tag) {
        crystalPositions.clear();
        ListTag list = tag.getList(TAG_POSITIONS, Tag.TAG_LONG);
        for (Tag t : list) {
            crystalPositions.add(BlockPos.of(((LongTag) t).getAsLong()));
        }
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos pos : crystalPositions) {
            list.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put(TAG_POSITIONS, list);
    }
}
