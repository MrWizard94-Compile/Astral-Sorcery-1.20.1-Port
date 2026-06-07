package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Base block entity with automatic client sync via custom/net/save NBT split.
 * In 1.16 this was TileEntitySynchronized extending TileEntity.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity,
 * TileEntityType -> BlockEntityType,
 * CompoundNBT -> CompoundTag,
 * AxisAlignedBB -> AABB,
 * getPos -> getBlockPos,
 * getWorld -> getLevel,
 * read(BlockState, CompoundNBT) -> load(CompoundTag),
 * write(CompoundNBT) -> saveAdditional(CompoundTag),
 * SUpdateTileEntityPacket -> ClientboundBlockEntityDataPacket,
 * markDirty -> setChanged,
 * notifyBlockUpdate -> sendBlockUpdated,
 * isRemote -> isClientSide,
 * getDefaultState -> defaultBlockState,
 * setBlockState -> setBlock</p>
 */
public abstract class BlockEntitySynchronized extends BlockEntity implements ILocatable {

    protected static final Random rand = new Random();
    protected static final AABB BOX = new AABB(0, 0, 0, 1, 1, 1);

    protected BlockEntitySynchronized(@Nonnull BlockEntityType<?> type, @Nonnull BlockPos pos,
                                      @Nonnull BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Nonnull
    public BlockPos getLocationPos() {
        return this.getBlockPos();
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        readCustomNBT(nbt);
        readSaveNBT(nbt);
    }

    /** Both Network and Chunk-saving */
    public void readCustomNBT(@Nonnull CompoundTag compound) {}

    /** Only Network-read */
    public void readNetNBT(@Nonnull CompoundTag compound) {}

    /** Only Chunk-read */
    public void readSaveNBT(@Nonnull CompoundTag compound) {}

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        writeCustomNBT(compound);
        writeSaveNBT(compound);
    }

    /** Both Network and Chunk-saving */
    public void writeCustomNBT(@Nonnull CompoundTag compound) {}

    /** Only Network-write */
    public void writeNetNBT(@Nonnull CompoundTag compound) {}

    /** Only Chunk-write */
    public void writeSaveNBT(@Nonnull CompoundTag compound) {}

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, be -> {
            CompoundTag compound = this.getUpdateTag();
            this.writeNetNBT(compound);
            return compound;
        });
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        CompoundTag compound = super.getUpdateTag();
        writeCustomNBT(compound);
        return compound;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        // Do NOT call super — vanilla calls load() which also runs readSaveNBT()
        // with an incomplete update tag, zeroing disk-only fields on the client.
        readCustomNBT(tag);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net,
                             ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            readCustomNBT(tag);
            readNetNBT(tag);
        }
        this.onDataReceived();
    }

    @OnlyIn(Dist.CLIENT)
    protected void onDataReceived() {}

    public void markForUpdate() {
        net.minecraft.world.level.Level level = getLevel();
        if (level != null) {
            BlockState thisState = this.getBlockState();
            level.sendBlockUpdated(getBlockPos(), thisState, thisState, 3);
        }
        setChanged();
    }

    @Nullable
    public ItemEntity dropItemOnTop(@Nonnull ItemStack stack) {
        net.minecraft.world.level.Level level = getLevel();
        if (level == null) return null;
        return ItemUtils.dropItem(level,
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + 1.5,
                getBlockPos().getZ() + 0.5,
                stack);
    }

    public boolean removeSelf() {
        net.minecraft.world.level.Level level = this.getLevel();
        if (level == null || level.isClientSide()) {
            return false;
        }
        return level.setBlock(this.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
    }
}
