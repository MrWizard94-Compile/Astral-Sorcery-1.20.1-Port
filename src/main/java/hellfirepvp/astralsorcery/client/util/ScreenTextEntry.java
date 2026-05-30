package hellfirepvp.astralsorcery.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Lightweight text-input field used by the journal's search box.
 * Does not render itself — the journal screen renders it as a plain string.
 *
 * <p>1.16 → 1.20: TextInputUtil → TextFieldHelper (renamed in 1.18).</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenTextEntry {

    private String text = "";
    private Runnable changeCallback = null;

    private final TextFieldHelper inputUtil;

    public ScreenTextEntry() {
        inputUtil = new TextFieldHelper(
                this::getText,
                this::setText,
                TextFieldHelper.createClipboardGetter(Minecraft.getInstance()),
                TextFieldHelper.createClipboardSetter(Minecraft.getInstance()),
                s -> s.length() < 256);
    }

    public void setChangeCallback(@Nullable Runnable changeCallback) {
        this.changeCallback = changeCallback;
    }

    public void setText(@Nullable String newText) {
        String t = newText == null ? "" : newText;
        String prev = this.text;
        this.text = t;
        if (!t.equals(prev) && changeCallback != null) {
            changeCallback.run();
        }
    }

    @Nonnull
    public String getText() {
        return text;
    }

    public boolean keyTyped(int key) {
        if (key == GLFW.GLFW_KEY_ESCAPE   || key == GLFW.GLFW_KEY_ENTER
                || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_HOME
                || key == GLFW.GLFW_KEY_END      || key == GLFW.GLFW_KEY_INSERT
                || key == GLFW.GLFW_KEY_DELETE) {
            return false;
        }
        if (key >= GLFW.GLFW_KEY_RIGHT && key <= GLFW.GLFW_KEY_UP) {
            return false;
        }
        return this.inputUtil.keyPressed(key);
    }

    public boolean charTyped(char charCode) {
        return this.inputUtil.charTyped(charCode);
    }
}
