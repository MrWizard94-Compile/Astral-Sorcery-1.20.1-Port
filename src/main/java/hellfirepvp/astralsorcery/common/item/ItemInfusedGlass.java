package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.constellation.engraving.EngravedStarMap;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemInfusedGlass extends ItemAS {

    public ItemInfusedGlass() {
        super(defaultProperties().durability(5).stacksTo(1));
    }

    @Nullable
    public static EngravedStarMap getEngraving(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemInfusedGlass)) {
            return null;
        }
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (tag.contains("starmap", Tag.TAG_COMPOUND)) {
            return EngravedStarMap.deserialize(tag.getCompound("starmap"));
        }
        return null;
    }

    public static void setEngraving(@Nonnull ItemStack stack, @Nullable EngravedStarMap map) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemInfusedGlass)) {
            return;
        }
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (map == null) {
            tag.remove("starmap");
        } else {
            tag.put("starmap", map.serialize());
        }
    }
}
