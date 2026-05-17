package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    @Nonnull
    private final TileInventory inventory;
    private final LazyOptional<IItemHandler> itemCap;

    private double storedStarlight = 0;
    private double starlightCapacity = 1000;
    private int recipeTick = 0;
    private boolean structureValid = false;
    private boolean isCrafting = false;
    private boolean registeredInNetwork = false;

    @Nullable
    private ResourceLocation receivedConstellation = null;

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
        if (isClientSide()) {
            // Client-side: altar animation/particles
            return;
        }

        // Server-side crafting logic:
        if (isCrafting) {
            recipeTick++;
            // TODO: Check recipe progress completion
            // When recipeTick reaches required ticks, complete the craft
        }

        // Gradually dissipate stored starlight if not crafting (small leak)
        if (!isCrafting && storedStarlight > 0) {
            storedStarlight = Math.max(0, storedStarlight - 0.5);
        }
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
     * Start the crafting process. Called when a valid recipe match is confirmed.
     */
    public void startCrafting() {
        this.isCrafting = true;
        this.recipeTick = 0;
        markForUpdate();
    }

    /**
     * Abort the current crafting process (e.g., structure broken).
     */
    public void abortCrafting() {
        this.isCrafting = false;
        this.recipeTick = 0;
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
    }
}
