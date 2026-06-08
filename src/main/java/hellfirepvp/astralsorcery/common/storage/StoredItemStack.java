/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.storage;

import hellfirepvp.astralsorcery.common.util.item.ItemComparator;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An item type together with a stored quantity, used inside {@link StorageCache}.
 * The template stack is always kept at count=1; the logical quantity is tracked
 * separately so counts can exceed vanilla stack limits.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag, ItemStack.read() → ItemStack.of().</p>
 */
public class StoredItemStack {

    private final ItemStack stack;
    private int amount;

    StoredItemStack(@Nonnull ItemStack stack) {
        this(stack, stack.getCount());
    }

    private StoredItemStack(@Nonnull ItemStack stack, int amount) {
        this.stack = ItemUtils.copyStackWithSize(stack, 1);
        this.amount = amount;
    }

    /** Returns a copy of the template stack sized to min(maxStackSize, amount). */
    @Nonnull
    public ItemStack getTemplateStack() {
        return ItemUtils.copyStackWithSize(stack, Math.min(stack.getMaxStackSize(), amount));
    }

    /**
     * Reduces the stored amount by {@code amount}. Returns false (and makes no change)
     * if the operation would bring the count below zero.
     */
    public boolean removeAmount(int amount) {
        if (this.amount - amount < 0) {
            return false;
        }
        this.amount -= amount;
        return true;
    }

    public boolean isEmpty() {
        return this.amount <= 0;
    }

    public int getAmount() {
        return amount;
    }

    /**
     * Merges another {@link StoredItemStack} into this one if they represent the
     * same item type (strict match, no amount). Returns true on success.
     */
    public boolean combineIntoThis(@Nonnull StoredItemStack other) {
        if (ItemComparator.compare(this.stack, other.stack, ItemComparator.Clause.Sets.ITEMSTACK_STRICT_NOAMOUNT)) {
            amount += other.amount;
            return true;
        }
        return false;
    }

    /**
     * Merges a vanilla {@link ItemStack} into this stored stack if item types match.
     * Returns true on success.
     */
    public boolean combineIntoThis(@Nonnull ItemStack other) {
        if (ItemComparator.compare(this.stack, other, ItemComparator.Clause.Sets.ITEMSTACK_STRICT_NOAMOUNT)) {
            amount += other.getCount();
            return true;
        }
        return false;
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("item", stack.save(new CompoundTag()));
        tag.putInt("amount", amount);
        return tag;
    }

    @Nullable
    public static StoredItemStack deserialize(@Nonnull CompoundTag cmp) {
        ItemStack stack = ItemStack.of(cmp.getCompound("item"));
        if (stack.isEmpty()) {
            return null;
        }
        int amount = cmp.getInt("amount");
        return new StoredItemStack(stack, amount);
    }
}
