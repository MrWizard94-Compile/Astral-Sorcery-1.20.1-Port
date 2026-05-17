package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Block entity for the Starlight Crafting Altar.
 * The core crafting machine of Astral Sorcery.
 * Handles inventory, starlight accumulation, and recipe processing.
 *
 * <p>The altar has 4 tiers, each unlocking more recipe slots and capabilities:
 * <ul>
 *   <li>Discovery: 3x3 grid (9 slots), 1000 SL capacity</li>
 *   <li>Attunement: 5x5 cross pattern (13 slots), 2000 SL capacity</li>
 *   <li>Constellation: 5x5 full grid (21 slots), 4000 SL capacity</li>
 *   <li>Radiance: 5x5 + outer relays (25 slots + 4 relay), 8000 SL capacity</li>
 * </ul></p>
 *
 * <p>Implements {@link IStarlightReceiver} to accept starlight from the
 * transmission network (collector crystals → lenses → altar).</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * CapabilityItemHandler -> ForgeCapabilities.ITEM_HANDLER,
 * Container/Menu system for player interaction</p>
 */
public class BlockEntityAltar extends BlockEntityTick implements IStarlightReceiver {

    private static final int MAX_SLOTS = 25;

    /** How often to scan for recipes when idle (ticks). */
    private static final int RECIPE_SCAN_INTERVAL = 20;

    /** Starlight consumed per tick while crafting, as a fraction of total required. */
    private static final double STARLIGHT_DRAIN_FACTOR = 0.01;

    @Nonnull
    private final TileInventory inventory;
    private final LazyOptional<IItemHandler> itemCap;

    private double storedStarlight = 0;
    private double starlightCapacity = 1000;
    private int recipeTick = 0;
    private int ticksExisted = 0;
    private boolean structureValid = false;
    private boolean isCrafting = false;
    private boolean registeredInNetwork = false;

    @Nullable
    private ResourceLocation receivedConstellation = null;

    @Nullable
    private SimpleAltarRecipe activeRecipe = null;

    @Nullable
    private ResourceLocation activeRecipeId = null;

    public BlockEntityAltar(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.ALTAR.get(), pos, state);
        this.inventory = new TileInventory(this, () -> MAX_SLOTS);
        this.itemCap = LazyOptional.of(() -> inventory);
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        updateCapacityFromTier();
        if (!isClientSide() && !registeredInNetwork) {
            StarlightNetworkHelper.registerReceiver(getLevel(), getBlockPos(), this);
            registeredInNetwork = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // Client-side: altar animation/particles
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Server-side crafting logic:
        if (isCrafting) {
            if (activeRecipe == null && activeRecipeId != null) {
                // Post-world-load: resolve the recipe from its ID
                resolveActiveRecipe(level);
            }
            if (activeRecipe != null) {
                tickCrafting(level);
            }
        } else if (ticksExisted % RECIPE_SCAN_INTERVAL == 0) {
            // Periodically scan for matching recipes when idle
            tryFindRecipe(level);
        }

        // Gradually dissipate stored starlight if not crafting (small leak)
        if (!isCrafting && storedStarlight > 0) {
            storedStarlight = Math.max(0, storedStarlight - 0.5);
        }
    }

    /**
     * Progresses the active crafting operation each tick.
     * Drains starlight and increments the recipe tick counter.
     * Aborts if starlight runs out or recipe no longer matches.
     */
    private void tickCrafting(@Nonnull Level level) {
        if (activeRecipe == null) {
            abortCrafting();
            return;
        }

        // Verify recipe still matches (items might have been removed by hopper, etc.)
        if (!activeRecipe.matches(inventory.toContainer(), level)) {
            AstralSorcery.log.debug("Altar craft aborted: recipe no longer matches at {}",
                    worldPosition.toShortString());
            abortCrafting();
            return;
        }

        // Drain starlight proportionally to craft progress
        double drainPerTick = activeRecipe.getStarlightRequired() * STARLIGHT_DRAIN_FACTOR;
        if (storedStarlight < drainPerTick) {
            // Stall — not enough starlight this tick, but don't abort
            // (starlight may arrive next tick from the network)
            return;
        }
        storedStarlight -= drainPerTick;
        recipeTick++;

        // Check completion
        if (recipeTick >= activeRecipe.getCraftDuration()) {
            completeCrafting(level);
        }
    }

