package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityFakedState;
import hellfirepvp.astralsorcery.common.util.tile.TileUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Block entity for a tree beacon component block — a faked-state block placed
 * by the tree beacon to fill in a sapling/leaf space. Periodically validates
 * that its parent tree beacon still exists, and removes itself if it does not.
 *
 * <p>1.16 → 1.20: getWorld() → getLevel(), getPos() → getBlockPos(),
 * CompoundNBT → CompoundTag, BlockPos.ZERO still valid.</p>
 */
public class BlockEntityTreeBeaconComponent extends BlockEntityFakedState {

    private BlockPos treeBeaconPos = BlockPos.ZERO;

    public BlockEntityTreeBeaconComponent(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.TREE_BEACON_COMPONENT.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        net.minecraft.world.level.Level level = getLevel();
        if (level == null || level.isClientSide()) return;

        if (getTicksExisted() % 200 == 0) {
            if (treeBeaconPos.equals(BlockPos.ZERO)) {
                removeSelf();
            } else {
                // Only self-destruct when the beacon's chunk IS loaded and the beacon
                // genuinely doesn't exist. If the chunk is unloaded, getTileAt returns
                // null regardless — don't tear down the structure in that case.
                boolean beaconChunkLoaded = level.getChunkSource()
                        .hasChunk(treeBeaconPos.getX() >> 4, treeBeaconPos.getZ() >> 4);
                if (beaconChunkLoaded) {
                    BlockEntityTreeBeacon beacon = TileUtils.getTileAt(
                            level, treeBeaconPos, BlockEntityTreeBeacon.class, false);
                    if (beacon == null) {
                        removeSelf();
                    }
                }
            }
        }
    }

    @Nonnull
    public BlockPos getTreeBeaconPos() {
        return treeBeaconPos;
    }

    public void setTreeBeaconPos(@Nonnull BlockPos pos) {
        this.treeBeaconPos = pos;
        markForUpdate();
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        BlockPos loaded = NBTHelper.readFromSubTag(compound, "treeBeaconPos",
                NBTHelper::readBlockPosFromNBT);
        this.treeBeaconPos = loaded != null ? loaded : BlockPos.ZERO;
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        NBTHelper.setAsSubTag(compound, "treeBeaconPos",
                tag -> NBTHelper.writeBlockPosToNBT(this.treeBeaconPos, tag));
    }
}
