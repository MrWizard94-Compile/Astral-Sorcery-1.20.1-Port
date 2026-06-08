package hellfirepvp.astralsorcery.common.item.base;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Interface for items that can store block states (used by Architect and Exchange wands).
 * States are persisted in the item's persistent NBT under "storedStates".
 *
 * <p>1.16 → 1.20: World → Level, TileEntity → BlockEntity, CompoundNBT → CompoundTag,
 * ListNBT → ListTag, Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND,
 * state.isAir(world, pos) → state.isAir(), state.getBlockHardness → state.getDestroySpeed</p>
 */
public interface ItemBlockStorage {

    static boolean storeBlockState(ItemStack stack, Level level, net.minecraft.core.BlockPos pos) {
        if (level.getBlockEntity(pos) != null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0 || ItemUtils.createBlockStack(state).isEmpty()) {
            return false;
        }
        CompoundTag persistent = NBTHelper.getPersistentData(stack);
        ListTag stored = persistent.getList("storedStates", Tag.TAG_COMPOUND);
        stored.add(NBTHelper.getBlockStateNBTTag(state));
        persistent.put("storedStates", stored);
        return true;
    }

    static void clearContainerFor(Player player) {
        Tuple<InteractionHand, ItemStack> held = MiscUtils.getMainOrOffHand(player,
                s -> s.getItem() instanceof ItemBlockStorage);
        if (held != null) {
            NBTHelper.getPersistentData(held.getB()).remove("storedStates");
        }
    }

    @Nonnull
    static List<Tuple<ItemStack, Integer>> getInventoryMatchingItemStacks(Player player, ItemStack referenceContainer) {
        Map<BlockState, Tuple<ItemStack, Integer>> storedStates = getInventoryMatching(player, referenceContainer);
        List<Tuple<ItemStack, Integer>> foundStacks = new ArrayList<>(storedStates.values());
        foundStacks.sort(Comparator.comparing(tpl -> tpl.getA().getItem().getDescriptionId()));
        return foundStacks;
    }

    @Nonnull
    static Map<BlockState, Tuple<ItemStack, Integer>> getInventoryMatching(Player player, ItemStack referenceContainer) {
        Map<BlockState, ItemStack> mappedStacks = getMappedStoredStates(referenceContainer);
        Map<BlockState, Tuple<ItemStack, Integer>> foundContents = new HashMap<>();
        for (Map.Entry<BlockState, ItemStack> e : mappedStacks.entrySet()) {
            BlockState state = e.getKey();
            ItemStack stored = e.getValue();
            int countDisplay = 0;
            Collection<ItemStack> stacks = ItemUtils.findItemsInPlayerInventory(player, stored, true);
            for (ItemStack found : stacks) {
                countDisplay += found.getCount();
            }
            foundContents.put(state, new Tuple<>(stored.copy(), countDisplay));
        }
        return foundContents;
    }

    @Nonnull
    static Map<BlockState, ItemStack> getMappedStoredStates(ItemStack referenceContainer) {
        List<BlockState> blockStates = getStoredStates(referenceContainer);
        Map<BlockState, ItemStack> map = new LinkedHashMap<>();
        for (BlockState state : blockStates) {
            ItemStack stack = ItemUtils.createBlockStack(state);
            if (!stack.isEmpty()) {
                map.put(state, stack);
            }
        }
        return map;
    }

    @Nonnull
    static NonNullList<BlockState> getStoredStates(ItemStack referenceContainer) {
        NonNullList<BlockState> states = NonNullList.create();
        if (!referenceContainer.isEmpty() && referenceContainer.getItem() instanceof ItemBlockStorage) {
            CompoundTag persistent = NBTHelper.getPersistentData(referenceContainer);
            ListTag stored = persistent.getList("storedStates", Tag.TAG_COMPOUND);
            for (int i = 0; i < stored.size(); i++) {
                BlockState state = NBTHelper.getBlockStateFromTag(stored.getCompound(i));
                if (state != null) {
                    states.add(state);
                }
            }
        }
        return states;
    }

    static Random getPreviewRandomFromWorld(Level level) {
        long tempSeed = 0x6834F10A91B03F15L;
        tempSeed *= (level.getGameTime() / 40) << 8;
        return new Random(tempSeed);
    }
}
