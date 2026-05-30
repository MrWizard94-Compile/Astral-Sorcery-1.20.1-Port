package hellfirepvp.astralsorcery.client.screen.base;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

/**
 * Base screen class adding drag support (pan/scroll) and per-tick key-hold callbacks.
 *
 * <p>1.16 → 1.20: no API changes in the drag/key handling —
 * Screen is still the base, signatures are identical.</p>
 */
@OnlyIn(Dist.CLIENT)
public class InputScreen extends Screen {

    private final Set<Integer> heldKeys = new HashSet<>();

    private double oMouseX, oMouseY;
    private boolean dragging = false;

    protected InputScreen(Component name) {
        super(name);
    }

    @Override
    public void tick() {
        heldKeys.forEach(this::keyPressedTick);
        super.tick();
    }

    protected void keyPressedTick(int key) {}

    protected void mouseDragStart(double mouseX, double mouseY) {}

    protected void mouseDragStop(double mouseX, double mouseY, double mouseDiffX, double mouseDiffY) {}

    protected void mouseDragTick(double mouseX, double mouseY, double mouseDiffX, double mouseDiffY,
                                 double mouseOffsetX, double mouseOffsetY) {}

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        heldKeys.add(key);
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        heldKeys.remove(key);
        return super.keyReleased(key, scanCode, modifiers);
    }

    public boolean isCurrentlyDragging() {
        return this.dragging;
    }

    protected void stopDragging(double mouseX, double mouseY) {
        if (this.dragging) {
            this.dragging = false;
            this.mouseDragStop(mouseX, mouseY, oMouseX, oMouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragging = true;
            this.oMouseX = mouseX;
            this.oMouseY = mouseY;
            this.mouseDragStart(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.stopDragging(mouseX, mouseY);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickType, double offsetX, double offsetY) {
        if (clickType == 0 && this.dragging) {
            double diffX = this.oMouseX - mouseX;
            double diffY = this.oMouseY - mouseY;
            this.mouseDragTick(mouseX, mouseY, diffX, diffY, offsetX, offsetY);
        }
        return super.mouseDragged(mouseX, mouseY, clickType, offsetX, offsetY);
    }
}
