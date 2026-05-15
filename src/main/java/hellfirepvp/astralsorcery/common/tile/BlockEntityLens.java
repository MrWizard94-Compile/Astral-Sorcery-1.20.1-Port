package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the Lens.
 * Part of the starlight transmission network. Redirects a starlight
 * beam from one source to one target. Supports a color overlay
 * from tinted lens items.
 *
 * <p>Maximum 1 linked target. Lenses do not split beams.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * ResourceLocation for color overlay keying,
 * NBTUtil -> NbtUtils</p>
 */
public class BlockEntityLens extends BlockEntityTick {

    private static final int MAX_LINKS = 1;

    @Nullable
    private ResourceLocation colorOverlay = null;

    @Nonnull
    private final List<BlockPos> linkedTargets = new ArrayList<>();

    private double transmissionEfficiency = 0.95;

    public BlockEntityLens(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.LENS.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side beam rendering particles
            return;
        }

        // TODO: Server-side transmission logic:
        // 1. Receive starlight from source
        // 2. Apply transmissionEfficiency loss
        // 3. Apply color overlay filter if present
        // 4. Forward to linkedTargets (max 1)
    }

    @Nullable
    public ResourceLocation getColorOverlay() {
        return colorOverlay;
    }

    public void setColorOverlay(@Nullable ResourceLocation overlay) {
        this.colorOverlay = overlay;
        markForUpdate();
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

    public double getTransmissionEfficiency() {
        return transmissionEfficiency;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        if (compound.contains("colorOverlay")) {
            this.colorOverlay = new ResourceLocation(compound.getString("colorOverlay"));
        } else {
            this.colorOverlay = null;
        }

        this.linkedTargets.clear();
        if (compound.contains("linkedTargets")) {
            ListTag list = compound.getList("linkedTargets", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                linkedTargets.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (colorOverlay != null) {
            compound.putString("colorOverlay", colorOverlay.toString());
        }

        ListTag list = new ListTag();
        for (BlockPos pos : linkedTargets) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        compound.put("linkedTargets", list);
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.transmissionEfficiency = compound.contains("transmissionEfficiency")
                ? compound.getDouble("transmissionEfficiency") : 0.95;
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putDouble("transmissionEfficiency", transmissionEfficiency);
    }
}
