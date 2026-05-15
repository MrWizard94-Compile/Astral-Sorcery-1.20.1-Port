package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
 * - Discovery: 3x3 grid (9 slots)
 * - Attunement: 5x5 cross pattern (13 slots)
 * - Constellation: 5x5 full grid (21 slots)
 * - Radiance: 5x5 + outer relays (25 slots + 4 relay)</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * CapabilityItemHandler -> ForgeCapabilities.ITEM_HANDLER,
 * Container/Menu system for player interaction</p>
 */
public class BlockEntityAltar extends BlockEntityTick {

    private static final int MAX_SLOTS = 25;

    @Nonnull
    private final TileInventory inventory;
    private final LazyOptional<IItemHandler> itemCap;

    private double storedStarlight = 0;
    private int recipeTick = 0;
    private boolean structureValid = false;

    public BlockEntityAltar(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.ALTAR.get(), pos, state);
        this.inventory = new TileInventory(this, () -> MAX_SLOTS);
        this.itemCap = LazyOptional.of(() -> inventory);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side altar animation/particles
            return;
        }

        // TODO: Server-side crafting logic:
        // 1. Check multiblock structure validity
        // 2. Collect starlight from network
        // 3. Match altar recipe for current tier
        // 4. Process recipe tick by tick
        // 5. Complete crafting and produce output
    }

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
        this.storedStarlight = Math.max(0, starlight);
    }

    public int getRecipeTick() {
        return recipeTick;
    }

    public boolean isStructureValid() {
        return structureValid;
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
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putDouble("storedStarlight", storedStarlight);
        compound.putInt("recipeTick", recipeTick);
        compound.putBoolean("structureValid", structureValid);
    }
}
