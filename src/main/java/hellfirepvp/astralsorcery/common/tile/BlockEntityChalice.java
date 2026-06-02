package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
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
    private int nextInteractionTick = -1;

    @Nonnull
    private final List<BlockPos> linkedChalices = new ArrayList<>();

    public BlockEntityChalice(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.CHALICE.get(), pos, state);
        this.tank = new PrecisionSingleFluidTank(CAPACITY_MB);
        this.fluidCap = LazyOptional.of(() -> new TankWrapper(tank));
    }

    @SuppressWarnings("null")
    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) return;

        if (ticksExisted % 20 != 0) return;

        Level level = getLevel();
        if (level == null) return;

        // Pull liquid starlight from nearby lightwells and fountains
        if (tank.getFluidAmount() < tank.getCapacity()) {
            tickLightwellDraw(level);
        }

        FluidStack thisFluid = tank.getFluid();
        if (thisFluid.isEmpty()) return;

        if (ticksExisted % 40 == 0) {
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.WELL_COLLECT, getBlockPos()),
                    (ServerLevel) level, getBlockPos());
        }

        linkedChalices.removeIf(pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            return !(be instanceof BlockEntityChalice);
        });

        for (BlockPos otherPos : linkedChalices) {
            BlockEntity be = level.getBlockEntity(otherPos);
            if (!(be instanceof BlockEntityChalice other)) continue;

            FluidStack otherFluid = other.tank.getFluid();
            if (otherFluid.isEmpty() || !otherFluid.isFluidEqual(thisFluid)) continue;

            int thisAmt = thisFluid.getAmount();
            int otherAmt = otherFluid.getAmount();
            if (thisAmt == otherAmt) continue;

            // Move half the imbalance per tick, minimum 1 mB
            int delta = (thisAmt - otherAmt) / 2;
            if (delta == 0) continue;

            if (delta > 0) {
                FluidStack moved = tank.drain(delta, IFluidHandler.FluidAction.EXECUTE);
                other.tank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
            } else {
                FluidStack moved = other.tank.drain(-delta, IFluidHandler.FluidAction.EXECUTE);
                tank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
            }
            markForUpdate();
            other.markForUpdate();
        }

        tickInteractions(level);
    }

    /**
     * Pull liquid starlight from nearby lightwells within 8 blocks.
     * Matches the 1.16 chalice behavior where a chalice automatically absorbs
     * fluid from a nearby lightwell, enabling passive fluid storage.
     */
    private void tickLightwellDraw(@Nonnull Level level) {
        BlockPos thisPos = getBlockPos();
        int radius = 8;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos checkPos = thisPos.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (!(be instanceof BlockEntityWell well)) continue;

                    FluidStack available = well.getTank().drain(400, IFluidHandler.FluidAction.SIMULATE);
                    if (available.isEmpty() || available.getAmount() < 100) continue;

                    int fillable = tank.fill(available, IFluidHandler.FluidAction.SIMULATE);
                    if (fillable <= 0) return; // chalice is full — stop searching

                    FluidStack toMove = new FluidStack(available.getFluid(), Math.min(fillable, available.getAmount()));
                    well.getTank().drain(toMove, IFluidHandler.FluidAction.EXECUTE);
                    tank.fill(toMove, IFluidHandler.FluidAction.EXECUTE);
                    markForUpdate();
                    return; // one well per tick
                }
            }
        }
    }

    private void tickInteractions(@Nonnull Level level) {
        RandomSource rng = level.getRandom();
        if (nextInteractionTick < 0) {
            nextInteractionTick = ticksExisted + 20 + rng.nextInt(40);
            return;
        }
        if (ticksExisted < nextInteractionTick) return;
        nextInteractionTick = ticksExisted + 20 + rng.nextInt(40);

        FluidStack thisFluid = tank.getFluid();
        if (thisFluid.isEmpty()) return;

        List<LiquidInteraction> allRecipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeTypesAS.LIQUID_INTERACTION.get());
        if (allRecipes.isEmpty()) return;

        // Scan nearby positions for other chalices (±16 X/Z, ±4 Y)
        BlockPos thisPos = getBlockPos();
        List<BlockEntityChalice> nearby = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -16; dx <= 16; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -16; dz <= 16; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    mutable.set(thisPos.getX() + dx, thisPos.getY() + dy, thisPos.getZ() + dz);
                    if (level.getBlockState(mutable).getBlock() == BlocksAS.CHALICE.get()) {
                        BlockEntity be = level.getBlockEntity(mutable);
                        if (be instanceof BlockEntityChalice other) {
                            nearby.add(other);
                        }
                    }
                }
            }
        }
        if (nearby.isEmpty()) return;

        // Fisher-Yates shuffle using level RNG for fairness
        for (int i = nearby.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            BlockEntityChalice tmp = nearby.get(i);
            nearby.set(i, nearby.get(j));
            nearby.set(j, tmp);
        }

        for (BlockEntityChalice other : nearby) {
            FluidStack otherFluid = other.tank.getFluid();
            if (otherFluid.isEmpty() || thisFluid.getFluid() == otherFluid.getFluid()) continue;

            List<LiquidInteraction> matching = new ArrayList<>();
            for (LiquidInteraction recipe : allRecipes) {
                if (recipe.matchesFluids(thisFluid, otherFluid)) matching.add(recipe);
            }
            if (matching.isEmpty()) continue;

            LiquidInteraction recipe = pickWeightedRecipe(matching, rng);
            if (recipe == null) continue;

            FluidStack rf1 = recipe.getInputFluid1();
            FluidStack rf2 = recipe.getInputFluid2();
            int drainThis, drainOther;
            if (rf1.getFluid() == thisFluid.getFluid()) {
                drainThis = rf1.getAmount();
                drainOther = rf2.getAmount();
            } else {
                drainThis = rf2.getAmount();
                drainOther = rf1.getAmount();
            }

            if (tank.getFluidAmount() < drainThis || other.tank.getFluidAmount() < drainOther) continue;

            tank.drain(drainThis, IFluidHandler.FluidAction.EXECUTE);
            other.tank.drain(drainOther, IFluidHandler.FluidAction.EXECUTE);
            markForUpdate();
            other.markForUpdate();

            ItemStack output = recipe.getOutputItem();
            if (!output.isEmpty()) {
                BlockPos otherPos = other.getBlockPos();
                double mx = (thisPos.getX() + otherPos.getX()) / 2.0 + 0.5;
                double my = Math.max(thisPos.getY(), otherPos.getY()) + 1.2;
                double mz = (thisPos.getZ() + otherPos.getZ()) / 2.0 + 0.5;
                ItemEntity item = new ItemEntity(level, mx, my, mz, output);
                item.setDefaultPickUpDelay();
                level.addFreshEntity(item);
            }
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(PktPlayEffect.EffectType.LIQUID_INTERACTION, thisPos),
                    (ServerLevel) level, thisPos);
            return; // one interaction per trigger
        }
    }

    @Nullable
    private static LiquidInteraction pickWeightedRecipe(@Nonnull List<LiquidInteraction> candidates,
                                                         @Nonnull RandomSource rng) {
        if (candidates.isEmpty()) return null;
        if (candidates.size() == 1) return candidates.get(0);
        float total = 0;
        for (LiquidInteraction r : candidates) total += r.getWeight();
        float roll = rng.nextFloat() * total;
        float cum = 0;
        for (LiquidInteraction r : candidates) {
            cum += r.getWeight();
            if (roll < cum) return r;
        }
        return candidates.get(candidates.size() - 1);
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
