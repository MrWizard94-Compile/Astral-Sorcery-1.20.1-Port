package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores constellation-based enchantments and applies them to equipped items
 * while worn as a curio/offhand item, via {@link hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler}.
 */
public class ItemEnchantmentAmulet extends ItemAS {

    private static final String KEY_ENCHANTMENTS = "amulet_enchantments";

    public ItemEnchantmentAmulet() {
        super(defaultProperties().stacksTo(1));
    }

    @Nonnull
    public static List<AmuletEnchantment> getAmuletEnchantments(@Nonnull ItemStack stack) {
        List<AmuletEnchantment> result = new ArrayList<>();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return result;
        }
        CompoundTag data = NBTHelper.getPersistentData(stack);
        if (!data.contains(KEY_ENCHANTMENTS, Tag.TAG_LIST)) {
            return result;
        }
        ListTag list = data.getList(KEY_ENCHANTMENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            AmuletEnchantment ench = AmuletEnchantment.deserialize(list.getCompound(i));
            if (ench != null) {
                result.add(ench);
            }
        }
        return result;
    }

    public static void setAmuletEnchantments(@Nonnull ItemStack stack, @Nonnull List<AmuletEnchantment> enchantments) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return;
        }
        CompoundTag data = NBTHelper.getPersistentData(stack);
        ListTag list = new ListTag();
        for (AmuletEnchantment ench : enchantments) {
            list.add(ench.serialize());
        }
        data.put(KEY_ENCHANTMENTS, list);
    }
}
