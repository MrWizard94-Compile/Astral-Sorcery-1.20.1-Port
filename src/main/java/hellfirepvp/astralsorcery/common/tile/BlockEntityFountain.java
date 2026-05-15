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
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Fountain.
 * Consumes liquid starlight for various area effects determined
 * by the inserted fountain prime item.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * CapabilityFluidHandler -> ForgeCapabilities.FLUID_HANDLER,
 * LazyOptional pattern unchanged</p>
 */
public class BlockEntityFountain extends BlockEntityTick {

    private static final int CAPACITY_MB = 16000;

    @Nonnull
    private final PrecisionSingleFluidTank tank;
    private final LazyOptional<IFluidHandler> fluidCap;

    @Nullable
    private ItemStack fountainPrime = null;

    private boolean structureValid = false;
    private int effectTick = 0;

    public BlockEntityFountain(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.FOUNTAIN.get(), pos, state);
        this.tank = new PrecisionSingleFluidTank(CAPACITY_MB);
        this.fluidCap = LazyOptional.of(() -> new TankWrapper(tank));
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side fountain visual effects (liquid starlight stream)
            return;
        }

        // TODO: Server-side fountain logic:
        // 1. Validate multiblock structure -> structureValid
        // 2. If structureValid && fountainPrime is present && tank has fluid:
        //    - Increment effectTick
        //    - Apply effect based on fountainPrime type
        //    - Consume liquid starlight from tank
        // 3. If not valid, reset effectTick
    }

    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    @Nullable
    public ItemStack getFountainPrime() {
        return fountainPrime;
    }

    public void setFountainPrime(@Nullable ItemStack prime) {
        this.fountainPrime = prime;
        markForUpdate();
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public void setStructureValid(boolean valid) {
        this.structureValid = valid;
    }

    public int getEffectTick() {
        return effectTick;
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
        if (compound.contains("fountainPrime")) {
            this.fountainPrime = ItemStack.of(compound.getCompound("fountainPrime"));
        } else {
            this.fountainPrime = null;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.put("tank", tank.writeNBT());
        if (fountainPrime != null && !fountainPrime.isEmpty()) {
            CompoundTag primeTag = new CompoundTag();
            fountainPrime.save(primeTag);
            compound.put("fountainPrime", primeTag);
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.structureValid = compound.getBoolean("structureValid");
        this.effectTick = compound.getInt("effectTick");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putBoolean("structureValid", structureValid);
        compound.putInt("effectTick", effectTick);
    }

    /**
     * Wraps the IFluidTank as a full IFluidHandler for capability exposure.
     * Allows both fill and drain for the fountain basin.
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
