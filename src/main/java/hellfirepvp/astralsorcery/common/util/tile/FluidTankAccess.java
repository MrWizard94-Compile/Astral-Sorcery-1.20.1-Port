package hellfirepvp.astralsorcery.common.util.tile;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Sided capability wrapper for multiple {@link IFluidTank} instances.
 * Each tank is assigned an ID and a set of accessible sides.
 *
 * <p>1.16 -> 1.20 changes:
 * CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY -> ForgeCapabilities.FLUID_HANDLER,
 * Direction unchanged (net.minecraft.core.Direction)</p>
 */
public class FluidTankAccess {

    private final Set<AccessibleTank> tanks = new HashSet<>();

    public void putTank(int tankId, @Nonnull IFluidTank tank, @Nonnull Direction... sides) {
        this.tanks.add(new AccessibleTank(tankId, tank, sides));
    }

    public void putTank(int tankId, @Nonnull IFluidTank tank,
                        @Nonnull Predicate<Direction> accessibleSides) {
        this.tanks.add(new AccessibleTank(tankId, tank, accessibleSides));
    }

    private boolean hasTanksForSide(@Nullable Direction dir) {
        return dir == null || MiscUtils.contains(this.tanks, tank -> tank.isAccessible(dir));
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return ForgeCapabilities.FLUID_HANDLER == capability && hasTanksForSide(facing);
    }

    @Nonnull
    public LazyOptional<IFluidHandler> getCapability(@Nullable Direction facing) {
        Set<AccessibleTank> available = facing == null
                ? this.tanks
                : this.tanks.stream()
                        .filter(t -> t.isAccessible(facing))
                        .collect(Collectors.toSet());
        return available.isEmpty()
                ? LazyOptional.empty()
                : LazyOptional.of(() -> new SidedAccess(available));
    }

    private static class SidedAccess implements IFluidHandler {

        private final Set<AccessibleTank> tanks;

        private SidedAccess(@Nonnull Set<AccessibleTank> accessibleTanks) {
            this.tanks = accessibleTanks;
        }

        @Nonnull
        private Optional<AccessibleTank> getTank(int id) {
            return this.tanks.stream().filter(tank -> tank.getId() == id).findFirst();
        }

        @Override
        public int getTanks() {
            return this.tanks.size();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return this.getTank(tank)
                    .map(t -> t.getTank().getFluid())
                    .orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            return this.getTank(tank)
                    .map(t -> t.getTank().getCapacity())
                    .orElse(0);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return this.getTank(tank)
                    .map(t -> t.getTank().isFluidValid(stack))
                    .orElse(false);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            for (AccessibleTank tank : this.tanks) {
                int filled = tank.getTank().fill(resource, action);
                if (filled > 0) {
                    return filled;
                }
            }
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            for (AccessibleTank tank : this.tanks) {
                FluidStack drained = tank.getTank().drain(resource, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (AccessibleTank tank : this.tanks) {
                FluidStack drained = tank.getTank().drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }
    }

    private static class AccessibleTank {

        private final int id;
        @Nonnull
        private final IFluidTank tank;
        @Nonnull
        private final Predicate<Direction> accessibleSides;

        private AccessibleTank(int id, @Nonnull IFluidTank tank, @Nonnull Direction... sides) {
            this(id, tank, side -> Arrays.asList(sides).contains(side));
        }

        private AccessibleTank(int id, @Nonnull IFluidTank tank,
                               @Nonnull Predicate<Direction> accessibleSides) {
            this.id = id;
            this.tank = tank;
            this.accessibleSides = accessibleSides;
        }

        @Nonnull
        private IFluidTank getTank() {
            return tank;
        }

        private int getId() {
            return id;
        }

        private boolean isAccessible(@Nonnull Direction side) {
            return accessibleSides.test(side);
        }
    }
}
