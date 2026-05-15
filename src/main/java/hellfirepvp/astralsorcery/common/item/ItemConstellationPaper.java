package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Constellation Paper — stores a single constellation reference.
 * Players discover constellations and receive these papers as records.
 * Has a glint effect when a constellation is set.
 *
 * <p>1.16 -> 1.20 changes:
 * addInformation -> appendHoverText,
 * ITextComponent -> Component,
 * hasEffect -> isFoil,
 * CompoundNBT -> CompoundTag,
 * ResourceLocation unchanged.</p>
 */
public class ItemConstellationPaper extends ItemAS {

    private static final String TAG_CONSTELLATION = "constellation";

    public ItemConstellationPaper() {
        super(defaultProperties().stacksTo(1));
    }

    /**
     * Set the constellation stored on this paper.
     *
     * @param stack         the item stack
     * @param constellation the constellation's registry name
     */
    public static void setConstellation(@Nonnull ItemStack stack, @Nonnull ResourceLocation constellation) {
        stack.getOrCreateTag().putString(TAG_CONSTELLATION, constellation.toString());
    }

    /**
     * Get the constellation stored on this paper, if any.
     *
     * @param stack the item stack
     * @return the constellation's registry name, or null if none is set
     */
    @Nullable
    public static ResourceLocation getConstellation(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CONSTELLATION)) {
            return null;
        }
        String value = tag.getString(TAG_CONSTELLATION);
        return value.isEmpty() ? null : new ResourceLocation(value);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nullable Level level,
                                @Nonnull List<Component> tooltip,
                                @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ResourceLocation constellation = getConstellation(stack);
        if (constellation != null) {
            tooltip.add(Component.translatable(
                    "astralsorcery.tooltip.constellation_paper.constellation",
                    constellation.getPath()));
        }
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return getConstellation(stack) != null || super.isFoil(stack);
    }
}
