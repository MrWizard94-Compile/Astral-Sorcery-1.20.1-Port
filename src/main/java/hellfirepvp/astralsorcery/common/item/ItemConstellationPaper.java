package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level,
                                                  @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            ResourceLocation key = getConstellation(stack);
            if (key != null) {
                IConstellation cst = ConstellationRegistry.getConstellation(key);
                if (cst != null) {
                    openScreenClient(cst);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreenClient(@Nonnull IConstellation cst) {
        hellfirepvp.astralsorcery.client.screen.ClientScreenHandler.openConstellationPaperScreen(cst);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return getConstellation(stack) != null || super.isFoil(stack);
    }
}
