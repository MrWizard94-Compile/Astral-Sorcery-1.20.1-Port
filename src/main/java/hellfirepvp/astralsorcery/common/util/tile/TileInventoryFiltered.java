package hellfirepvp.astralsorcery.common.util.tile;

import hellfirepvp.astralsorcery.common.tile.base.BlockEntitySynchronized;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Extension of {@link TileInventory} with pluggable insert/extract filters.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntitySynchronized -> BlockEntitySynchronized,
 * CompoundNBT -> CompoundTag</p>
 */
public class TileInventoryFiltered extends TileInventory {

    @Nullable
    private InputFilter inputFilter = null;
    @Nullable
    private ExtractFilter extractFilter = null;

    public TileInventoryFiltered(@Nonnull BlockEntitySynchronized tile,
                                 @Nonnull Supplier<Integer> slotCountProvider,
                                 @Nonnull Direction... applicableSides) {
        super(tile, slotCountProvider, applicableSides);
    }

    public TileInventoryFiltered(@Nonnull BlockEntitySynchronized tile,
                                 @Nonnull Supplier<Integer> slotCountProvider,
                                 @Nullable Consumer<Integer> changeListener,
                                 @Nonnull Direction... applicableSides) {
        super(tile, slotCountProvider, changeListener, applicableSides);
    }

    protected TileInventoryFiltered(@Nonnull BlockEntitySynchronized tile,
                                    @Nonnull Supplier<Integer> slotCountProvider,
                                    @Nullable Consumer<Integer> changeListener,
                                    @Nonnull Collection<Direction> applicableSides,
                                    @Nonnull BiFunction<Integer, ItemStack, Integer> stackSizeLimiter) {
        super(tile, slotCountProvider, changeListener, applicableSides, stackSizeLimiter);
    }

    @Nonnull
    public TileInventoryFiltered canInsert(@Nullable InputFilter filter) {
        this.inputFilter = filter;
        return this;
    }

    @Nonnull
    public TileInventoryFiltered canExtract(@Nullable ExtractFilter filter) {
        this.extractFilter = filter;
        return this;
    }

    @Override
    @Nonnull
    protected TileInventoryFiltered makeNewInstance() {
        TileInventoryFiltered inv = new TileInventoryFiltered(
                this.tile, this.slotCountProvider,
                this.changeListener, MiscUtils.copySet(this.applicableSides),
                this.stackSizeLimiter);
        inv.canInsert(this.inputFilter);
        inv.canExtract(this.extractFilter);
        return inv;
    }

    @Nonnull
    @Override
    public TileInventoryFiltered deserialize(@Nonnull CompoundTag tag) {
        return (TileInventoryFiltered) super.deserialize(tag);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!canInsertItem(slot, stack, getStackInSlot(slot))) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExtractItem(slot, amount, getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    public boolean canInsertItem(int slot, @Nonnull ItemStack toAdd) {
        return this.canInsertItem(slot, toAdd, this.getStackInSlot(slot));
    }

    private boolean canInsertItem(int slot, @Nonnull ItemStack toAdd, @Nonnull ItemStack existing) {
        return inputFilter == null || inputFilter.canInsert(slot, toAdd, existing);
    }

    public boolean canExtractItem(int slot, int amount) {
        return this.canExtractItem(slot, amount, this.getStackInSlot(slot));
    }

    private boolean canExtractItem(int slot, int amount, @Nonnull ItemStack existing) {
        return extractFilter == null || extractFilter.canExtract(slot, amount, existing);
    }

    public interface InputFilter {

        boolean canInsert(int slot, @Nonnull ItemStack toAdd, @Nonnull ItemStack existing);
    }

    public interface ExtractFilter {

        boolean canExtract(int slot, int amount, @Nonnull ItemStack existing);
    }
}
