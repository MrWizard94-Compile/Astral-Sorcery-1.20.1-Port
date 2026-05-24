package hellfirepvp.astralsorcery.common.enchantment.dynamic;

import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantmentHelper;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.QuickChargeEnchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class DynamicEnchantmentHelper {

    // Re-entry guard: prevents infinite recursion when canHaveDynamicEnchantment
    // is triggered recursively (e.g. from enchantability/damageable checks).
    private static final ThreadLocal<Boolean> CHECKING = ThreadLocal.withInitial(() -> false);

    private static int getNewEnchantmentLevel(int current, String enchStr, ItemStack item, @Nullable List<DynamicEnchantment> context) {
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchStr));
        if (enchantment != null) {
            current = getNewEnchantmentLevel(current, enchantment, item, context);
            if (enchantment instanceof QuickChargeEnchantment) {
                current = Mth.clamp(current, 0, 5);
            }
        }
        return current;
    }

    public static int getNewEnchantmentLevel(int current, Enchantment enchantment, ItemStack item, @Nullable List<DynamicEnchantment> context) {
        if (!canHaveDynamicEnchantment(item)) {
            return current;
        }
        if (enchantment == null || enchantment.isCurse()) {
            return current;
        }

        List<DynamicEnchantment> modifiers = context != null ? context : fireEnchantmentGatheringEvent(item);
        for (DynamicEnchantment mod : modifiers) {
            Enchantment target = mod.getEnchantment();
            switch (mod.getType()) {
                case ADD_TO_SPECIFIC:
                    if (enchantment.equals(target)) {
                        current += mod.getLevelAddition();
                    }
                    break;
                case ADD_TO_EXISTING_SPECIFIC:
                    if (enchantment.equals(target) && current > 0) {
                        current += mod.getLevelAddition();
                    }
                    break;
                case ADD_TO_EXISTING_ALL:
                    if (current > 0) {
                        current += mod.getLevelAddition();
                    }
                    break;
                default:
                    break;
            }
        }
        if (enchantment instanceof QuickChargeEnchantment) {
            current = Mth.clamp(current, 0, 5);
        }
        return current;
    }

    public static net.minecraft.nbt.ListTag modifyEnchantmentTags(net.minecraft.nbt.ListTag existingEnchantments, ItemStack stack) {
        if (!canHaveDynamicEnchantment(stack)) {
            return existingEnchantments;
        }

        List<DynamicEnchantment> context = fireEnchantmentGatheringEvent(stack);
        if (context.isEmpty()) {
            return existingEnchantments;
        }

        net.minecraft.nbt.ListTag returnNew = new net.minecraft.nbt.ListTag();
        Set<String> enchantments = new HashSet<>(existingEnchantments.size());
        for (int i = 0; i < existingEnchantments.size(); i++) {
            net.minecraft.nbt.CompoundTag cmp = existingEnchantments.getCompound(i);
            String enchKey = cmp.getString("id");
            int lvl = cmp.getInt("lvl");
            int newLvl = getNewEnchantmentLevel(lvl, enchKey, stack, context);

            net.minecraft.nbt.CompoundTag newEnchTag = new net.minecraft.nbt.CompoundTag();
            newEnchTag.putString("id", enchKey);
            newEnchTag.putInt("lvl", newLvl);
            returnNew.add(newEnchTag);
            enchantments.add(enchKey);
        }

        for (DynamicEnchantment mod : context) {
            if (mod.getType() == DynamicEnchantmentType.ADD_TO_SPECIFIC) {
                Enchantment ench = mod.getEnchantment();
                if (ench == null || ench.isCurse()) {
                    continue;
                }
                if (!ench.canEnchant(stack)) {
                    continue;
                }
                ResourceLocation enchKey = ForgeRegistries.ENCHANTMENTS.getKey(ench);
                if (enchKey == null) continue;
                String enchName = enchKey.toString();
                if (!enchantments.contains(enchName)) {
                    net.minecraft.nbt.CompoundTag newEnchTag = new net.minecraft.nbt.CompoundTag();
                    newEnchTag.putString("id", enchName);
                    newEnchTag.putInt("lvl", getNewEnchantmentLevel(0, ench, stack, context));
                    returnNew.add(newEnchTag);
                }
            }
        }
        return returnNew;
    }

    public static Map<Enchantment, Integer> addNewLevels(Map<Enchantment, Integer> enchantmentLevelMap, ItemStack stack) {
        if (!canHaveDynamicEnchantment(stack)) {
            return enchantmentLevelMap;
        }

        List<DynamicEnchantment> context = fireEnchantmentGatheringEvent(stack);
        if (context.isEmpty()) {
            return enchantmentLevelMap;
        }

        Map<Enchantment, Integer> copyRet = Maps.newLinkedHashMap(enchantmentLevelMap);
        enchantmentLevelMap.clear();
        for (Map.Entry<Enchantment, Integer> enchant : copyRet.entrySet()) {
            enchantmentLevelMap.put(enchant.getKey(),
                    getNewEnchantmentLevel(enchant.getValue(), enchant.getKey(), stack, context));
        }

        for (DynamicEnchantment mod : context) {
            if (mod.getType() == DynamicEnchantmentType.ADD_TO_SPECIFIC) {
                Enchantment ench = mod.getEnchantment();
                if (ench == null || ench.isCurse()) {
                    continue;
                }
                if (!enchantmentLevelMap.containsKey(ench)) {
                    enchantmentLevelMap.put(ench, getNewEnchantmentLevel(0, ench, stack, context));
                }
            }
        }
        return enchantmentLevelMap;
    }

    public static boolean canHaveDynamicEnchantment(ItemStack stack) {
        if (CHECKING.get()) {
            // Re-entrant call — we're already evaluating this stack, skip to avoid infinite recursion.
            return false;
        }
        CHECKING.set(true);
        try {
            if (stack.isEmpty()) return false;
            if (stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK) return false;
            try {
                return stack.isEnchantable();
            } catch (NullPointerException ignored) {
                // Some items throw NPE when capabilities are not yet initialized.
                return false;
            }
        } finally {
            CHECKING.set(false);
        }
    }

    private static List<DynamicEnchantment> fireEnchantmentGatheringEvent(ItemStack tool) {
        net.minecraft.world.entity.player.Player foundEntity = AmuletEnchantmentHelper.getPlayerHavingTool(tool);
        if (foundEntity == null) {
            return new ArrayList<>();
        }
        DynamicEnchantmentEvent.Add addEvent = new DynamicEnchantmentEvent.Add(tool, foundEntity);
        if (MinecraftForge.EVENT_BUS.post(addEvent)) {
            return new ArrayList<>();
        }
        DynamicEnchantmentEvent.Modify modifyEvent = new DynamicEnchantmentEvent.Modify(tool, addEvent.getEnchantmentsToApply(), foundEntity);
        if (MinecraftForge.EVENT_BUS.post(modifyEvent)) {
            return new ArrayList<>();
        }
        return modifyEvent.getEnchantmentsToApply();
    }
}
