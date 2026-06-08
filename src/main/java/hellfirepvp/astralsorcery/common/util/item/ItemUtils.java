package hellfirepvp.astralsorcery.common.util.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static hellfirepvp.astralsorcery.common.util.item.ItemComparator.Clause.CAPABILITIES_COMPATIBLE;
import static hellfirepvp.astralsorcery.common.util.item.ItemComparator.Clause.ITEM;
import static hellfirepvp.astralsorcery.common.util.item.ItemComparator.Clause.NBT_STRICT;

/**
 * Item/inventory manipulation utilities.
 * Handles item dropping, inventory scanning, consumption, and conversion.
 *
 * <p>1.16 → 1.20 changes:
 * World → Level, PlayerEntity → Player, Vector3d → Vec3,
 * isRemote → isClientSide, addEntity → addFreshEntity,
 * setMotion → setDeltaMovement, getEntityWorld → level(),
 * getPosX/Y/Z → getX/Y/Z, onCollideWithPlayer → playerTouch,
 * EquipmentSlotType → EquipmentSlot, Group.ARMOR → Type.ARMOR,
 * hasContainerItem → hasCraftingRemainingItem,
 * getContainerItem → getCraftingRemainingItem,
 * stack.write → stack.save, ItemStack.read → ItemStack.of,
 * item.getRegistryName → ForgeRegistries.ITEMS.getKey,
 * ITag → TagKey, ItemTags.getCollection → ForgeRegistries.ITEMS.tags()</p>
 */
public class ItemUtils {

    public static final IItemHandler EMPTY_INVENTORY = new ItemHandlerEmpty();
    private static final Random rand = new Random();

    @Nullable
    public static ItemEntity dropItem(@Nonnull Level level, double x, double y, double z,
                                      @Nonnull ItemStack stack) {
        if (level.isClientSide()) {
            return null;
        }
        ItemEntity ei = new ItemEntity(level, x, y, z, stack);
        ei.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(ei);
        ei.setPickUpDelay(20);
        return ei;
    }

    @Nullable
    public static ItemEntity dropItemNaturally(@Nonnull Level level, double x, double y,
                                               double z, @Nonnull ItemStack stack) {
        if (level.isClientSide()) {
            return null;
        }
        ItemEntity ei = new ItemEntity(level, x, y, z, stack);
        applyRandomDropOffset(ei);
        level.addFreshEntity(ei);
        ei.setPickUpDelay(20);
        return ei;
    }

    public static void decrementItem(@Nonnull Supplier<ItemStack> getFromInventory,
                                     @Nonnull Consumer<ItemStack> setIntoInventory,
                                     @Nonnull Consumer<ItemStack> handleExcess) {
        ItemStack toConsume = getFromInventory.get();
        toConsume = copyStackWithSize(toConsume, toConsume.getCount());

        ItemStack toReplaceWith = ItemStack.EMPTY;
        if (toConsume.hasCraftingRemainingItem()) {
            toReplaceWith = toConsume.getCraftingRemainingItem();
        }

        toConsume.shrink(1);

        if (!toReplaceWith.isEmpty()) {
            if (toConsume.isEmpty()) {
                setIntoInventory.accept(toReplaceWith);
            } else if (ItemComparator.compare(toConsume, toReplaceWith,
                    ItemComparator.Clause.Sets.ITEMSTACK_STRICT_NOAMOUNT)) {
                toReplaceWith.grow(toConsume.getCount());
                if (toReplaceWith.getCount() > toReplaceWith.getMaxStackSize()) {
                    int overcapped = toReplaceWith.getCount() - toReplaceWith.getMaxStackSize();
                    setIntoInventory.accept(
                            copyStackWithSize(toReplaceWith, toReplaceWith.getMaxStackSize()));
                    handleExcess.accept(copyStackWithSize(toReplaceWith, overcapped));
                } else {
                    setIntoInventory.accept(toReplaceWith);
                }
            } else {
                handleExcess.accept(toReplaceWith);
            }
        } else {
            setIntoInventory.accept(toConsume);
        }
    }

