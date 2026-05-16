package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the Chalice.
 * Stores a large amount of liquid starlight and auto-distributes
 * it between linked chalices to maintain balanced fluid levels.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * CapabilityFluidHandler -> ForgeCapabilities.FLUID_HANDLER,
 * NBTUtil.readBlockPos -> NbtUtils.readBlockPos,
 * NBTUtil.writeBlockPos -> NbtUtils.writeBlockPos,
 * INBT -> Tag, ListNBT -> ListTag</p>
 */
public class BlockEntityChalice extends BlockEntityTick {

    private static final int CAPACITY_MB = 24000;

    @Nonnull
    private final PrecisionSingleFluidTank tank;
    private final LazyOptional<IFluidHandler> fluidCap;

    private int ticksExisted = 0;

    @Nonnull
    private final List<BlockPos> linkedChalices = new ArrayList<>();

    public BlockEntityChalice(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.CHALICE.get(), pos, state);
        this.tank = new PrecisionSingleFluidTank(CAPACITY_MB);
        this.fluidCap = LazyOptional.of(() -> new TankWrapper(tank));
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // TODO: Client-side fluid level visual effects
            return;
        }

        // TODO: Auto-balance fluid between linked chalices:
        // 1. Iterate linkedChalices positions
        // 2. For each valid loaded BlockEntityChalice:
        //    - Compare fluid levels
        //    - Transfer from higher to lower to equalize
        // 3. Remove invalid/unloaded positions from linkedChalices
    }

    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    /**
     * Get the amount of fluid stored in the chalice in millibuckets.
     * Used by the renderer to determine fluid level.
     */
    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    @Nonnull
    public List<BlockPos> getLinkedChalices() {
        return linkedChalices;
    }

    public void addLinkedChalice(@Nonnull BlockPos pos) {
        if (!linkedChalices.contains(pos)) {
            linkedChalices.add(pos);
            markForUpdate();
        }
    }

    public void removeLinkedChalice(@Nonnull BlockPos pos) {
        if (linkedChalices.remove(pos)) {
            markForUpdate();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.tank.readNBT(compound.getCompound("tank"));

        this.linkedChalices.clear();
        if (compound.contains("linkedChalices")) {
            ListTag list = compound.getList("linkedChalices", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                linkedChalices.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.put("tank", tank.writeNBT());

        ListTag list = new ListTag();
        for (BlockPos pos : linkedChalices) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        compound.put("linkedChalices", list);
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
    }

    /**
     * Wraps the IFluidTank as a full IFluidHandler for capability exposure.
     * Allows both fill and drain from all sides.
     */
    private static class TankWrapper implements IFluidHandler {

        private final PrecisionSingleFluidTank tank;

        TankWrapper(@Nonnull PrecisionSingleFluidTank tank) {
            this.tank = tank;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tankIndex, @Nonnull FluidStack stack) {
            return tank.isFluidValid(stack);
        }

        @Override
        public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
            return tank.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
            FluidStack stored = tank.getFluid();
            if (stored.isEmpty() || !stored.isFluidEqual(resource)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, @Nonnull FluidAction action) {
            return tank.drain(maxDrain, action);
        }
    }
}
