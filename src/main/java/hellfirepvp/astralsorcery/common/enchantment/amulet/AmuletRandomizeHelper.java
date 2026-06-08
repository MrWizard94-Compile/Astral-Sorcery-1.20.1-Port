package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentType;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class AmuletRandomizeHelper {

    // Cached once on first roll; registry is frozen after mod setup so this is safe.
    @Nullable
    private static List<Enchantment> enchantPool = null;

    // Config values — tunable via server config when ConfigManager is wired up.
    public static double chance2nd         = 0.80;
    public static double chance3rd         = 0.25;
    public static double chance2Level      = 0.15;
    public static double chanceToAll       = 0.02;
    public static double chanceToNonExisting = 0.35;

    public static void rollAmulet(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return;
        }

        List<AmuletEnchantment> ench = new ArrayList<>();
        while (mayGetAdditionalRoll(ench)) {
            DynamicEnchantmentType type = getRollType(ench);
            if (type != null) {
                int lvl = getRollLevel();
                if (type.isEnchantmentSpecific()) {
                    Enchantment e = getRandomEnchant();
                    if (e != null) {
                        ench.add(new AmuletEnchantment(type, e, lvl));
                    }
                } else {
                    ench.add(new AmuletEnchantment(type, lvl));
                }
            }
        }
        ItemEnchantmentAmulet.setAmuletEnchantments(stack, collapseEnchantments(ench));
    }

    @Nullable
    private static Enchantment getRandomEnchant() {
        List<Enchantment> pool = enchantPool;
        if (pool == null) {
            pool = ForgeRegistries.ENCHANTMENTS.getValues().stream()
                    .filter(e -> !e.isCurse())
                    .collect(Collectors.toList());
            enchantPool = pool;
        }
        if (pool.isEmpty()) return null;
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    @Nullable
    private static DynamicEnchantmentType getRollType(List<AmuletEnchantment> existing) {
        int exAll = getAdditionAll(existing);
        switch (existing.size()) {
            case 0:
            case 1:
                if (ThreadLocalRandom.current().nextFloat() < chanceToAll) {
                    return DynamicEnchantmentType.ADD_TO_EXISTING_ALL;
                }
                if (ThreadLocalRandom.current().nextFloat() < chanceToNonExisting) {
                    return DynamicEnchantmentType.ADD_TO_SPECIFIC;
                }
                return DynamicEnchantmentType.ADD_TO_EXISTING_SPECIFIC;
            case 2:
                if (exAll > 1) {
                    return null;
                } else if (exAll == 1) {
                    if (ThreadLocalRandom.current().nextFloat() < chanceToNonExisting) {
                        return DynamicEnchantmentType.ADD_TO_SPECIFIC;
                    }
                    return DynamicEnchantmentType.ADD_TO_EXISTING_SPECIFIC;
                } else {
                    if (ThreadLocalRandom.current().nextFloat() < chanceToAll) {
                        return DynamicEnchantmentType.ADD_TO_EXISTING_ALL;
                    }
                    if (ThreadLocalRandom.current().nextFloat() < chanceToNonExisting) {
                        return DynamicEnchantmentType.ADD_TO_SPECIFIC;
                    }
                    return DynamicEnchantmentType.ADD_TO_EXISTING_SPECIFIC;
                }
            default:
                return null;
        }
    }

    private static int getRollLevel() {
        return ThreadLocalRandom.current().nextFloat() < chance2Level ? 2 : 1;
    }

    private static boolean mayGetAdditionalRoll(List<AmuletEnchantment> existing) {
        if (existing.isEmpty()) return true;
        switch (existing.size()) {
            case 1:
                return ThreadLocalRandom.current().nextFloat() < chance2nd;
            case 2:
                return getAdditionAll(existing) < 2 && ThreadLocalRandom.current().nextFloat() < chance3rd;
            default:
                return false;
        }
    }

    private static int getAdditionAll(List<AmuletEnchantment> ench) {
        int count = 0;
        for (AmuletEnchantment e : ench) {
            if (e.getType() == DynamicEnchantmentType.ADD_TO_EXISTING_ALL) count++;
        }
        return count;
    }

    private static List<AmuletEnchantment> collapseEnchantments(List<AmuletEnchantment> ench) {
        List<AmuletEnchantment> result = new LinkedList<>();
        for (AmuletEnchantment e : ench) {
            boolean found = false;
            for (AmuletEnchantment ex : result) {
                if (ex.canMerge(e)) {
                    ex.merge(e);
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(e);
            }
        }
        return result;
    }
}
