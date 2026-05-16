/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.screen.ScreenJournal;
import hellfirepvp.astralsorcery.client.screen.ScreenPerkTree;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

/**
 * Astral Sorcery keybinds for accessing mod screens.
 *
 * <p>1.16 → 1.20:
 * {@code KeyBinding} → {@code KeyMapping}.
 * Registration via {@code RegisterKeyMappingsEvent} (mod bus event).
 * Input check via {@code InputConstants.Type.KEYSYM}.
 * Key consumption pattern unchanged.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class KeyBindingsAS {

    private KeyBindingsAS() {}

    private static final String CATEGORY = "key.categories." + AstralSorcery.MODID;

    /**
     * Open the Astral Tome (journal/knowledge book).
     * Default: J
     */
    public static final KeyMapping KEY_JOURNAL = new KeyMapping(
            "key.astralsorcery.journal",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY);

    /**
     * Open the Perk Tree screen.
     * Default: P
     */
    public static final KeyMapping KEY_PERK_TREE = new KeyMapping(
            "key.astralsorcery.perk_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            CATEGORY);

    // =========================================================================
    // Registration (mod bus event)
    // =========================================================================

    /**
     * Register all keybinds. Called from ClientProxy via mod bus listener.
     */
    public static void register(@Nonnull RegisterKeyMappingsEvent event) {
        event.register(KEY_JOURNAL);
        event.register(KEY_PERK_TREE);
    }

    // =========================================================================
    // Input handling (forge bus event, handled by ClientRenderEventHandler)
    // =========================================================================

    /**
     * Process keybind presses during client tick.
     * Must be called from a client tick event handler.
     */
    public static void handleInput() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        while (KEY_JOURNAL.consumeClick()) {
            mc.setScreen(new ScreenJournal());
        }

        while (KEY_PERK_TREE.consumeClick()) {
            mc.setScreen(new ScreenPerkTree());
        }
    }
}
