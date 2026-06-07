package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
public class BlockEntityWell extends BlockEntityTick implements IStarlightReceiver {

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

    /** Sub-mB accumulated but not yet added to the integer tank. */
    private double fractionalFluid = 0.0;

    /** Starlight received from the network this tick â€” consumed during production. */
    private double starlightBuffer = 0.0;

    /** Ticks until the catalyst degrades by 1 durability point. */
    private static final int CATALYST_DEGRADE_INTERVAL = 600; // 30 seconds

    @Nullable
    private WellLiquefaction cachedRecipe = null;
    private boolean recipeDirty = true;

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        tank.setCapacity(hellfirepvp.astralsorcery.common.data.config.CommonConfig.CONFIG.wellMaxStorage.get());
        if (!isClientSide()) {
            StarlightNetworkHelper.registerReceiver(getLevel(), getBlockPos(), this);
            hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler handler =
                    StarlightNetworkHelper.getHandler(getLevel());
            if (handler != null) handler.attemptAutoLinkTo(getBlockPos());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isClientSide()) {
            hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler handler =
                    StarlightNetworkHelper.getHandler(getLevel());
            if (handler != null) handler.removeAutoLinkTo(getBlockPos());
        }
        StarlightNetworkHelper.removeNode(getLevel(), getBlockPos());
    }

    // IStarlightReceiver implementation
    @Override
    public void receiveStarlight(double amount, @Nullable ResourceLocation constellation) {
        starlightBuffer += amount;
    }

    @Override
    public double getMaxStarlightInput() {
        return 4.0;
    }

    @Nullable
    @Override
    public Level getReceiverLevel() {
        return this.level;
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // Client-side: drip particle effects handled by renderer
            return;
        }

        if (catalystStack.isEmpty()) {
            productionProgress = 0;
            cachedRecipe = null;
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Find or use cached recipe
        if (recipeDirty || cachedRecipe == null) {
            cachedRecipe = findRecipeFor(level, catalystStack);
            recipeDirty = false;
        }
        WellLiquefaction cr = cachedRecipe;
        if (cr == null) {
            return; // No valid recipe for this catalyst
        }

        // Must have sky access for starlight collection
        if (!level.canSeeSky(worldPosition.above())) {
            return;
        }

        // Production rate modified by time of day, weather, and received starlight
        float distributionFactor = CelestialHandler.getStarlightDistributionFactor(level);
        double starlightBoost = Math.sqrt(starlightBuffer + 1.0);
        double ratePerTick = hellfirepvp.astralsorcery.common.data.config.CommonConfig.CONFIG.wellBaseProduction.get()
                * cr.getProductionMultiplier()
                * distributionFactor
                * starlightBoost;
        starlightBuffer = 0;

        if (ratePerTick <= 0) return;

        // Accumulate fluid with sub-mB precision
        fractionalFluid += ratePerTick;
        productionProgress = Math.min(1.0, productionProgress + ratePerTick / 10.0);

        // When we've accumulated 1+ mB, add to the tank
        if (fractionalFluid >= 1.0) {
            int wholeMB = (int) fractionalFluid;
            fractionalFluid -= wholeMB;

            FluidStack produced = cr.getOutputFluid();
            produced.setAmount(wholeMB);

            int filled = tank.fill(produced, IFluidHandler.FluidAction.EXECUTE);
            if (filled < wholeMB) {
                // Tank is full â€” excess is lost (overflow)
                fractionalFluid = 0;
            }
        }

        // Degrade catalyst over time — gated by configurable probability per cycle
        if (getTicksExisted() % CATALYST_DEGRADE_INTERVAL == 0) {
            double chance = hellfirepvp.astralsorcery.common.data.config.CommonConfig.CONFIG.wellCatalystConsumptionChance.get();
            if (level.getRandom().nextFloat() < (float) chance) {
                degradeCatalyst();
            }
        }
    }

    /**
     * Degrades the catalyst item. For damageable items, damages them.
     * For non-damageable items, shrinks the stack.
     */
    private void degradeCatalyst() {
        if (catalystStack.isEmpty()) return;

        if (catalystStack.isDamageableItem()) {
            catalystStack.setDamageValue(catalystStack.getDamageValue() + 1);
            if (catalystStack.getDamageValue() >= catalystStack.getMaxDamage()) {
                catalystStack = ItemStack.EMPTY;
                recipeDirty = true;
                markForUpdate();
                sendCatalystConsumedEffect();
            }
        } else {
            catalystStack.shrink(1);
            if (catalystStack.isEmpty()) {
                recipeDirty = true;
                markForUpdate();
                sendCatalystConsumedEffect();
            }
        }
    }

    private void sendCatalystConsumedEffect() {
        if (getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(PktPlayEffect.EffectType.WELL_FILL_BURST, worldPosition),
                    serverLevel, worldPosition);
        }
    }

    /**
     * Finds the WellLiquefaction recipe matching the given item.
     */
    @Nullable
    private WellLiquefaction findRecipeFor(@Nonnull Level level, @Nonnull ItemStack stack) {
        List<WellLiquefaction> allRecipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeTypesAS.WELL_LIQUEFACTION.get());
        for (WellLiquefaction recipe : allRecipes) {
            if (recipe.matches(stack)) {
                return recipe;
            }
        }
        return null;
    }

    @Nonnull
    public ItemStack getCatalystStack() {
        return catalystStack;
    }

    public void setCatalystStack(@Nonnull ItemStack stack) {
        this.catalystStack = stack;
        this.recipeDirty = true;
        this.fractionalFluid = 0;
        this.productionProgress = 0;
        markForUpdate();
    }

    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    public double getProductionProgress() {
        return productionProgress;
    }

    /**
     * Get the ratio of stored fluid to max capacity [0, 1].
     * Used by the renderer to adjust fluid level visuals.
     */
    public float getFluidFillRatio() {
        if (tank.getCapacity() <= 0) return 0f;
        return (float) tank.getFluid().getAmount() / (float) tank.getCapacity();
    }

    /**
     * Whether there is a catalyst item in the well.
     * Used by the renderer to show/hide the catalyst item.
     */
    public boolean hasCatalyst() {
        return !catalystStack.isEmpty();
    }

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */

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
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return false; // Well only outputs; input is via catalyst item
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0; // No external filling
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack stored = tank.getFluid();
            if (stored.isEmpty() || !stored.isFluidEqual(resource)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
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
