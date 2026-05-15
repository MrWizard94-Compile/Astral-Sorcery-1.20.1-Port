package hellfirepvp.astralsorcery.common.util.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * Single-fluid tank with sub-integer (double) precision for the stored amount.
 * Useful for partial-mB drip mechanics like the lightwell.
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag,
 * ResourceLocation -> net.minecraft.resources.ResourceLocation,
 * MathHelper -> Mth,
 * fluid.getRegistryName() -> ForgeRegistries.FLUIDS.getKey(fluid),
 * Fluid/Fluids -> net.minecraft.world.level.material.*</p>
 */
public class PrecisionSingleFluidTank implements IFluidTank {

    private double amount = 0;
    private int maxCapacity;
    @Nonnull
    private Fluid fluid = Fluids.EMPTY;
    @Nonnull
    private Runnable onUpdate = () -> {};

    private boolean allowInput = true;
    private boolean allowOutput = true;

    private PrecisionSingleFluidTank() {}

    public PrecisionSingleFluidTank(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void addUpdateFunction(@Nonnull Runnable onUpdate) {
        Runnable prevRunnable = this.onUpdate;
        this.onUpdate = () -> {
            prevRunnable.run();
            onUpdate.run();
        };
    }

    public void setAllowInput(boolean allowInput) {
        this.allowInput = allowInput;
    }

    public void setAllowOutput(boolean allowOutput) {
        this.allowOutput = allowOutput;
    }

    /** Returns min(toAdd, what can be added at most). */
    public double getMaxAddable(double toAdd) {
        return Math.min(toAdd, maxCapacity - amount);
    }

    public int getMaxDrainable(double toDrain) {
        return (int) Math.floor(Math.min(toDrain, amount));
    }

    /** @return leftover amount that could not be added */
    public double addAmount(double amount) {
        if (this.fluid == Fluids.EMPTY) {
            return amount;
        }
        double addable = getMaxAddable(amount);
        this.amount += addable;
        if (Math.abs(addable) > 0) {
            this.onUpdate.run();
        }
        return amount - addable;
    }

    /** @return the drained FluidStack */
    @Nonnull
    public FluidStack drain(double amount) {
        if (this.fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        int drainable = getMaxDrainable(amount);
        this.amount -= drainable;
        Fluid drainedFluid = this.fluid;
        if (this.amount <= 0) {
            setFluid(Fluids.EMPTY);
        }
        if (Math.abs(drainable) > 0) {
            this.onUpdate.run();
        }
        return new FluidStack(drainedFluid, drainable);
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, getFluidAmount());
    }

    public void setFluid(@Nonnull Fluid fluid) {
        boolean update = false;
        if (fluid != this.fluid) {
            this.amount = 0;
            update = true;
        }
        this.fluid = fluid;
        if (update) {
            this.onUpdate.run();
        }
    }

    @Override
    public int getFluidAmount() {
        return Mth.floor(amount);
    }

    @Override
    public int getCapacity() {
        return this.maxCapacity;
    }

    @Override
    public boolean isFluidValid(@Nonnull FluidStack stack) {
        return true;
    }

    public boolean canFill() {
        return this.allowInput && this.amount < this.maxCapacity;
    }

    public boolean canDrain() {
        return this.allowOutput && this.amount > 0 && this.fluid != Fluids.EMPTY;
    }

    public boolean canFillFluidType(@Nonnull FluidStack fluidStack) {
        return canFill() && (this.fluid == Fluids.EMPTY
                || fluidStack.getFluid().equals(this.fluid));
    }

    public boolean canDrainFluidType(@Nonnull FluidStack fluidStack) {
        return canDrain() && (this.fluid != Fluids.EMPTY
                && fluidStack.getFluid().equals(this.fluid));
    }

    public float getPercentageFilled() {
        return ((float) amount) / ((float) maxCapacity);
    }

    @Override
    public int fill(@Nonnull FluidStack resource, @Nonnull IFluidHandler.FluidAction action) {
        if (!canFillFluidType(resource)) {
            return 0;
        }
        int maxAdded = resource.getAmount();
        int addable = Mth.floor(getMaxAddable(maxAdded));
        if (action.execute()) {
            if (addable > 0 && this.fluid == Fluids.EMPTY) {
                setFluid(resource.getFluid());
            }
            addable -= (int) addAmount(addable);
        }
        return addable;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, @Nonnull IFluidHandler.FluidAction action) {
        if (!canDrain()) {
            return FluidStack.EMPTY;
        }
        int maxDrainable = getMaxDrainable(maxDrain);
        if (action.execute()) {
            return drain((double) maxDrainable);
        }
        return new FluidStack(this.fluid, maxDrainable);
    }

    @Nonnull
    @Override
    public FluidStack drain(@Nonnull FluidStack resource, @Nonnull IFluidHandler.FluidAction action) {
        if (!canDrainFluidType(resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Nonnull
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("amt", this.amount);
        tag.putInt("capacity", this.maxCapacity);
        tag.putBoolean("aIn", this.allowInput);
        tag.putBoolean("aOut", this.allowOutput);
        ResourceLocation fluidKey = ForgeRegistries.FLUIDS.getKey(this.fluid);
        tag.putString("fluid", fluidKey != null ? fluidKey.toString() : "minecraft:empty");
        return tag;
    }

    public void readNBT(@Nonnull CompoundTag tag) {
        this.amount = tag.getDouble("amt");
        this.maxCapacity = tag.getInt("capacity");
        this.allowInput = tag.getBoolean("aIn");
        this.allowOutput = tag.getBoolean("aOut");
        Fluid readFluid = ForgeRegistries.FLUIDS.getValue(
                new ResourceLocation(tag.getString("fluid")));
        this.fluid = readFluid != null ? readFluid : Fluids.EMPTY;
    }

    @Nonnull
    public static PrecisionSingleFluidTank deserialize(@Nonnull CompoundTag tag) {
        PrecisionSingleFluidTank tank = new PrecisionSingleFluidTank();
        tank.readNBT(tag);
        return tank;
    }
}
