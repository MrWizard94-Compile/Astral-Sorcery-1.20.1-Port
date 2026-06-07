package hellfirepvp.astralsorcery.common.item.quality;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemDazzlingFrame extends ItemAS {

    public ItemDazzlingFrame() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        getQuality(stack).ifPresent(q -> tooltip.add(q.getDisplayName()));
    }

    public static boolean setQuality(ItemStack stack, GemQuality quality) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDazzlingFrame)) return false;
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        tag.putInt("quality", quality.ordinal());
        return true;
    }

    public static Optional<GemQuality> getQuality(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDazzlingFrame)) return Optional.empty();
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (!tag.contains("quality", Tag.TAG_INT)) return Optional.empty();
        int id = tag.getInt("quality");
        if (id < 0 || id >= GemQuality.values().length) return Optional.empty();
        return Optional.of(GemQuality.values()[id]);
    }
}
