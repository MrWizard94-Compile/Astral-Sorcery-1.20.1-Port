/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.item.base.PerkExperienceRevealer;
import hellfirepvp.astralsorcery.common.perk.PerkLevelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import javax.annotation.Nonnull;

/**
 * Renders a vertical perk experience bar on the left edge of the screen.
 * Shown when the player holds a {@link PerkExperienceRevealer} item and is
 * attuned. Fades in/out over {@link #FADE_TICKS} ticks.
 *
 * 1.16 → 1.20: RenderGameOverlayEvent → IGuiOverlay,
 * fontRenderer → font, StringTextComponent → Component,
 * GL11 vertex batching → GuiGraphics.blit / drawString.
 */
@OnlyIn(Dist.CLIENT)
public class OverlayPerkExperience implements IGuiOverlay {

    public static final OverlayPerkExperience INSTANCE = new OverlayPerkExperience();

    private static final ResourceLocation TEX_FRAME =
            AstralSorcery.key("textures/gui/overlay/exp_frame.png");
    private static final ResourceLocation TEX_BAR =
            AstralSorcery.key("textures/gui/overlay/exp_bar.png");

    // Layout constants matching the 1.16 design
    private static final int FRAME_X      =  0;
    private static final int FRAME_Y      =  5;
    private static final int FRAME_WIDTH  = 32;
    private static final int FRAME_HEIGHT = 128;
    private static final int BAR_Y_OFFSET = 27;   // bar top within frame
    private static final int BAR_HEIGHT   = 78;   // bar drawable area

    private static final int FADE_TICKS  = 15;
    private static final float FADE_STEP = 1f / FADE_TICKS;

    private int   revealTicks      = 0;
    private float visibilityReveal = 0f;

    private OverlayPerkExperience() {}

    // ---- Tick (called from ClientRenderEventHandler) ----

    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            for (ItemStack stack : player.getAllSlots()) {
                if (!stack.isEmpty() && stack.getItem() instanceof PerkExperienceRevealer revealer
                        && revealer.shouldReveal(stack)) {
                    revealExperience(20);
                    break;
                }
            }
        }

        revealTicks--;
        if (revealTicks - FADE_TICKS < 0) {
            visibilityReveal = Math.max(0f, visibilityReveal - FADE_STEP);
        } else {
            visibilityReveal = Math.min(1f, visibilityReveal + FADE_STEP);
        }
    }

    public void revealExperience(int forTicks) {
        revealTicks = forTicks;
    }

    public void resetReveal() {
        revealTicks      = 0;
        visibilityReveal = 0f;
    }

    // ---- IGuiOverlay ----

    @Override
    public void render(@Nonnull ForgeGui gui, @Nonnull GuiGraphics graphics,
                        float partialTick, int screenWidth, int screenHeight) {
        if (visibilityReveal <= 0f) return;

        PlayerProgress progress = PlayerProgressManager.getClientProgress();
        if (progress.getAttunedConstellation() == null) return;

        long perkExp  = progress.getPerkExp();
        float perc    = PerkLevelManager.getLevelProgress(perkExp);
        int   level   = PerkLevelManager.getLevelFromExp(perkExp);
        int   barHeight = (int) (BAR_HEIGHT * perc);

        RenderSystem.enableBlend();

        // Frame
        RenderSystem.setShaderColor(1f, 1f, 1f, visibilityReveal * 0.9f);
        graphics.blit(TEX_FRAME, FRAME_X, FRAME_Y, 0, 0,
                FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);

        // Bar fill (bottom-up): take the bottom `barHeight` pixels of the texture
        if (barHeight > 0) {
            int vOffset = BAR_HEIGHT - barHeight;
            int barScreenY = FRAME_Y + BAR_Y_OFFSET + vOffset;
            RenderSystem.setShaderColor(1f, 0.9f, 0f, visibilityReveal * 0.9f);
            graphics.blit(TEX_BAR, FRAME_X, barScreenY, 0, vOffset,
                    FRAME_WIDTH, barHeight, FRAME_WIDTH, BAR_HEIGHT);
        }

        // Level number
        String levelStr = String.valueOf(level);
        int textX = FRAME_X + FRAME_WIDTH / 2 - Minecraft.getInstance().font.width(levelStr) / 2 + 2;
        int textY = FRAME_Y + BAR_Y_OFFSET + BAR_HEIGHT + 6;
        int alpha  = (int) (255f * visibilityReveal);
        int color  = (alpha << 24) | 0x00DDDDDD;
        if (visibilityReveal > 1e-4f) {
            graphics.drawString(Minecraft.getInstance().font, levelStr, textX, textY, color, false);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }
}
