package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Spectral Relay.
 * Collects additional starlight for nearby altars.
 * Part of altar multiblock structures placed on relay pedestals.
 *
 * <p>Has a single inventory slot for the relay item, which
 * determines the type/amount of additional starlight collected.
 * The relay is linked to a specific altar position.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * CapabilityItemHandler -> ForgeCapabilities.ITEM_HANDLER,
 * NBTUtil -> NbtUtils</p>
 */
public class BlockEntityRelay extends BlockEntityTick {

    private static final int SLOT_COUNT = 1;

    @Nonnull
    private final TileInventory inventory;
    private final LazyOptional<IItemHandler> itemCap;

    @Nullable
    private BlockPos linkedAltar = null;

    public BlockEntityRelay(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.RELAY.get(), pos, state);
        this.inventory = new TileInventory(this, () -> SLOT_COUNT);
        this.itemCap = LazyOptional.of(() -> inventory);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side relay glow/particle effects
            return;
        }

        // TODO: Server-side relay logic:
        // 1. Check if linkedAltar position contains a valid BlockEntityAltar
        // 2. Collect starlight based on heldItem properties
        // 3. Forward collected starlight to the linked altar
    }

    @Nonnull
    public TileInventory getInventory() {
        return inventory;
    }

    @Nonnull
    public ItemStack getHeldItem() {
        return inventory.getStackInSlot(0);
    }

    public void setHeldItem(@Nonnull ItemStack stack) {
        inventory.setStackInSlot(0, stack);
    }

    @Nullable
    public BlockPos getLinkedAltar() {
        return linkedAltar;
    }

    public void setLinkedAltar(@Nullable BlockPos altarPos) {
        this.linkedAltar = altarPos;
        markForUpdate();
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
        if (compound.contains("linkedAltar")) {
            this.linkedAltar = NbtUtils.readBlockPos(compound.getCompound("linkedAltar"));
        } else {
            this.linkedAltar = null;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.put("inventory", this.inventory.serializeNBT());
        if (linkedAltar != null) {
            compound.put("linkedAltar", NbtUtils.writeBlockPos(linkedAltar));
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
    }
}
