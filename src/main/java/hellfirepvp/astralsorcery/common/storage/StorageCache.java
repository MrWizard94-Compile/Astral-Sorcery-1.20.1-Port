/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Per-network item storage backed by a map of {@link StorageKey} → list of
 * {@link StoredItemStack}. Multiple stored stacks per key are possible when
 * NBT differs between entries.
 *
 * <p>1.16 → 1.20: CompoundNBT/ListNBT → CompoundTag/ListTag,
 * Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND.</p>
 */
public class StorageCache {

    private final Map<StorageKey, List<StoredItemStack>> content = Maps.newHashMap();

    public int getTotalItemCount() {
        int i = 0;
        for (List<StoredItemStack> stacks : content.values()) {
            for (StoredItemStack stack : stacks) {
                i += stack.getAmount();
            }
        }
        return i;
    }

    public int getItemCount(@Nonnull StorageKey key) {
        int i = 0;
        for (StoredItemStack s : content.getOrDefault(key, Lists.newArrayList())) {
            i += s.getAmount();
        }
        return i;
    }

    public boolean add(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        StorageKey key = StorageKey.from(stack);
        for (StoredItemStack s : content.computeIfAbsent(key, k -> Lists.newArrayList())) {
            if (s.combineIntoThis(stack)) {
                return true;
            }
        }
        content.get(key).add(new StoredItemStack(stack));
        return true;
    }

    void mergeIntoThis(@Nonnull StorageCache otherStorage) {
        for (StorageKey otherKey : otherStorage.content.keySet()) {
            List<StoredItemStack> thisStorage =
                    this.content.computeIfAbsent(otherKey, k -> Lists.newArrayList());
            List<StoredItemStack> notMerged = Lists.newArrayList();

            outer:
            for (StoredItemStack toMerge : otherStorage.content.get(otherKey)) {
                for (StoredItemStack thisItem : thisStorage) {
                    if (thisItem.combineIntoThis(toMerge)) {
                        continue outer;
                    }
                }
                notMerged.add(toMerge);
            }
            thisStorage.addAll(notMerged);
        }
    }

    /**
     * Attempts to transfer one item of the given key into a specific slot of an
     * inventory. Returns true if anything was transferred.
     */
    public boolean attemptTransferInto(@Nonnull StorageKey key,
                                       @Nonnull IItemHandler inv,
                                       int slot,
                                       boolean simulate) {
        if (content.isEmpty()) return false;

        List<StoredItemStack> stacks = this.content.get(key);
        if (stacks == null || stacks.isEmpty()) {
            return false;
        }

        for (StoredItemStack stack : stacks) {
            ItemStack sample = stack.getTemplateStack();
            int amountToRemove = sample.getCount();

            ItemStack notInserted = inv.insertItem(slot, sample, simulate);
            int addedCount = amountToRemove - notInserted.getCount();

            if (addedCount > 0) {
                if (!simulate) {
                    if (!stack.removeAmount(addedCount)) {
                        return false;
                    }
                    if (stack.isEmpty()) {
                        stacks.remove(stack);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to transfer items of the given key into all slots of an inventory.
     * Returns true if anything was transferred.
     */
    public boolean attemptTransferInto(@Nonnull StorageKey key,
                                       @Nonnull IItemHandler inv,
                                       boolean simulate) {
        if (content.isEmpty()) return false;

        List<StoredItemStack> stacks = this.content.get(key);
        if (stacks == null || stacks.isEmpty()) {
            return false;
        }

        boolean change = false;
        outer:
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            for (StoredItemStack stack : stacks) {
                ItemStack sample = stack.getTemplateStack();
                int amountToRemove = sample.getCount();

                ItemStack notInserted = inv.insertItem(slot, sample, simulate);
                int addedCount = amountToRemove - notInserted.getCount();

                if (addedCount > 0) {
                    change = true;
                }

                if (!simulate) {
                    if (!stack.removeAmount(addedCount)) {
                        return false;
                    }
                    if (stack.isEmpty()) {
                        stacks.remove(stack);
                        continue outer;
                    }
                }
            }
        }
        return change;
    }

    public void writeToNBT(@Nonnull CompoundTag tag) {
        ListTag contentList = new ListTag();

        for (Map.Entry<StorageKey, List<StoredItemStack>> entry : this.content.entrySet()) {
            CompoundTag itemStorage = new CompoundTag();
            itemStorage.put("storageKey", entry.getKey().serialize());

            ListTag items = new ListTag();
            for (StoredItemStack stack : entry.getValue()) {
                items.add(stack.serialize());
            }
            itemStorage.put("items", items);
            contentList.add(itemStorage);
        }

        tag.put("content", contentList);
    }

    public void readFromNBT(@Nonnull CompoundTag tag) {
        this.content.clear();

        ListTag contentList = tag.getList("content", Tag.TAG_COMPOUND);
        for (int i = 0; i < contentList.size(); i++) {
            CompoundTag itemStorage = contentList.getCompound(i);
            StorageKey key = StorageKey.deserialize(itemStorage.getCompound("storageKey"));
            if (key == null) {
                continue;
            }

            ListTag items = itemStorage.getList("items", Tag.TAG_COMPOUND);
            List<StoredItemStack> stacks = new ArrayList<>(items.size());
            for (int j = 0; j < items.size(); j++) {
                StoredItemStack stored = StoredItemStack.deserialize(items.getCompound(j));
                if (stored != null) {
                    stacks.add(stored);
                }
            }
            this.content.put(key, stacks);
        }
    }
}
