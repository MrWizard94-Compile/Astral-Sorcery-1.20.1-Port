package hellfirepvp.astralsorcery.client.screen.base;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

/**
 * A fixed-size screen with drag/scroll support.
 * Extends {@link InputScreen} for key-hold and mouse-drag handling.
 *
 * <p>1.16 → 1.20: MatrixStack replaced by GuiGraphics throughout;
 * closeScreen() → onClose(); gameSettings.keyBindInventory → options.keyInventory;
 * RenderSystem.enableAlphaTest() calls removed (no-op in 1.17+).</p>
 */
@OnlyIn(Dist.CLIENT)
public class WidthHeightScreen extends InputScreen {

    protected final int guiHeight;
    protected final int guiWidth;
    protected int guiLeft, guiTop;

    protected boolean closeWithInventoryKey = true;

    protected WidthHeightScreen(Component title, int guiHeight, int guiWidth) {
        super(title);
        this.guiHeight = guiHeight;
        this.guiWidth = guiWidth;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = width / 2 - guiWidth / 2;
        this.guiTop  = height / 2 - guiHeight / 2;
    }

    public int getGuiLeft()   { return guiLeft; }
    public int getGuiTop()    { return guiTop; }
    public int getGuiWidth()  { return guiWidth; }
    public int getGuiHeight() { return guiHeight; }
    public float getGuiZLevel() { return 0f; }

    public Rectangle getGuiBox() {
        return new Rectangle(guiLeft, guiTop, guiWidth, guiHeight);
    }

    protected void drawWHRect(GuiGraphics graphics, AbstractRenderableTexture resource) {
        RenderingDrawUtils.drawTexturedRect(graphics, resource.getKey(), guiLeft, guiTop, guiWidth, guiHeight);
    }

    @Override
    public boolean charTyped(char charCode, int keyModifiers) {
        if (super.charTyped(charCode, keyModifiers)) return true;
        if (closeWithInventoryKey && Minecraft.getInstance().options.keyInventory.isDown()) {
            this.onClose();
            if (Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 1 && shouldRightClickCloseScreen(mouseX, mouseY)) {
            this.onClose();
            if (Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
            return true;
        }
        return false;
    }

    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
