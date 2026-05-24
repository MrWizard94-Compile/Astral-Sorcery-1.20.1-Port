package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityFakedState;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
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
        if (getLevel() == null || getLevel().isClientSide()) return;

        if (getTicksExisted() % 200 == 0) {
            if (treeBeaconPos.equals(BlockPos.ZERO)) {
                removeSelf();
            } else {
                BlockEntityTreeBeacon beacon = MiscUtils.getTileAt(
                        getLevel(), treeBeaconPos, BlockEntityTreeBeacon.class, false);
                if (beacon == null) {
                    removeSelf();
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
        this.treeBeaconPos = NBTHelper.readFromSubTag(compound, "treeBeaconPos",
                NBTHelper::readBlockPosFromNBT);
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        NBTHelper.setAsSubTag(compound, "treeBeaconPos",
                tag -> NBTHelper.writeBlockPosToNBT(this.treeBeaconPos, tag));
    }
}
