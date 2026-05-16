package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the Prism.
 * Part of the starlight transmission network. Similar to a Lens
 * but supports multiple output targets (up to 4) and an inserted
 * color lens item for filtering.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * NBTUtil -> NbtUtils</p>
 */
public class BlockEntityPrism extends BlockEntityTick {

    private static final int MAX_LINKS = 4;

    @Nonnull
    private final List<BlockPos> linkedTargets = new ArrayList<>();

    @Nullable
    private ItemStack insertedLens = null;

    private int ticksExisted = 0;
    private double transmissionEfficiency = 0.85;

    public BlockEntityPrism(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.PRISM.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // TODO: Client-side beam split rendering particles
            return;
        }

        // TODO: Server-side transmission logic:
        // 1. Receive starlight from source
        // 2. Apply transmissionEfficiency loss
        // 3. Apply color filter from insertedLens if present
        // 4. Split and forward to all linkedTargets (up to 4)
        //    - Each target receives starlight / linkedTargets.size()
    }

    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean isTransmitting() {
        return !linkedTargets.isEmpty();
    }

    @Nonnull
    public List<BlockPos> getLinkedTargets() {
        return linkedTargets;
    }

    /**
     * Attempts to add a linked target. Returns false if the max link count is reached.
     */
    public boolean addLinkedTarget(@Nonnull BlockPos target) {
        if (linkedTargets.size() >= MAX_LINKS) {
            return false;
        }
        if (!linkedTargets.contains(target)) {
            linkedTargets.add(target);
            markForUpdate();
        }
        return true;
    }

    public void removeLinkedTarget(@Nonnull BlockPos target) {
        if (linkedTargets.remove(target)) {
            markForUpdate();
        }
    }

    public void clearLinkedTargets() {
        linkedTargets.clear();
        markForUpdate();
    }

    @Nullable
    public ItemStack getInsertedLens() {
        return insertedLens;
    }

    public void setInsertedLens(@Nullable ItemStack lens) {
        this.insertedLens = lens;
        markForUpdate();
    }

    public double getTransmissionEfficiency() {
        return transmissionEfficiency;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);

        this.linkedTargets.clear();
        if (compound.contains("linkedTargets")) {
            ListTag list = compound.getList("linkedTargets", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                linkedTargets.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }

        if (compound.contains("insertedLens")) {
            this.insertedLens = ItemStack.of(compound.getCompound("insertedLens"));
        } else {
            this.insertedLens = null;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);

        ListTag list = new ListTag();
        for (BlockPos pos : linkedTargets) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        compound.put("linkedTargets", list);

        if (insertedLens != null && !insertedLens.isEmpty()) {
            CompoundTag lensTag = new CompoundTag();
            insertedLens.save(lensTag);
            compound.put("insertedLens", lensTag);
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.transmissionEfficiency = compound.contains("transmissionEfficiency")
                ? compound.getDouble("transmissionEfficiency") : 0.85;
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putDouble("transmissionEfficiency", transmissionEfficiency);
    }
}