    /**
     * Attempts to find a matching altar recipe for the current inventory and tier.
     * If found and sufficient starlight is available, starts crafting.
     */
    private void tryFindRecipe(@Nonnull Level level) {
        if (!structureValid) return;

        BlockAltar.AltarType currentTier = getAltarType();
        Optional<SimpleAltarRecipe> match = level.getRecipeManager()
                .getRecipeFor(RecipeTypesAS.ALTAR.get(), inventory.toContainer(), level);

        if (match.isPresent()) {
            SimpleAltarRecipe recipe = match.get();
            // Recipe tier must match or be below current altar tier
            if (recipe.getAltarType().ordinal() > currentTier.ordinal()) {
                return;
            }
            // Focus constellation check (Constellation and Radiance tiers)
            if (recipe.getFocusConstellation() != null) {
                if (receivedConstellation == null
                        || !receivedConstellation.equals(recipe.getFocusConstellation())) {
                    return;
                }
            }
            // Start crafting if we have at least some starlight
            if (storedStarlight >= activeRecipeMinimumStarlight(recipe)) {
                startCraftingRecipe(recipe);
            }
        }
    }

    /**
     * Minimum stored starlight required to begin a craft.
     * At least 10% of total required starlight must be available to start.
     */
    private double activeRecipeMinimumStarlight(@Nonnull SimpleAltarRecipe recipe) {
        return recipe.getStarlightRequired() * 0.1;
    }

    /**
     * Begins crafting the given recipe.
     */
    private void startCraftingRecipe(@Nonnull SimpleAltarRecipe recipe) {
        this.activeRecipe = recipe;
        this.activeRecipeId = recipe.getId();
        this.isCrafting = true;
        this.recipeTick = 0;
        markForUpdate();
        AstralSorcery.log.debug("Altar started crafting {} at {}",
                recipe.getId(), worldPosition.toShortString());
    }

    /**
     * Completes the active recipe: consumes inputs, produces output.
     */
    private void completeCrafting(@Nonnull Level level) {
        if (activeRecipe == null) return;

        ItemStack result = activeRecipe.assemble(inventory.toContainer(),
                level.registryAccess());

        // Consume inputs: one item from each non-empty slot
        int slotCount = activeRecipe.getExpectedSlotCount();
        for (int i = 0; i < slotCount; i++) {
            ItemStack slot = inventory.getStackInSlot(i);
            if (!slot.isEmpty()) {
                // Handle container items (buckets, etc.)
                ItemStack containerItem = slot.getCraftingRemainingItem();
                inventory.extractItem(i, 1, false);
                if (!containerItem.isEmpty()) {
                    inventory.insertItem(i, containerItem, false);
                }
            }
        }

        // Output goes to the first empty slot beyond the crafting grid, or drops
        // For now, use slot 0 as output (the center result slot concept)
        // In the original mod, the output appears floating above the altar
        // and is collected by the player. Here we spawn it as an item entity.
        spawnCraftResult(result);

        // Reset state
        this.activeRecipe = null;
        this.activeRecipeId = null;
        this.isCrafting = false;
        this.recipeTick = 0;
        markForUpdate();

        AstralSorcery.log.debug("Altar craft complete at {}: produced {}",
                worldPosition.toShortString(), result.getDisplayName().getString());

        // TODO: Send particle burst packet for craft completion visual
        // TODO: Play altar crafting completion sound
    }

    /**
     * Spawns the crafted result as an item entity above the altar.
     */
    private void spawnCraftResult(@Nonnull ItemStack result) {
        Level level = getLevel();
        if (level == null || result.isEmpty()) return;

        net.minecraft.world.entity.item.ItemEntity itemEntity =
                new net.minecraft.world.entity.item.ItemEntity(
                        level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 1.5,
                        worldPosition.getZ() + 0.5,
                        result);
        itemEntity.setDeltaMovement(0, 0.1, 0);
        itemEntity.setPickUpDelay(20); // Brief delay before pickup
        level.addFreshEntity(itemEntity);
    }

    // ========================================================================
    // IStarlightReceiver implementation
    // ========================================================================

    @Override
    public void receiveStarlight(double amount, @Nullable ResourceLocation constellation) {
        double space = starlightCapacity - storedStarlight;
        if (space <= 0) return;

        double received = Math.min(amount, space);
        storedStarlight += received;
        this.receivedConstellation = constellation;
        setChanged();
    }

