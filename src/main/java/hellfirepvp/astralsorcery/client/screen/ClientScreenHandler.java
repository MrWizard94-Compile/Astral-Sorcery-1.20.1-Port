/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRefractionTable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Client-side helper for opening Astral Sorcery GUI screens.
 * All methods here must only be called on the client (Dist.CLIENT).
 *
 * <p>This class exists so that common-side code (blocks, items) can
 * reference screen-opening functionality through a proxy pattern
 * without directly importing client-only screen classes.</p>
 *
 * <p>1.16 → 1.20: Minecraft.getInstance().setScreen() stable.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientScreenHandler {

    private ClientScreenHandler() {}

    /**
     * Opens the telescope constellation discovery screen.
     * Called from BlockTelescope and ItemHandTelescope on client side.
     */
    public static void openTelescopeScreen() {
        Minecraft.getInstance().setScreen(new ScreenTelescope());
    }

    /**
     * Opens the observatory full-screen sky view.
     * Called from BlockObservatory on client side after the server mounts the player.
     */
    public static void openObservatoryScreen() {
        Minecraft.getInstance().setScreen(new ScreenObservatory());
    }

    /**
     * Opens the perk tree screen.
     * Called when the player opens the perk allocation UI.
     */
    public static void openPerkTreeScreen() {
        Minecraft.getInstance().setScreen(new ScreenPerkTree());
    }

    /**
     * Opens the journal/knowledge screen.
     * Called from the journal item or key binding.
     */
    public static void openJournalScreen() {
        Minecraft.getInstance().setScreen(ScreenJournalProgression.getJournalInstance());
    }

    /**
     * Opens the refraction table engraving screen.
     * Called from BlockRefractionTable when right-clicked with empty hand.
     */
    public static void openRefractionTableScreen(@Nonnull BlockEntityRefractionTable tile) {
        Minecraft.getInstance().setScreen(new ScreenRefractionTable(tile));
    }
}
