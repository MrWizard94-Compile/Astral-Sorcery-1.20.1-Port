package hellfirepvp.astralsorcery.common.util.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * Simple single-fluid tank with integer precision.
 * Suitable for most block entity fluid storage.
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag,
 * ResourceLocation -> net.minecraft.resources.ResourceLocation,
 * fluid.getRegistryName() -> ForgeRegistries.FLUIDS.getKey(fluid),
 * Fluid/Fluids -> net.minecraft.world.level.material.*</p>
 */
public class SimpleSingleFluidTank implements IFluidTank {

    private int amount = 0;
    @Nonnull
    private Fluid fluid = Fluids.EMPTY;
    private int maxCapacity;
    @Nonnull
    private Runnable onUpdate = () -> {};

    private boolean allowInput = true;
    private boolean allowOutput = true;

    private SimpleSingleFluidTank() {}

    public SimpleSingleFluidTank(int maxCapacity) {
        this.maxCapacity = Math.max(0, maxCapacity);
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
    public int getMaxAddable(int toAdd) {
        return Math.min(toAdd, maxCapacity - amount);
    }

    public int getMaxDrainable(int toDrain) {
        return Math.min(toDrain, amount);
    }

    /** @return leftover amount that could not be added */
    public int addAmount(int amount) {
        if (this.fluid == Fluids.EMPTY) {
            return amount;
        }
        int addable = getMaxAddable(amount);
        this.amount += addable;
        if (Math.abs(addable) > 0) {
            this.onUpdate.run();
        }
        return amount - addable;
    }

    /** @return the drained FluidStack */
    @Nonnull
    public FluidStack drain(int amount) {
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

    @Nonnull
    @Override
    public FluidStack getFluid() {
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    @Override
    public int getFluidAmount() {
        return amount;
    }

    @Override
    public int getCapacity() {
        return this.maxCapacity;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
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
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!canFillFluidType(resource)) {
            return 0;
        }
        int maxAdded = resource.getAmount();
        int addable = getMaxAddable(maxAdded);
        if (action.execute()) {
            if (addable > 0 && this.fluid == Fluids.EMPTY) {
                setFluid(resource.getFluid());
            }
            addable -= addAmount(addable);
        }
        return addable;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!canDrainFluidType(resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (!canDrain()) {
            return FluidStack.EMPTY;
        }
        int maxDrainable = getMaxDrainable(maxDrain);
        if (action.execute()) {
            return drain(maxDrainable);
        }
        return new FluidStack(this.fluid, maxDrainable);
    }

    @Nonnull
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("amt", this.amount);
        tag.putInt("capacity", this.maxCapacity);
        tag.putBoolean("aIn", this.allowInput);
        tag.putBoolean("aOut", this.allowOutput);
        ResourceLocation fluidKey = ForgeRegistries.FLUIDS.getKey(this.fluid);
        tag.putString("fluid", fluidKey != null ? fluidKey.toString() : "minecraft:empty");
        return tag;
    }

    public void readNBT(@Nonnull CompoundTag tag) {
        this.amount = tag.getInt("amt");
        this.maxCapacity = tag.getInt("capacity");
        this.allowInput = tag.getBoolean("aIn");
        this.allowOutput = tag.getBoolean("aOut");
        ResourceLocation fluidKey = ResourceLocation.tryParse(tag.getString("fluid"));
        Fluid readFluid = fluidKey != null ? ForgeRegistries.FLUIDS.getValue(fluidKey) : null;
        this.fluid = readFluid != null ? readFluid : Fluids.EMPTY;
    }

    @Nonnull
    public static SimpleSingleFluidTank deserialize(@Nonnull CompoundTag tag) {
        SimpleSingleFluidTank tank = new SimpleSingleFluidTank();
        tank.readNBT(tag);
        return tank;
    }
}
