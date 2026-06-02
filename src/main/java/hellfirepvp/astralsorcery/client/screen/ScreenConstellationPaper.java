package hellfirepvp.astralsorcery.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays a constellation paper item — shows the star pattern and
 * the moon phases during which the constellation is visible.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; renderConstellationIntoGUI → renderConstellationLarge;
 * IFormattableTextComponent → Component; SkyHandler.getContext loses LogicalSide parameter.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenConstellationPaper extends WidthHeightScreen {

    private final IConstellation constellation;
    private List<MoonPhase> activePhasesCache = null;

    public ScreenConstellationPaper(@Nonnull IConstellation constellation) {
        super(constellation.getConstellationName(), 275, 344);
        this.constellation = constellation;
    }

    @Override
    public void onClose() {
        super.onClose();
        SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_CLOSE.get(), 1F, 1F);
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        drawWHRect(graphics, TexturesAS.TEX_GUI_CONSTELLATION_PAPER);
        drawHeader(graphics);
        drawConstellation(graphics);
        drawPhaseInformation(graphics);
    }

    private void drawHeader(GuiGraphics graphics) {
        Component name = constellation.getConstellationName();
        int textWidth = Minecraft.getInstance().font.width(name);
        float scale = 1.8f;
        int x = (int) ((width / 2f) - (textWidth * scale / 2f));
        int y = guiTop + 45;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(Minecraft.getInstance().font, name, 0, 0, 0x4D4D4D, false);
        graphics.pose().popPose();
    }

    private void drawConstellation(GuiGraphics graphics) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        int cstX = width / 2 - 72;
        int cstY = guiTop + 84;
        RenderingConstellationUtils.renderConstellationLarge(graphics, constellation,
                cstX, cstY, 145, 145, 0.75f);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void drawPhaseInformation(GuiGraphics graphics) {
        if (activePhasesCache == null) {
            resolvePhases();
        }

        List<MoonPhase> phases = activePhasesCache == null ? Collections.emptyList() : activePhasesCache;
        if (phases.isEmpty()) {
            Component unknown = Component.translatable("astralsorcery.journal.constellation.unknown");
            int textWidth = Minecraft.getInstance().font.width(unknown);
            float scale = 1.4f;
            int x = (int) ((width / 2f) - (textWidth * scale / 2f));
            int y = guiTop + 237;

            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.drawString(Minecraft.getInstance().font, unknown, 0, 0, 0x4D4D4D, false);
            graphics.pose().popPose();
        } else {
            int iconSize = 16;
            int spacing = 2;
            int totalWidth = phases.size() * (iconSize + spacing) - spacing;
            int startX = width / 2 - totalWidth / 2;
            int startY = guiTop + 237;

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            for (int i = 0; i < phases.size(); i++) {
                int x = startX + i * (iconSize + spacing);
                RenderingDrawUtils.drawTexturedRect(graphics, phases.get(i).getTexture().getKey(),
                        x, startY, iconSize, iconSize);
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void resolvePhases() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        WorldContext ctx = SkyHandler.getContext(mc.level);
        if (ctx == null) return;

        activePhasesCache = new ArrayList<>();
        for (MoonPhase phase : MoonPhase.values()) {
            if (ctx.getConstellationHandler().isActiveInPhase(constellation, phase)) {
                activePhasesCache.add(phase);
            }
        }
    }
}
