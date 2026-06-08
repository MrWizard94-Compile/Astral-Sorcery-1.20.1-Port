package hellfirepvp.astralsorcery.client.screen.base;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Rectangle;

/**
 * Provides default navigation arrow rendering for journal screens.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; GL11.GL_QUADS vertex drawing replaced
 * by GuiGraphics.blit with scale transform; RenderSystem blend calls retained.</p>
 */
@OnlyIn(Dist.CLIENT)
public interface NavigationArrowScreen {

    default Rectangle drawArrow(GuiGraphics graphics, int offsetLeft, int offsetTop,
                                 Type direction, int mouseX, int mouseY, float pTicks) {
        float width  = 30f;
        float height = 15f;

        Rectangle rectArrow = new Rectangle(offsetLeft, offsetTop, (int) width, (int) height);
        boolean hovered = rectArrow.contains(mouseX, mouseY);

        graphics.pose().pushPose();
        graphics.pose().translate(rectArrow.getX() + width / 2f, rectArrow.getY() + height / 2f, 0);

        float scale;
        if (hovered) {
            scale = 1.1f;
        } else {
            double t = ClientScheduler.getClientTick() + pTicks;
            scale = (float) Math.sin(t / 4.0) / 32f + 1f;
        }
        graphics.pose().scale(scale, scale, 1f);
        graphics.pose().translate(-(width / 2f), -(height / 2f), 0);

        // UV layout: arrows.png has 2 columns × 2 rows
        // left-pointing: row 1 (v=0.5..1), right-pointing: row 0 (v=0..0.5)
        // normal: u=0..0.5, hovered: u=0.5..1
        float uFrom   = hovered ? 0.5f : 0f;
        float vFrom   = direction == Type.LEFT ? 0.5f : 0f;
        int uOffset   = (int) (uFrom * 60);  // texture is 60×30 (2×2 layout)
        int vOffset   = (int) (vFrom * 30);

        RenderingDrawUtils.drawTexturedRect(graphics, TexturesAS.TEX_GUI_BOOK_ARROWS.getKey(),
                0, 0, (int) width, (int) height,
                uOffset, vOffset, (int) width, (int) height, 60, 30);

        graphics.pose().popPose();
        return rectArrow;
    }

    enum Type {
        LEFT,
        RIGHT
    }
}
