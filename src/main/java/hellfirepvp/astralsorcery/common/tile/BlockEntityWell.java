package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Lightwell.
 * Slowly converts rock crystals into liquid starlight via drip mechanics.
 * Uses PrecisionSingleFluidTank for sub-mB precision.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * CapabilityFluidHandler -> ForgeCapabilities.FLUID_HANDLER,
 * LazyOptional pattern unchanged</p>
 */
public class BlockEntityWell extends BlockEntityTick {

    private static final int CAPACITY_MB = 2000;

    @Nonnull
    private ItemStack catalystStack = ItemStack.EMPTY;
    @Nonnull
    private final PrecisionSingleFluidTank tank;
    private final LazyOptional<IFluidHandler> fluidCap;

    private double productionProgress = 0.0;

    public BlockEntityWell(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.WELL.get(), pos, state);
        this.tank = new PrecisionSingleFluidTank(CAPACITY_MB);
        this.fluidCap = LazyOptional.of(() -> new TankWrapper(tank));
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side drip particle effects
            return;
        }

        if (catalystStack.isEmpty()) {
            return;
        }

        // TODO: Look up WellLiquefaction recipe for catalystStack
        // - Calculate drip rate based on starlight collection, catalyst type, time of day
        // - Add fluid to tank via precisionAdd
        // - Degrade catalyst over time
    }

    @Nonnull
    public ItemStack getCatalystStack() {
        return catalystStack;
    }

    public void setCatalystStack(@Nonnull ItemStack stack) {
        this.catalystStack = stack;
        markForUpdate();
    }

    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    public double getProductionProgress() {
        return productionProgress;
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
        this.catalystStack = compound.contains("catalyst")
                ? ItemStack.of(compound.getCompound("catalyst"))
                : ItemStack.EMPTY;
        this.tank.readNBT(compound.getCompound("tank"));
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (!catalystStack.isEmpty()) {
            CompoundTag catalystTag = new CompoundTag();
            catalystStack.save(catalystTag);
            compound.put("catalyst", catalystTag);
        }
        compound.put("tank", tank.writeNBT());
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.productionProgress = compound.getDouble("productionProgress");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putDouble("productionProgress", productionProgress);
    }

    /**
     * Wraps the IFluidTank as a full IFluidHandler for capability exposure.
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
            return false; // Well only outputs; input is via catalyst item
        }

        @Override
        public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
            return 0; // No external filling
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
            FluidStack stored = tank.getFluid();
            if (stored.isEmpty()) {
                return FluidStack.EMPTY;
            }
            int drained = Math.min(stored.getAmount(), maxDrain);
            FluidStack result = new FluidStack(stored, drained);
            if (action.execute()) {
                tank.drain(drained, FluidAction.EXECUTE);
            }
            return result;
        }
    }
}
