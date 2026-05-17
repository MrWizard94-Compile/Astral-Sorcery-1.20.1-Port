package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Block entity for the Starlight Infuser.
 * Performs liquid starlight infusion recipes on a single input item.
 * Requires a valid multiblock structure and liquid starlight in the tank.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * CapabilityItemHandler -> ForgeCapabilities.ITEM_HANDLER,
 * CapabilityFluidHandler -> ForgeCapabilities.FLUID_HANDLER,
 * LazyOptional pattern unchanged</p>
 */
public class BlockEntityInfuser extends BlockEntityTick implements IStarlightReceiver {

    private static final int SLOT_COUNT = 1;
    private static final int CAPACITY_MB = 4000;
    private static final double MAX_STARLIGHT_PER_TICK = 300.0;

    @Nonnull
    private final TileInventory inventory;
    private final LazyOptional<IItemHandler> itemCap;

    @Nonnull
    private final PrecisionSingleFluidTank tank;
    private final LazyOptional<IFluidHandler> fluidCap;

    private int recipeTick = 0;
    private int craftingProgress = 0;
    private boolean structureValid = false;
    private int ticksExisted = 0;

    @Nullable
    private ResourceLocation activeRecipeId = null;
    private double storedStarlight = 0;
    private boolean registeredInNetwork = false;

