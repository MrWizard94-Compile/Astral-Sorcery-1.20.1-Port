/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Base class for all Astral Sorcery GUI screens that are not tied
 * to a container/menu (i.e., standalone screens like the journal,
 * constellation paper, observatory view, etc.).
 *
 * <p>Provides:
 * <ul>
 *   <li>Centered background texture rendering</li>
 *   <li>Standardized dimensions and margins</li>
 *   <li>Common rendering helpers for AS visual style</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20:
 * {@code Screen.render(MatrixStack, int, int, float)} →
 * {@code Screen.render(GuiGraphics, int, int, float)}.
 * {@code Screen.drawCenteredString()} is gone — use GuiGraphics methods.</p>
 */
@OnlyIn(Dist.CLIENT)
public abstract class ScreenBaseAS extends Screen {

    /** Default background texture width. */
    protected static final int DEFAULT_WIDTH = 344;
    /** Default background texture height. */
    protected static final int DEFAULT_HEIGHT = 230;

    /** The background texture for this screen. */
    @Nonnull
    protected final ResourceLocation backgroundTexture;

    /** Background texture dimensions. */
    protected final int bgWidth;
    protected final int bgHeight;

    /** Top-left corner of the background texture in screen coordinates. */
    protected int guiLeft;
    protected int guiTop;

    protected ScreenBaseAS(@Nonnull Component title,
                            @Nonnull ResourceLocation backgroundTexture,
                            int bgWidth, int bgHeight) {
        super(title);
        this.backgroundTexture = backgroundTexture;
        this.bgWidth = bgWidth;
        this.bgHeight = bgHeight;
    }

    protected ScreenBaseAS(@Nonnull Component title,
                            @Nonnull ResourceLocation backgroundTexture) {
        this(title, backgroundTexture, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.bgWidth) / 2;
        this.guiTop = (this.height - this.bgHeight) / 2;
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dark overlay background
        this.renderBackground(graphics);

        // Background texture
        renderBackgroundTexture(graphics);

        // Subclass content
        renderContent(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Render the background texture centered on screen.
     */
    protected void renderBackgroundTexture(@Nonnull GuiGraphics graphics) {
        RenderingDrawUtils.drawTexturedRect(graphics, backgroundTexture,
                guiLeft, guiTop, bgWidth, bgHeight);
    }

    /**
     * Subclasses implement this to render screen content
     * on top of the background.
     *
     * @param graphics   the GUI graphics context
     * @param mouseX     mouse X position
     * @param mouseY     mouse Y position
     * @param partialTick partial tick for animation
     */
    protected abstract void renderContent(@Nonnull GuiGraphics graphics,
                                           int mouseX, int mouseY, float partialTick);

    /**
     * Test if the mouse is within a rectangular region relative to guiLeft/guiTop.
     *
     * @param mouseX  current mouse X
     * @param mouseY  current mouse Y
     * @param x       region left (relative to guiLeft)
     * @param y       region top (relative to guiTop)
     * @param width   region width
     * @param height  region height
     * @return true if mouse is inside
     */
    protected boolean isMouseInRegion(int mouseX, int mouseY,
                                       int x, int y, int width, int height) {
        int absX = guiLeft + x;
        int absY = guiTop + y;
        return mouseX >= absX && mouseX < absX + width
                && mouseY >= absY && mouseY < absY + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
