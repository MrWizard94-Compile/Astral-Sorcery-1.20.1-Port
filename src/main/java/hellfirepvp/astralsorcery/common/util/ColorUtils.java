package hellfirepvp.astralsorcery.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * Color blending, overlay, and dye-color mapping utilities.
 *
 * <p>1.16 → 1.20 changes:
 * MathHelper → Mth, TextFormatting → ChatFormatting,
 * TranslationTextComponent → Component.translatable(),
 * IFormattableTextComponent → MutableComponent,
 * DyeColor.getTranslationKey() → getSerializedName(),
 * FluidAttributes.getColor() → IClientFluidTypeExtensions.getTintColor()</p>
 */
public class ColorUtils {

    @Nonnull
    public static Color blendColors(@Nonnull Color color1, @Nonnull Color color2, float color1Ratio) {
        return new Color(blendColors(color1.getRGB(), color2.getRGB(), color1Ratio), true);
    }

    public static int blendColors(int color1, int color2, float color1Ratio) {
        float ratio1 = Mth.clamp(color1Ratio, 0F, 1F);
        float ratio2 = 1F - ratio1;

        int a1 = (color1 & 0xFF000000) >> 24;
        int r1 = (color1 & 0x00FF0000) >> 16;
        int g1 = (color1 & 0x0000FF00) >>  8;
        int b1 = (color1 & 0x000000FF);

        int a2 = (color2 & 0xFF000000) >> 24;
        int r2 = (color2 & 0x00FF0000) >> 16;
        int g2 = (color2 & 0x0000FF00) >>  8;
        int b2 = (color2 & 0x000000FF);

        int a = Mth.clamp(Math.round(a1 * ratio1 + a2 * ratio2), 0, 255);
        int r = Mth.clamp(Math.round(r1 * ratio1 + r2 * ratio2), 0, 255);
        int g = Mth.clamp(Math.round(g1 * ratio1 + g2 * ratio2), 0, 255);
        int b = Mth.clamp(Math.round(b1 * ratio1 + b2 * ratio2), 0, 255);

        return a << 24 | r << 16 | g << 8 | b;
    }

    @Nonnull
    public static Color overlayColor(@Nonnull Color base, @Nonnull Color overlay) {
        return new Color(overlayColor(base.getRGB(), overlay.getRGB()), true);
    }

    public static int overlayColor(int base, int overlay) {
        int alpha = (base & 0xFF000000) >> 24;

        int baseR = (base & 0x00FF0000) >> 16;
        int baseG = (base & 0x0000FF00) >>  8;
        int baseB = (base & 0x000000FF);

        int overlayR = (overlay & 0x00FF0000) >> 16;
        int overlayG = (overlay & 0x0000FF00) >>  8;
        int overlayB = (overlay & 0x000000FF);

        int r = Math.round(baseR * (overlayR / 255F)) & 0xFF;
        int g = Math.round(baseG * (overlayG / 255F)) & 0xFF;
        int b = Math.round(baseB * (overlayB / 255F)) & 0xFF;

        return alpha << 24 | r << 16 | g << 8 | b;
    }

    /**
     * Get the tint color for a fluid stack.
     * In 1.20, FluidAttributes is replaced by IClientFluidTypeExtensions.
     */
    public static int getOverlayColor(@Nonnull FluidStack stack) {
        if (stack.isEmpty()) {
            return 0xFFFFFFFF;
        }
        return IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
    }

    /**
     * Get the tint color for an item stack (block or item color handler).
     */
    @OnlyIn(Dist.CLIENT)
    public static int getOverlayColor(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0xFFFFFFFF;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            return Minecraft.getInstance().getBlockColors().getColor(state, null, null, 0);
        } else {
            return Minecraft.getInstance().getItemColors().getColor(stack, 0);
        }
    }

    @Nonnull
    public static MutableComponent getTranslation(@Nonnull DyeColor color) {
        return Component.translatable(
                String.format("color.minecraft.%s", color.getSerializedName()));
    }

    @Nonnull
    public static Color flareColorFromDye(@Nonnull DyeColor color) {
        return new Color(color.getFireworkColor());
    }

    @Nonnull
    public static ChatFormatting textFormattingForDye(@Nonnull DyeColor color) {
        return switch (color) {
            case WHITE -> ChatFormatting.WHITE;
            case ORANGE -> ChatFormatting.GOLD;
            case MAGENTA -> ChatFormatting.DARK_PURPLE;
            case LIGHT_BLUE -> ChatFormatting.DARK_AQUA;
            case YELLOW -> ChatFormatting.YELLOW;
            case LIME -> ChatFormatting.GREEN;
            case PINK -> ChatFormatting.LIGHT_PURPLE;
            case GRAY -> ChatFormatting.DARK_GRAY;
            case LIGHT_GRAY -> ChatFormatting.GRAY;
            case CYAN -> ChatFormatting.BLUE;
            case PURPLE -> ChatFormatting.DARK_PURPLE;
            case BLUE -> ChatFormatting.DARK_BLUE;
            case BROWN -> ChatFormatting.GOLD;
            case GREEN -> ChatFormatting.DARK_GREEN;
            case RED -> ChatFormatting.DARK_RED;
            case BLACK -> ChatFormatting.DARK_GRAY;
        };
    }
}