    public static boolean isEquippableArmor(@Nonnull Entity entity, @Nonnull ItemStack stack) {
        for (EquipmentSlot type : EquipmentSlot.values()) {
            if (type.getType() == EquipmentSlot.Type.ARMOR) {
                if (stack.canEquip(type, entity)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    public static ItemStack dropItemToPlayer(@Nonnull Player player, @Nonnull ItemStack stack) {
        Level level = player.level();
        if (level.isClientSide() || stack.isEmpty()) {
            return stack;
        }
        ItemEntity item = new ItemEntity(level,
                player.getX(), player.getY(), player.getZ(), stack);
        if (item.getItem().isEmpty()) {
            return stack;
        }
        item.setNoPickUpDelay();
        try {
            item.playerTouch(player);
        } catch (Exception ignored) {
            // Some mod could run into an issue here
        }
        if (item.isAlive()) {
            return item.getItem().copy();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private static void applyRandomDropOffset(@Nonnull ItemEntity item) {
        item.setDeltaMovement(
                rand.nextFloat() * 0.3F - 0.15D,
                rand.nextFloat() * 0.3F - 0.05D,
                rand.nextFloat() * 0.3F - 0.15D);
    }

    @Nonnull
    public static ItemStack changeItem(@Nonnull ItemStack stack, @Nonnull Item item) {
        CompoundTag nbt = stack.save(new CompoundTag());
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        nbt.putString("id", key != null ? key.toString() : "minecraft:air");
        return ItemStack.of(nbt);
    }

    @Nonnull
    public static ItemStack createBlockStack(@Nonnull BlockState state) {
        return new ItemStack(state.getBlock());
    }

    @Nullable
    public static BlockState createBlockState(@Nonnull ItemStack stack) {
        Block b = Block.byItem(stack.getItem());
        if (b == Blocks.AIR) {
            return null;
        }
        return b.defaultBlockState();
    }

    @Nonnull
    public static List<ItemStack> getItemsOfTag(@Nonnull ResourceLocation key) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, key);
        return getItemsOfTag(tag);
    }

    @Nonnull
    public static List<ItemStack> getItemsOfTag(@Nonnull TagKey<Item> itemTag) {
        net.minecraftforge.registries.tags.ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager == null) return List.of();
        return tagManager.getTag(itemTag).stream()
                .map(ItemStack::new)
                .collect(Collectors.toList());
    }

    @Nonnull
    public static Collection<ItemStack> scanInventoryFor(@Nonnull IItemHandler handler,
                                                         @Nonnull Item item) {
        List<ItemStack> out = new LinkedList<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (!s.isEmpty() && s.getItem() == item) {
                out.add(copyStackWithSize(s, s.getCount()));
            }
        }
        return out;
    }

    @Nonnull
    public static Collection<ItemStack> scanInventoryForMatching(
            @Nonnull IItemHandler handler, @Nonnull ItemStack match, boolean strict) {
        return findItemsInInventory(handler, match, strict);
    }

    @Nonnull
    public static Collection<ItemStack> findItemsInPlayerInventory(
            @Nonnull Player player, @Nonnull ItemStack match, boolean strict) {
        IItemHandler handler = player.getCapability(
                ForgeCapabilities.ITEM_HANDLER).orElse(EMPTY_INVENTORY);
        return findItemsInInventory(handler, match, strict);
        // Botania integration deferred — depends on IntegrationBotania (not ported)
    }

    @Nonnull
    public static Collection<ItemStack> findItemsInInventory(
            @Nonnull IItemHandler handler, @Nonnull ItemStack match, boolean strict) {
        List<ItemStack> stacksOut = new LinkedList<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (strict
                    ? ItemComparator.compare(s, match, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE)
                    : ItemComparator.compare(s, match, ITEM)) {
                stacksOut.add(copyStackWithSize(s, s.getCount()));
            }
        }
        return stacksOut;
    }

    @Nonnull
    public static Map<Integer, ItemStack> findItemsIndexedInPlayerInventory(
            @Nonnull Player player, @Nonnull Predicate<ItemStack> match) {
        return findItemsIndexedInInventory(
                player.getCapability(ForgeCapabilities.ITEM_HANDLER)
                        .orElse(EMPTY_INVENTORY), match);
    }

    @Nonnull
    public static Map<Integer, ItemStack> findItemsIndexedInInventory(
            @Nonnull IItemHandler handler, @Nonnull ItemStack match, boolean strict) {
        return findItemsIndexedInInventory(handler,
                s -> strict
                        ? ItemComparator.compare(s, match, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE)
                        : ItemComparator.compare(s, match, ITEM));
    }

    @Nonnull
    public static Map<Integer, ItemStack> findItemsIndexedInInventory(
            @Nonnull IItemHandler handler, @Nonnull Predicate<ItemStack> match) {
        Map<Integer, ItemStack> stacksOut = new HashMap<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (match.test(s)) {
                stacksOut.put(j, copyStackWithSize(s, s.getCount()));
            }
        }
        return stacksOut;
    }

