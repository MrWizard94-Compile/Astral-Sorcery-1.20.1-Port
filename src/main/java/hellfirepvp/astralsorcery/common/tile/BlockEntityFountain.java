package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.block.tile.fountain.BlockFountainPrime;
import hellfirepvp.astralsorcery.common.crafting.nojson.FountainEffectRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffect;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.StructuresAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
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
 * Consumes liquid starlight for area effects determined by the BlockFountainPrime
 * block placed directly below the fountain. The effect progresses through
 * STARTUP â†’ PREPARATION â†’ RUNNING phases.
 */
public class BlockEntityFountain extends BlockEntityTick {

    private static final int CAPACITY_MB = 16000;

    @Nonnull
    private final PrecisionSingleFluidTank tank;
    private final LazyOptional<IFluidHandler> fluidCap;

    private boolean structureValid = false;

    @Nullable
    private FountainEffect<?> currentEffect = null;
    @Nullable
    private Object currentContext = null;
    @Nonnull
    private FountainEffect.OperationSegment currentSegment = FountainEffect.OperationSegment.INACTIVE;
    private int operationTick = 0;

    public BlockEntityFountain(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.FOUNTAIN.get(), pos, state);
        this.tank = new PrecisionSingleFluidTank(CAPACITY_MB);
        this.fluidCap = LazyOptional.of(() -> new TankWrapper(tank));
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) return;

        Level level = getLevel();
        if (level == null) return;

        if (getTicksExisted() % 40 == 0) {
            boolean wasValid = structureValid;
            structureValid = StructuresAS.getFountain().matches(level, worldPosition);
            if (wasValid != structureValid) {
                markForUpdate();
            }
        }

        if (!structureValid) {
            clearEffect();
            return;
        }

        // Detect which prime block is below the fountain
        BlockState primeState = level.getBlockState(worldPosition.below());
        FountainEffect<?> detectedEffect = null;
        if (primeState.getBlock() instanceof BlockFountainPrime prime) {
            detectedEffect = FountainEffectRegistry.getEffect(prime.getEffectId());
        }

        // If the prime changed, reset effect state
        if (detectedEffect != currentEffect) {
            if (currentEffect != null && currentContext != null) {
                uncheckedOnReplace(currentEffect, currentContext, detectedEffect);
            }
            currentEffect = detectedEffect;
            currentContext = detectedEffect != null ? detectedEffect.createContext(this) : null;
            currentSegment = detectedEffect != null
                    ? FountainEffect.OperationSegment.STARTUP
                    : FountainEffect.OperationSegment.INACTIVE;
            operationTick = 0;
            markForUpdate();
        }

        if (currentEffect == null || currentContext == null) return;

        FluidStack stored = tank.getFluid();
        if (stored.isEmpty()) {
            clearEffect();
            return;
        }
        tank.drain(1, IFluidHandler.FluidAction.EXECUTE);

        // Advance segment when its duration is reached
        if (currentSegment.duration > 0 && operationTick >= currentSegment.duration) {
            FountainEffect.OperationSegment next = nextSegment(currentSegment);
            uncheckedTransition(currentEffect, currentContext, currentSegment, next);
            currentSegment = next;
            operationTick = 0;
        }

        // Tick counter only advances during timed phases
        if (currentSegment == FountainEffect.OperationSegment.STARTUP
                || currentSegment == FountainEffect.OperationSegment.PREPARATION) {
            operationTick++;
        }

        uncheckedTick(currentEffect, currentContext);
        markForUpdate();
    }

    private void clearEffect() {
        if (currentEffect != null && currentContext != null) {
            uncheckedOnReplace(currentEffect, currentContext, null);
        }
        currentEffect = null;
        currentContext = null;
        currentSegment = FountainEffect.OperationSegment.INACTIVE;
        operationTick = 0;
    }

    @SuppressWarnings("unchecked")
    private <C> void uncheckedTick(FountainEffect<C> effect, Object context) {
        effect.tick(this, (C) context, operationTick, currentSegment);
    }

    @SuppressWarnings("unchecked")
    private <C> void uncheckedTransition(FountainEffect<C> effect, Object context,
                                          FountainEffect.OperationSegment prev,
                                          FountainEffect.OperationSegment next) {
        effect.transition(this, (C) context, prev, next);
    }

    @SuppressWarnings("unchecked")
    private <C> void uncheckedOnReplace(FountainEffect<C> effect, Object context,
                                         @Nullable FountainEffect<?> newEffect) {
        effect.onReplace(this, (C) context, newEffect);
    }

    private FountainEffect.OperationSegment nextSegment(FountainEffect.OperationSegment current) {
        return switch (current) {
            case INACTIVE, STARTUP -> FountainEffect.OperationSegment.PREPARATION;
            case PREPARATION, RUNNING -> FountainEffect.OperationSegment.RUNNING;
        };
    }


    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    @Nullable
    public FountainEffect<?> getCurrentEffect() {
        return currentEffect;
    }

    @Nonnull
    public FountainEffect.OperationSegment getCurrentSegment() {
        return currentSegment;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
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
        this.structureValid = compound.getBoolean("structureValid");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.put("tank", tank.writeNBT());
        compound.putBoolean("structureValid", structureValid);
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.structureValid = compound.getBoolean("structureValid");
        this.operationTick = compound.getInt("operationTick");
        String segName = compound.getString("segment");
        try {
            this.currentSegment = FountainEffect.OperationSegment.valueOf(segName);
        } catch (IllegalArgumentException e) {
            this.currentSegment = FountainEffect.OperationSegment.INACTIVE;
        }
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putBoolean("structureValid", structureValid);
        compound.putInt("operationTick", operationTick);
        compound.putString("segment", currentSegment.name());
    }

    private static class TankWrapper implements IFluidHandler {

        private final PrecisionSingleFluidTank tank;

        TankWrapper(@Nonnull PrecisionSingleFluidTank tank) {
            this.tank = tank;
        }

        @Override
        public int getTanks() { return 1; }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tankIndex) { return tank.getFluid(); }

        @Override
        public int getTankCapacity(int tankIndex) { return tank.getCapacity(); }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return tank.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack stored = tank.getFluid();
            if (stored.isEmpty() || !stored.isFluidEqual(resource)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return tank.drain(maxDrain, action);
        }
    }
}
