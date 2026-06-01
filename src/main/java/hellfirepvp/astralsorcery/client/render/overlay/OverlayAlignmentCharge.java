/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeRevealer;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Renders the alignment charge bar at the bottom-center of the screen.
 * Fades in when charge drops below 95 % or when an AlignmentChargeRevealer
 * item is equipped. Fades out after {@link #FADE_TICKS} ticks.
 *
 * 1.16 → 1.20: RenderGameOverlayEvent → IGuiOverlay, MatrixStack → GuiGraphics,
 * GL11 quads → GuiGraphics.blit, MainWindow → screenWidth/screenHeight params.
 */
@OnlyIn(Dist.CLIENT)
public class OverlayAlignmentCharge implements IGuiOverlay {

    public static final OverlayAlignmentCharge INSTANCE = new OverlayAlignmentCharge();

    private static final ResourceLocation TEX_CHARGE =
            AstralSorcery.key("textures/gui/overlay/charge.png");
    private static final ResourceLocation TEX_CHARGE_COLORLESS =
            AstralSorcery.key("textures/gui/overlay/charge_colorless.png");

    private static final int BAR_WIDTH   = 194;
    private static final int BAR_HEIGHT  = 54;
    private static final int FADE_TICKS  = 15;
    private static final float FADE_STEP = 1f / FADE_TICKS;

    private int   revealTicks = 0;
    private float alphaReveal  = 0f;

    private OverlayAlignmentCharge() {}

    // ---- Tick (called from ClientRenderEventHandler) ----

    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (AlignmentChargeHandler.INSTANCE.getFilledPercentage(player, LogicalSide.CLIENT) <= 0.95f) {
                revealCharge(20);
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty() && stack.getItem() instanceof AlignmentChargeRevealer revealer
                        && revealer.shouldReveal(stack)) {
                    revealCharge(20);
                    break;
                }
            }
        }

        revealTicks--;
        if (revealTicks - FADE_TICKS < 0) {
            alphaReveal = Math.max(0f, alphaReveal - FADE_STEP);
        } else {
            alphaReveal = Math.min(1f, alphaReveal + FADE_STEP);
        }
    }

    public void revealCharge(int forTicks) {
        revealTicks = forTicks;
    }

    public void resetReveal() {
        revealTicks = 0;
        alphaReveal  = 0f;
    }

    // ---- IGuiOverlay ----

    @Override
    public void render(@Nonnull ForgeGui gui, @Nonnull GuiGraphics graphics,
                        float partialTick, int screenWidth, int screenHeight) {
        if (alphaReveal <= 0f) return;

        Player player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return;

        float percFilled = AlignmentChargeHandler.INSTANCE.getFilledPercentage(player, LogicalSide.CLIENT);

        // Determine charge usage from any equipped AlignmentChargeConsumer
        boolean hasEnoughCharge = true;
        float usagePerc = 0f;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = player.getItemBySlot(slot);
            if (!equipped.isEmpty() && equipped.getItem() instanceof AlignmentChargeConsumer consumer) {
                float chargeRequired = consumer.getAlignmentChargeCost(player, equipped);
                float max = AlignmentChargeHandler.INSTANCE.getMaximumCharge(player, LogicalSide.CLIENT);
                usagePerc = Math.min(chargeRequired / max, percFilled);
                hasEnoughCharge = percFilled > usagePerc;
                percFilled -= usagePerc;
                break;
            }
        }

        int offsetLeft = screenWidth / 2 - BAR_WIDTH / 2;
        int offsetTop  = screenHeight - 78;

        int chargedWidth = (int) (BAR_WIDTH * percFilled);
        int usageWidth   = (int) (BAR_WIDTH * usagePerc);

        RenderSystem.enableBlend();

        // Colored (filled) portion
        if (chargedWidth > 0) {
            RenderSystem.setShaderColor(1f, 1f, 1f, alphaReveal);
            graphics.blit(TEX_CHARGE, offsetLeft, offsetTop, 0, 0,
                    chargedWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        // Usage / missing portion
        if (usageWidth > 0) {
            Color c = hasEnoughCharge ? ColorsAS.OVERLAY_CHARGE_USAGE : ColorsAS.OVERLAY_CHARGE_MISSING;
            RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, alphaReveal);
            graphics.blit(TEX_CHARGE_COLORLESS,
                    offsetLeft + chargedWidth, offsetTop,
                    chargedWidth, 0,
                    usageWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }
}
