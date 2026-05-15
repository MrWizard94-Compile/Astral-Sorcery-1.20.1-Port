package hellfirepvp.astralsorcery.common.util.tile;

import hellfirepvp.astralsorcery.common.tile.base.BlockEntitySynchronized;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Item inventory backed by {@link ItemStackHandler}, owned by a {@link BlockEntitySynchronized}.
 * Supports per-side capability exposure, custom stack size limiting, and change listeners.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntitySynchronized -> BlockEntitySynchronized,
 * CompoundNBT -> CompoundTag,
 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> ForgeCapabilities.ITEM_HANDLER</p>
 */
public class TileInventory extends ItemStackHandler implements Iterable<ItemStack> {

    @Nonnull
    protected final BlockEntitySynchronized tile;
    @Nullable
    protected final Consumer<Integer> changeListener;
    @Nonnull
    protected final Supplier<Integer> slotCountProvider;
    @Nonnull
    protected Set<Direction> applicableSides = new HashSet<>();
    @Nonnull
    protected BiFunction<Integer, ItemStack, Integer> stackSizeLimiter;

    public TileInventory(@Nonnull BlockEntitySynchronized tile,
                         @Nonnull Supplier<Integer> slotCountProvider,
                         @Nonnull Direction... applicableSides) {
        this(tile, slotCountProvider, null, applicableSides);
    }

    public TileInventory(@Nonnull BlockEntitySynchronized tile,
                         @Nonnull Supplier<Integer> slotCountProvider,
                         @Nullable Consumer<Integer> changeListener,
                         @Nonnull Direction... applicableSides) {
        this(tile, slotCountProvider, changeListener,
                Arrays.asList(applicableSides),
                (slot, stack) -> stack.getMaxStackSize());
    }

    protected TileInventory(@Nonnull BlockEntitySynchronized tile,
                            @Nonnull Supplier<Integer> slotCountProvider,
                            @Nullable Consumer<Integer> changeListener,
                            @Nonnull Collection<Direction> applicableSides,
                            @Nonnull BiFunction<Integer, ItemStack, Integer> stackSizeLimiter) {
        super(slotCountProvider.get());
        this.tile = tile;
        this.changeListener = changeListener;
        this.slotCountProvider = slotCountProvider;
        this.applicableSides.addAll(applicableSides);
        this.stackSizeLimiter = stackSizeLimiter;
    }

    @Nonnull
    public TileInventory filterMaxStackSize(@Nonnull BiFunction<Integer, ItemStack, Integer> stackSizeLimiter) {
        this.stackSizeLimiter = stackSizeLimiter;
        return this;
    }

    @Nonnull
    protected TileInventory makeNewInstance() {
        return new TileInventory(this.tile, this.slotCountProvider,
                this.changeListener, MiscUtils.copySet(this.applicableSides),
                this.stackSizeLimiter);
    }

    @Nonnull
    public TileInventory deserialize(@Nonnull CompoundTag tag) {
        this.deserializeNBT(tag);
        if (this.getSlots() != this.slotCountProvider.get()) {
            TileInventory newInv = makeNewInstance();
            for (int i = 0; i < Math.min(this.getSlots(), newInv.getSlots()); i++) {
                ItemStack old = this.getStackInSlot(i);
                old = ItemUtils.copyStackWithSize(old, old.getCount());
                newInv.setStackInSlot(i, old);
            }
            return newInv;
        }
        return this;
    }

    @Nonnull
    public CompoundTag serialize() {
        return this.serializeNBT();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        int insertable = this.stackSizeLimiter.apply(slot, stack);
        int leftOver = stack.getCount() - insertable;
        ItemStack toInsert = ItemUtils.copyStackWithSize(stack, insertable);
        ItemStack notInserted = super.insertItem(slot, toInsert, simulate);
        return ItemUtils.copyStackWithSize(toInsert, leftOver + notInserted.getCount());
    }

    private boolean hasHandlerForSide(@Nullable Direction facing) {
        return facing == null || applicableSides.contains(facing);
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return hasHandlerForSide(facing) && ForgeCapabilities.ITEM_HANDLER == capability;
    }

    @Nonnull
    public LazyOptional<TileInventory> getCapability() {
        return LazyOptional.of(() -> this);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        if (changeListener != null) {
            changeListener.accept(slot);
        }
        tile.markForUpdate();
    }

    public void clearInventory() {
        for (int i = 0; i < getSlots(); i++) {
            setStackInSlot(i, ItemStack.EMPTY);
            onContentsChanged(i);
        }
    }

    @Override
    @Nonnull
    public Iterator<ItemStack> iterator() {
        return this.stacks.iterator();
    }
}
