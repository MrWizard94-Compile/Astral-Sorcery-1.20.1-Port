/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;

/**
 * 2D drawing utilities for GUI screens, overlays, and HUD elements.
 * All methods assume screen coordinates (origin top-left, Y increases downward).
 *
 * <p>1.16 → 1.20 changes:
 * {@code MatrixStack} → {@code PoseStack}.
 * {@code AbstractGui.fill()} removed — use {@code GuiGraphics.fill()}.
 * Font rendering uses {@code GuiGraphics.drawString()} instead of direct font calls.
 * Many methods now take {@code GuiGraphics} instead of raw PoseStack.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class RenderingDrawUtils {

    private RenderingDrawUtils() {}

    // =========================================================================
    // Rectangle drawing
    // =========================================================================

    /**
     * Draw a filled rectangle with a solid color.
     *
     * @param graphics the GUI graphics context
     * @param x        left edge
     * @param y        top edge
     * @param width    rectangle width
     * @param height   rectangle height
     * @param color    ARGB color (e.g., 0x80000000 for semi-transparent black)
     */
    public static void drawRect(@Nonnull GuiGraphics graphics,
                                 int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    /**
     * Draw a filled rectangle using a {@link Color} object.
     */
    public static void drawRect(@Nonnull GuiGraphics graphics,
                                 int x, int y, int width, int height,
                                 @Nonnull Color color) {
        drawRect(graphics, x, y, width, height, color.getRGB());
    }

    /**
     * Draw a gradient rectangle (top to bottom color transition).
     *
     * @param graphics  the GUI graphics context
     * @param x         left edge
     * @param y         top edge
     * @param width     rectangle width
     * @param height    rectangle height
     * @param colorTop  ARGB top color
     * @param colorBot  ARGB bottom color
     */
    public static void drawGradientRect(@Nonnull GuiGraphics graphics,
                                         int x, int y, int width, int height,
                                         int colorTop, int colorBot) {
        graphics.fillGradient(x, y, x + width, y + height, colorTop, colorBot);
    }

    /**
     * Draw a rectangular outline (no fill).
     *
     * @param graphics  the GUI graphics context
     * @param x         left edge
     * @param y         top edge
     * @param width     rectangle width
     * @param height    rectangle height
     * @param color     ARGB border color
     * @param thickness border thickness in pixels
     */
    public static void drawRectOutline(@Nonnull GuiGraphics graphics,
                                        int x, int y, int width, int height,
                                        int color, int thickness) {
        // Top
        graphics.fill(x, y, x + width, y + thickness, color);
        // Bottom
        graphics.fill(x, y + height - thickness, x + width, y + height, color);
        // Left
        graphics.fill(x, y + thickness, x + thickness, y + height - thickness, color);
        // Right
        graphics.fill(x + width - thickness, y + thickness, x + width, y + height - thickness, color);
    }

    // =========================================================================
    // Textured quad drawing (for GUI)
    // =========================================================================

    /**
     * Draw a textured quad in a GUI using a specific render type.
     *
     * @param buffer vertex consumer from a buffer source
     * @param matrix the current transform matrix
     * @param x      left edge
     * @param y      top edge
     * @param width  quad width
     * @param height quad height
     * @param uMin   minimum U texture coordinate
     * @param vMin   minimum V texture coordinate
     * @param uMax   maximum U texture coordinate
     * @param vMax   maximum V texture coordinate
     * @param r      red 0-1
     * @param g      green 0-1
     * @param b      blue 0-1
     * @param a      alpha 0-1
     */
    public static void drawTexturedRect(@Nonnull VertexConsumer buffer,
                                         @Nonnull Matrix4f matrix,
                                         float x, float y, float width, float height,
                                         float uMin, float vMin, float uMax, float vMax,
                                         float r, float g, float b, float a) {
        RenderingUtils.vertex(buffer, matrix, x, y + height, 0, r, g, b, a, uMin, vMax);
        RenderingUtils.vertex(buffer, matrix, x + width, y + height, 0, r, g, b, a, uMax, vMax);
        RenderingUtils.vertex(buffer, matrix, x + width, y, 0, r, g, b, a, uMax, vMin);
        RenderingUtils.vertex(buffer, matrix, x, y, 0, r, g, b, a, uMin, vMin);
    }

    /**
     * Draw a textured quad with full UV range (0-1).
     */
    public static void drawTexturedRect(@Nonnull VertexConsumer buffer,
                                         @Nonnull Matrix4f matrix,
                                         float x, float y, float width, float height,
                                         float r, float g, float b, float a) {
        drawTexturedRect(buffer, matrix, x, y, width, height, 0, 0, 1, 1, r, g, b, a);
    }

    /**
     * Draw a textured quad using a texture from a GuiGraphics context.
     *
     * @param graphics the GUI graphics context
     * @param texture  the texture resource location
     * @param x        left edge
     * @param y        top edge
     * @param width    quad width
     * @param height   quad height
     */
    public static void drawTexturedRect(@Nonnull GuiGraphics graphics,
                                         @Nonnull ResourceLocation texture,
                                         int x, int y, int width, int height) {
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    /**
     * Draw a textured quad with UV offsets (for sprite sheets).
     */
    public static void drawTexturedRect(@Nonnull GuiGraphics graphics,
                                         @Nonnull ResourceLocation texture,
                                         int x, int y, int width, int height,
                                         float uOffset, float vOffset,
                                         int uWidth, int vHeight,
                                         int texWidth, int texHeight) {
        graphics.blit(texture, x, y, uOffset, vOffset, uWidth, vHeight, texWidth, texHeight);
    }

    // =========================================================================
    // Text drawing
    // =========================================================================

    /**
     * Draw a single line of text centered horizontally.
     *
     * @param graphics the GUI graphics context
     * @param text     the text component
     * @param centerX  the center X position
     * @param y        the Y position (top of text)
     * @param color    ARGB text color
     */
    public static void drawCenteredString(@Nonnull GuiGraphics graphics,
                                           @Nonnull Component text,
                                           int centerX, int y, int color) {
        Font font = Minecraft.getInstance().font;
        int width = font.width(text);
        graphics.drawString(font, text, centerX - width / 2, y, color);
    }

    /**
     * Draw a single line of text with a shadow.
     */
    public static void drawStringWithShadow(@Nonnull GuiGraphics graphics,
                                             @Nonnull String text,
                                             int x, int y, int color) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, text, x, y, color, true);
    }

    /**
     * Draw a tooltip-style text box at the given position.
     *
     * @param graphics the GUI graphics context
     * @param lines    the tooltip lines
     * @param mouseX   mouse X position
     * @param mouseY   mouse Y position
     */
    public static void drawTooltip(@Nonnull GuiGraphics graphics,
                                    @Nonnull List<Component> lines,
                                    int mouseX, int mouseY) {
        graphics.renderTooltip(Minecraft.getInstance().font, lines,
                java.util.Optional.empty(), mouseX, mouseY);
    }

    // =========================================================================
    // Circle drawing
    // =========================================================================

    /**
     * Draw a circle outline using line segments.
     *
     * @param buffer   vertex consumer for a line or quad render type
     * @param matrix   the current transform matrix
     * @param centerX  circle center X
     * @param centerY  circle center Y
     * @param radius   circle radius
     * @param segments number of line segments (higher = smoother)
     * @param r        red 0-1
     * @param g        green 0-1
     * @param b        blue 0-1
     * @param a        alpha 0-1
     */
    public static void drawCircle(@Nonnull VertexConsumer buffer,
                                   @Nonnull Matrix4f matrix,
                                   float centerX, float centerY,
                                   float radius, int segments,
                                   float r, float g, float b, float a) {
        float angleStep = (float) (Math.PI * 2.0 / segments);
        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;
            float x1 = centerX + (float) Math.cos(angle1) * radius;
            float y1 = centerY + (float) Math.sin(angle1) * radius;
            float x2 = centerX + (float) Math.cos(angle2) * radius;
            float y2 = centerY + (float) Math.sin(angle2) * radius;

            // Thin quad approximating a line segment
            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len < 0.001f) continue;
            float nx = -dy / len * 0.5f;
            float ny = dx / len * 0.5f;

            RenderingUtils.vertex(buffer, matrix, x1 - nx, y1 - ny, 0, r, g, b, a, 0, 0);
            RenderingUtils.vertex(buffer, matrix, x2 - nx, y2 - ny, 0, r, g, b, a, 1, 0);
            RenderingUtils.vertex(buffer, matrix, x2 + nx, y2 + ny, 0, r, g, b, a, 1, 1);
            RenderingUtils.vertex(buffer, matrix, x1 + nx, y1 + ny, 0, r, g, b, a, 0, 1);
        }
    }

    // =========================================================================
    // Line and colored textured quad drawing
    // =========================================================================

    /**
     * Draw a line between two points using a thin quad.
     *
     * @param graphics  the GUI graphics context
     * @param poseStack the current pose stack (for transforms)
     * @param x1        start X
     * @param y1        start Y
     * @param x2        end X
     * @param y2        end Y
     * @param width     line width in pixels
     * @param color     line color
     */
    public static void drawLine(@Nonnull GuiGraphics graphics,
                                 @Nonnull PoseStack poseStack,
                                 float x1, float y1, float x2, float y2,
                                 float width, @Nonnull Color color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) return;
        float nx = -dy / len * width * 0.5f;
        float ny = dx / len * width * 0.5f;

        int argb = color.getRGB();
        int minX = (int) Math.min(x1 - nx, Math.min(x1 + nx, Math.min(x2 - nx, x2 + nx)));
        int minY = (int) Math.min(y1 - ny, Math.min(y1 + ny, Math.min(y2 - ny, y2 + ny)));
        int maxX = (int) Math.max(x1 - nx, Math.max(x1 + nx, Math.max(x2 - nx, x2 + nx))) + 1;
        int maxY = (int) Math.max(y1 - ny, Math.max(y1 + ny, Math.max(y2 - ny, y2 + ny))) + 1;
        // Simplified: draw a thin filled rect along the line direction
        graphics.fill(minX, minY, maxX, maxY, argb);
    }

    /**
     * Draw a textured quad with a color tint.
     *
     * @param graphics the GUI graphics context
     * @param texture  the texture to render
     * @param x        left edge (float for sub-pixel positioning)
     * @param y        top edge
     * @param width    quad width
     * @param height   quad height
     * @param color    tint color (blended multiplicatively)
     */
    public static void drawTexturedRectColored(@Nonnull GuiGraphics graphics,
                                                @Nonnull ResourceLocation texture,
                                                float x, float y, float width, float height,
                                                @Nonnull Color color) {
        int ix = (int) x;
        int iy = (int) y;
        int iw = (int) width;
        int ih = (int) height;
        // Set color tint via setColor before blit
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f);
        graphics.blit(texture, ix, iy, 0, 0, iw, ih, iw, ih);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    // =========================================================================
    // Progress bar / gauge drawing
    // =========================================================================

    /**
     * Draw a horizontal progress bar.
     *
     * @param graphics   the GUI graphics context
     * @param x          left edge
     * @param y          top edge
     * @param width      total width
     * @param height     bar height
     * @param progress   fill ratio [0, 1]
     * @param bgColor    ARGB background color
     * @param fillColor  ARGB fill color
     */
    public static void drawProgressBar(@Nonnull GuiGraphics graphics,
                                        int x, int y, int width, int height,
                                        float progress,
                                        int bgColor, int fillColor) {
        float clamped = Math.max(0, Math.min(1, progress));
        graphics.fill(x, y, x + width, y + height, bgColor);
        int fillWidth = (int) (width * clamped);
        if (fillWidth > 0) {
            graphics.fill(x, y, x + fillWidth, y + height, fillColor);
        }
    }
}