    public BlockEntityInfuser(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.INFUSER.get(), pos, state);
        this.inventory = new TileInventory(this, () -> SLOT_COUNT);
        this.itemCap = LazyOptional.of(() -> inventory);
        this.tank = new PrecisionSingleFluidTank(CAPACITY_MB);
        this.fluidCap = LazyOptional.of(() -> new TankWrapper(tank));
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide() && !registeredInNetwork) {
            StarlightNetworkHelper.registerReceiver(getLevel(), getBlockPos(), this);
            registeredInNetwork = true;
        }
    }

    // ---- IStarlightReceiver ----

    @Override
    public void receiveStarlight(double amount, @Nullable ResourceLocation constellation) {
        storedStarlight += amount;
        setChanged();
    }

    @Override
    public double getMaxStarlightInput() {
        return MAX_STARLIGHT_PER_TICK;
    }

    @Nullable
    @Override
    public Level getReceiverLevel() {
        return getLevel();
    }

    @Nonnull
    @Override
    public BlockPos getLocationPos() {
        return getBlockPos();
    }

    /** Structure check interval (ticks). */
    private static final int STRUCTURE_CHECK_INTERVAL = 40;

    /** Liquid starlight consumed per infusion tick. */
    private static final int FLUID_DRAIN_PER_TICK = 2;

    /** Recipe scan interval when idle (ticks). */
    private static final int RECIPE_SCAN_INTERVAL = 10;

    @Nullable
    private LiquidInfusion cachedRecipe = null;

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // Client-side: infusion particles handled by renderer
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Periodically validate multiblock structure
        if (ticksExisted % STRUCTURE_CHECK_INTERVAL == 0) {
            structureValid = validateStructure();
        }

        if (!structureValid) {
            if (craftingProgress > 0) {
                abortInfusion();
            }
            return;
        }

        // Active infusion in progress
        if (activeRecipeId != null && cachedRecipe != null) {
            tickInfusion(level);
        } else if (ticksExisted % RECIPE_SCAN_INTERVAL == 0) {
            // Scan for matching recipe
            tryFindInfusionRecipe(level);
        }
    }

    /**
     * Progresses the active infusion.
     */
    private void tickInfusion(@Nonnull Level level) {
        if (cachedRecipe == null) {
            abortInfusion();
            return;
        }

        // Verify input is still present
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty() || !cachedRecipe.getInputItem().test(input)) {
            abortInfusion();
            return;
        }

        // Drain liquid starlight from tank
        FluidStack drained = tank.drain(FLUID_DRAIN_PER_TICK, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty() || drained.getAmount() < FLUID_DRAIN_PER_TICK) {
            // Stall — not enough fluid, but don't abort (more may arrive)
            return;
        }
        tank.drain(FLUID_DRAIN_PER_TICK, IFluidHandler.FluidAction.EXECUTE);

        craftingProgress++;

        if (craftingProgress >= recipeTick) {
            completeInfusion(level);
        }
    }

    /**
     * Attempts to find a matching infusion recipe for the current input + fluid.
     */
    private void tryFindInfusionRecipe(@Nonnull Level level) {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty()) return;

        FluidStack stored = tank.getFluid();
        if (stored.isEmpty()) return;

        List<LiquidInfusion> allRecipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeTypesAS.LIQUID_INFUSION.get());

        for (LiquidInfusion recipe : allRecipes) {
            if (recipe.getInputItem().test(input)) {
                if (stored.getAmount() >= recipe.getFluidMbRequired()) {
                    startInfusion(recipe);
                    return;
                }
            }
        }
    }

    /**
     * Starts an infusion process.
     */
    private void startInfusion(@Nonnull LiquidInfusion recipe) {
        this.activeRecipeId = recipe.getId();
        this.cachedRecipe = recipe;
        this.recipeTick = recipe.getCraftDuration();
        this.craftingProgress = 0;
        markForUpdate();
        AstralSorcery.log.debug("Infuser started recipe {} at {}",
                recipe.getId(), worldPosition.toShortString());
    }

    /**
     * Completes the infusion: replaces input with output.
     */
    private void completeInfusion(@Nonnull Level level) {
        if (cachedRecipe == null) return;

        ItemStack result = cachedRecipe.getOutput().copy();
        inventory.setStackInSlot(0, result);

        // Consume the required fluid amount beyond what was drained per-tick
        int totalFluidRequired = cachedRecipe.getFluidMbRequired();
        int alreadyDrained = craftingProgress * FLUID_DRAIN_PER_TICK;
        int remaining = totalFluidRequired - alreadyDrained;
        if (remaining > 0) {
            tank.drain(remaining, IFluidHandler.FluidAction.EXECUTE);
        }

        AstralSorcery.log.debug("Infuser completed recipe {} at {}: produced {}",
                cachedRecipe.getId(), worldPosition.toShortString(),
                result.getDisplayName().getString());

        this.activeRecipeId = null;
        this.cachedRecipe = null;
        this.recipeTick = 0;
        this.craftingProgress = 0;
        markForUpdate();

        // TODO: Send particle burst packet for infusion completion visual
    }

    /**
     * Aborts the current infusion process.
     */
    private void abortInfusion() {
        this.activeRecipeId = null;
        this.cachedRecipe = null;
        this.recipeTick = 0;
        this.craftingProgress = 0;
        markForUpdate();
    }

    /**
     * Validates the infuser multiblock structure.
     * Requires 8 chalice positions around the infuser.
     */
    private boolean validateStructure() {
        Level level = getLevel();
        if (level == null) return false;

        // Must have sky access
        if (!level.canSeeSky(worldPosition.above())) {
            return false;
        }

        // Check for support blocks at cardinal and diagonal positions
        BlockPos[] supportPositions = {
                worldPosition.offset(1, 0, 0),
                worldPosition.offset(-1, 0, 0),
                worldPosition.offset(0, 0, 1),
                worldPosition.offset(0, 0, -1),
                worldPosition.offset(1, 0, 1),
                worldPosition.offset(-1, 0, 1),
                worldPosition.offset(1, 0, -1),
                worldPosition.offset(-1, 0, -1)
        };

        for (BlockPos support : supportPositions) {
            if (level.getBlockState(support).isAir()) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    public TileInventory getInventory() {
        return inventory;
    }

    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    public int getRecipeTick() {
        return recipeTick;
    }

    public int getCraftingProgress() {
        return craftingProgress;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    @Nullable
    public ResourceLocation getActiveRecipeId() {
        return activeRecipeId;
    }

    public void setActiveRecipeId(@Nullable ResourceLocation recipeId) {
        this.activeRecipeId = recipeId;
        markForUpdate();
    }

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    /**
     * Whether an infusion recipe is currently in progress.
     * Used by the renderer to show infusion particle effects.
     */
    public boolean isInfusing() {
        return activeRecipeId != null && craftingProgress > 0;
    }

    /**
     * Get the infusion progress as a ratio [0, 1].
     * Used by the renderer for progress visualization.
     */
    public float getInfusionProgress() {
        if (recipeTick <= 0) return 0f;
        return Math.min(1.0f, (float) craftingProgress / (float) recipeTick);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        fluidCap.invalidate();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isClientSide()) {
            StarlightNetworkHelper.removeNode(getLevel(), getBlockPos());
        }
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.inventory.deserialize(compound.getCompound("inventory"));
        this.tank.readNBT(compound.getCompound("tank"));
        if (compound.contains("activeRecipe")) {
            this.activeRecipeId = new ResourceLocation(compound.getString("activeRecipe"));
        } else {
            this.activeRecipeId = null;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.put("inventory", this.inventory.serializeNBT());
        compound.put("tank", tank.writeNBT());
        if (activeRecipeId != null) {
            compound.putString("activeRecipe", activeRecipeId.toString());
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.recipeTick = compound.getInt("recipeTick");
        this.craftingProgress = compound.getInt("craftingProgress");
        this.structureValid = compound.getBoolean("structureValid");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putInt("recipeTick", recipeTick);
        compound.putInt("craftingProgress", craftingProgress);
        compound.putBoolean("structureValid", structureValid);
    }

    /**
     * Wraps the IFluidTank as a full IFluidHandler for capability exposure.
     * Allows both fill and drain for the infuser basin.
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