    @Override
    public double getMaxStarlightInput() {
        // Accept up to 10% of capacity per tick
        return starlightCapacity * 0.1;
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

    // ========================================================================
    // Public API
    // ========================================================================

    @Nonnull
    public BlockAltar.AltarType getAltarType() {
        return getBlockState().getValue(BlockAltar.ALTAR_TYPE);
    }

    @Nonnull
    public TileInventory getInventory() {
        return inventory;
    }

    public double getStoredStarlight() {
        return storedStarlight;
    }

    public void setStoredStarlight(double starlight) {
        this.storedStarlight = Math.max(0, Math.min(starlightCapacity, starlight));
    }

    /**
     * Get stored starlight as a float, for use by renderers.
     */
    public float getStarlightStored() {
        return (float) storedStarlight;
    }

    /**
     * Get the starlight capacity for the current altar tier, for use by renderers.
     */
    public float getStarlightCapacity() {
        return (float) starlightCapacity;
    }

    /**
     * Get the fill percentage [0, 1] of stored starlight.
     */
    public float getStarlightPercentage() {
        return starlightCapacity > 0 ? (float) (storedStarlight / starlightCapacity) : 0;
    }

    public int getRecipeTick() {
        return recipeTick;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public void setStructureValid(boolean valid) {
        this.structureValid = valid;
    }

    public boolean isCrafting() {
        return isCrafting;
    }

    /**
     * Start the crafting process externally (e.g., from a GUI interaction).
     * Typically called after the player places ingredients and the altar
     * auto-detects a recipe match, but can be triggered manually.
     */
    public void startCrafting() {
        Level level = getLevel();
        if (level != null && !level.isClientSide()) {
            tryFindRecipe(level);
        }
    }

    /**
     * Abort the current crafting process (e.g., structure broken, items removed).
     */
    public void abortCrafting() {
        this.isCrafting = false;
        this.recipeTick = 0;
        this.activeRecipe = null;
        this.activeRecipeId = null;
        markForUpdate();
    }

    /**
     * Get the maximum number of active crafting slots for the current tier.
     */
    public int getActiveSlotCount() {
        return switch (getAltarType()) {
            case DISCOVERY -> 9;
            case ATTUNEMENT -> 13;
            case CONSTELLATION -> 21;
            case RADIANCE -> 25;
        };
    }

    /**
     * Gets the constellation of the starlight currently being received (for VFX).
     */
    @Nullable
    public ResourceLocation getReceivedConstellation() {
        return receivedConstellation;
    }

    /**
     * Updates the internal capacity based on the altar tier.
     * Called on first tick and when the block state changes.
     */
    private void updateCapacityFromTier() {
        this.starlightCapacity = switch (getAltarType()) {
            case DISCOVERY -> 1000.0;
            case ATTUNEMENT -> 2000.0;
            case CONSTELLATION -> 4000.0;
            case RADIANCE -> 8000.0;
        };
    }

    // ========================================================================
    // Capabilities
    // ========================================================================

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
    }

    // ========================================================================
    // Lifecycle
    // ========================================================================

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isClientSide()) {
            StarlightNetworkHelper.removeNode(getLevel(), getBlockPos());
        }
    }

    // ========================================================================
    // NBT serialization
    // ========================================================================

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.inventory.deserialize(compound.getCompound("inventory"));
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.put("inventory", this.inventory.serializeNBT());
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.storedStarlight = compound.getDouble("storedStarlight");
        this.recipeTick = compound.getInt("recipeTick");
        this.structureValid = compound.getBoolean("structureValid");
        this.isCrafting = compound.getBoolean("isCrafting");
        this.starlightCapacity = compound.contains("starlightCapacity")
                ? compound.getDouble("starlightCapacity") : 1000.0;
        if (compound.contains("receivedConstellation")) {
            this.receivedConstellation = new ResourceLocation(
                    compound.getString("receivedConstellation"));
        }
        if (compound.contains("activeRecipeId")) {
            this.activeRecipeId = new ResourceLocation(compound.getString("activeRecipeId"));
            // Recipe object is resolved lazily on next tick from the RecipeManager
            this.activeRecipe = null;
        } else {
            this.activeRecipeId = null;
            this.activeRecipe = null;
        }
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putDouble("storedStarlight", storedStarlight);
        compound.putInt("recipeTick", recipeTick);
        compound.putBoolean("structureValid", structureValid);
        compound.putBoolean("isCrafting", isCrafting);
        compound.putDouble("starlightCapacity", starlightCapacity);
        if (receivedConstellation != null) {
            compound.putString("receivedConstellation", receivedConstellation.toString());
        }
        if (activeRecipeId != null) {
            compound.putString("activeRecipeId", activeRecipeId.toString());
        }
    }

    /**
     * Resolves the active recipe from its ID after world load.
     * Called lazily on first tick when isCrafting is true but activeRecipe is null.
     */
    private void resolveActiveRecipe(@Nonnull Level level) {
        if (activeRecipeId == null) return;
        Optional<SimpleAltarRecipe> resolved = level.getRecipeManager()
                .getRecipeFor(RecipeTypesAS.ALTAR.get(), inventory.toContainer(), level)
                .filter(r -> r.getId().equals(activeRecipeId));
        if (resolved.isPresent()) {
            this.activeRecipe = resolved.get();
        } else {
            AstralSorcery.log.warn("Altar at {} could not resolve recipe {} after load — aborting",
                    worldPosition.toShortString(), activeRecipeId);
            abortCrafting();
        }
    }
}