    public static boolean consumeFromPlayerInventory(
            @Nonnull Player player,
            @Nonnull ItemStack requestingItemStack,
            @Nonnull ItemStack toConsume,
            boolean simulate) {
        ItemStack tryConsume = copyStackWithSize(toConsume, toConsume.getCount());
        if (tryConsume.isEmpty()) {
            return true;
        }

        IItemHandlerModifiable handler = (IItemHandlerModifiable) player.getCapability(
                ForgeCapabilities.ITEM_HANDLER, null).orElse(EMPTY_INVENTORY);
        return consumeFromInventory(handler, tryConsume, simulate);
        // Botania integration deferred — depends on IntegrationBotania (not ported)
    }

    public static boolean tryConsumeFromInventory(@Nonnull IItemHandler handler,
                                                  @Nonnull ItemStack toConsume,
                                                  boolean simulate) {
        return handler instanceof IItemHandlerModifiable modifiable
                && consumeFromInventory(modifiable, toConsume, simulate);
    }

    public static boolean consumeFromInventory(@Nonnull IItemHandlerModifiable handler,
                                               @Nonnull ItemStack toConsume,
                                               boolean simulate) {
        Map<Integer, ItemStack> contents = findItemsIndexedInInventory(handler, toConsume, false);
        if (contents.isEmpty()) return false;

        int cAmt = toConsume.getCount();
        for (Map.Entry<Integer, ItemStack> slotEntry : contents.entrySet()) {
            int slot = slotEntry.getKey();
            ItemStack inSlot = slotEntry.getValue();
            int toRemove = Math.min(cAmt, inSlot.getCount());
            cAmt -= toRemove;
            if (!simulate) {
                handler.setStackInSlot(slot,
                        copyStackWithSize(inSlot, inSlot.getCount() - toRemove));
            }
            if (cAmt <= 0) {
                break;
            }
        }
        return cAmt <= 0;
    }

    public static void dropInventory(@Nonnull IItemHandler handle, @Nonnull Level level,
                                     @Nonnull BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        for (int i = 0; i < handle.getSlots(); i++) {
            ItemStack stack = handle.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 0.5,
                    pos.getZ() + 0.5, stack);
        }
    }

    public static void decrStackInInventory(@Nonnull ItemStackHandler handler, int slot) {
        if (slot < 0 || slot >= handler.getSlots()) {
            return;
        }
        ItemStack st = handler.getStackInSlot(slot);
        if (st.isEmpty()) {
            return;
        }
        st.setCount(st.getCount() - 1);
        if (st.getCount() <= 0) {
            handler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public static boolean tryPlaceItemInInventory(@Nonnull ItemStack stack,
                                                  @Nonnull IItemHandler handler) {
        return tryPlaceItemInInventory(stack, handler, 0, handler.getSlots());
    }

    public static boolean tryPlaceItemInInventory(@Nonnull ItemStack stack,
                                                  @Nonnull IItemHandler handler,
                                                  int start, int end) {
        ItemStack toAdd = stack.copy();
        if (!hasInventorySpace(toAdd, handler, start, end)) {
            return false;
        }
        int max = stack.getMaxStackSize();

        for (int i = start; i < end; i++) {
            ItemStack in = handler.getStackInSlot(i);
            if (in.isEmpty()) {
                int added = Math.min(stack.getCount(), max);
                stack.setCount(stack.getCount() - added);
                handler.insertItem(i, copyStackWithSize(stack, added), false);
                return true;
            } else {
                if (ItemComparator.compare(stack, in, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE)) {
                    int space = max - in.getCount();
                    int added = Math.min(stack.getCount(), space);
                    stack.setCount(stack.getCount() - added);
                    handler.getStackInSlot(i).setCount(
                            handler.getStackInSlot(i).getCount() + added);
                    if (stack.getCount() <= 0) {
                        return true;
                    }
                }
            }
        }
        return stack.getCount() == 0;
    }

    public static boolean hasInventorySpace(@Nonnull ItemStack stack,
                                            @Nonnull IItemHandler handler,
                                            int rangeMin, int rangeMax) {
        int size = stack.getCount();
        int max = stack.getMaxStackSize();
        for (int i = rangeMin; i < rangeMax && size > 0; i++) {
            ItemStack in = handler.getStackInSlot(i);
            if (in.isEmpty()) {
                size -= max;
            } else {
                if (ItemComparator.compare(stack, in, ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE)) {
                    int space = max - in.getCount();
                    size -= space;
                }
            }
        }
        return size <= 0;
    }

    @Nonnull
    public static ItemStack copyStackWithSize(@Nonnull ItemStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack s = stack.copy();
        s.setCount(amount);
        return s;
    }

    private static class ItemHandlerEmpty implements IItemHandlerModifiable {

        @Override
        public int getSlots() {
            return 0;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            // No-op
        }
    }
}
