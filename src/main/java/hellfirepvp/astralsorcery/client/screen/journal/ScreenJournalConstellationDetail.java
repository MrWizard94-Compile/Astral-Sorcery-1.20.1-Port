package hellfirepvp.astralsorcery.client.screen.journal;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageAltarRecipe;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed constellation journal page: shows constellation drawing, description,
 * ritual/mantle/enchantment info, and moon phase activity.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; IReorderingProcessor → FormattedCharSequence;
 * font.trimStringToWidth → font.split; font.getStringPropertyWidth → font.width;
 * IWeakConstellation.hasConstellationDiscovered → PlayerProgress.hasDiscovered;
 * SkyHandler moon-phase activity wired via SkyHandler.getContext() + ConstellationHandler.isActiveInPhase().</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournalConstellationDetail extends ScreenJournal implements NavigationArrowScreen {

    private final ScreenJournal origin;
    private final IConstellation constellation;
    private final boolean detailed;

    private int doublePageID = 0;
    private int doublePages  = 0;

    private Rectangle rectBack, rectNext, rectPrev;

    private final List<FormattedCharSequence> locTextMain       = new ArrayList<>();
    private final List<FormattedCharSequence> locTextRitual     = new ArrayList<>();
    private final List<FormattedCharSequence> locTextRefraction = new ArrayList<>();
    private final List<FormattedCharSequence> locTextMantle     = new ArrayList<>();

    public ScreenJournalConstellationDetail(ScreenJournal origin, IConstellation cst) {
        super(cst.getConstellationName(), NO_BOOKMARK);
        this.origin = origin;
        this.constellation = cst;
        this.detailed = ResearchHelper.getClientProgress().hasDiscovered(cst.getRegistryName());

        PlayerProgress progress = ResearchHelper.getClientProgress();
        if (detailed) {
            if (progress.isAtLeast(ProgressionTier.ATTUNEMENT)) {
                this.doublePages++;
            }
            if (progress.isAtLeast(ProgressionTier.TRAIT_CRAFT)) {
                if (!(constellation instanceof IMinorConstellation)) {
                    this.doublePages++;
                }
                this.doublePages++;
            }
        }

        buildMainText();
        buildEnchText();
        buildRitualText();
        buildCapeText();
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    private void buildCapeText() {
        if (!(constellation instanceof IWeakConstellation weak)) return;
        if (!ResearchHelper.getClientProgress().isAtLeast(ProgressionTier.TRAIT_CRAFT)) return;

        addHeading(locTextMantle, "astralsorcery.journal.constellation.mantle");
        splitAndAdd(locTextMantle, weak.getInfoMantleEffect());
    }

    private void buildEnchText() {
        if (!ResearchHelper.getClientProgress().isAtLeast(ProgressionTier.CONSTELLATION_CRAFT)) return;

        addHeading(locTextRefraction, "astralsorcery.journal.constellation.enchantments");
        splitAndAdd(locTextRefraction, constellation.getConstellationEnchantmentDescription());
    }

    private void buildRitualText() {
        PlayerProgress progress = ResearchHelper.getClientProgress();
        if (constellation instanceof IMinorConstellation minor) {
            if (!progress.isAtLeast(ProgressionTier.TRAIT_CRAFT)) return;
            addHeading(locTextRitual, "astralsorcery.journal.constellation.ritual.trait");
            splitAndAdd(locTextRitual, minor.getInfoTraitEffect());
        } else if (constellation instanceof IWeakConstellation weak) {
            if (progress.isAtLeast(ProgressionTier.ATTUNEMENT)) {
                addHeading(locTextRitual, "astralsorcery.journal.constellation.ritual");
                splitAndAdd(locTextRitual, weak.getInfoRitualEffect());
            }
            if (progress.isAtLeast(ProgressionTier.TRAIT_CRAFT)) {
                addHeading(locTextRitual, "astralsorcery.journal.constellation.corruption");
                splitAndAdd(locTextRitual, weak.getInfoCorruptedRitualEffect());
            }
        }
    }

    private void buildMainText() {
        splitAndAdd(locTextMain, constellation.getConstellationDescription());
    }

    private void addHeading(List<FormattedCharSequence> target, String key) {
        Component heading = Component.translatable(key);
        target.addAll(Minecraft.getInstance().font.split(heading, JournalPage.DEFAULT_WIDTH));
        target.add(FormattedCharSequence.EMPTY);
    }

    private void splitAndAdd(List<FormattedCharSequence> target, MutableComponent text) {
        String raw = text.getString();
        for (String segment : raw.split("<NL>")) {
            target.addAll(Minecraft.getInstance().font.split(
                    Component.literal(segment), JournalPage.DEFAULT_WIDTH));
            target.add(FormattedCharSequence.EMPTY);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        super.render(graphics, mouseX, mouseY, pTicks);

        if (doublePageID == 0) {
            drawCstBackground(graphics);
            drawDefault(graphics, TexturesAS.TEX_GUI_BOOK_FRAME_LEFT, mouseX, mouseY);
        } else {
            drawDefault(graphics, TexturesAS.TEX_GUI_BOOK_BLANK, mouseX, mouseY);
        }

        drawNavArrows(graphics, pTicks, mouseX, mouseY);

        switch (doublePageID) {
            case 0 -> {
                drawPageConstellation(graphics, pTicks);
                drawPageMoonPhases(graphics);
                drawPageExtendedInformation(graphics);
            }
            case 1 -> drawRefractionTableInformation(graphics);
            case 2 -> drawCapeInformationPages(graphics, mouseX, mouseY, pTicks);
            case 3 -> drawConstellationPaperRecipePage(graphics, mouseX, mouseY, pTicks);
            default -> { }
        }
    }

    private void drawRefractionTableInformation(GuiGraphics graphics) {
        renderTextLines(graphics, locTextRitual, guiLeft + 30, guiTop + 30);
        renderTextLines(graphics, locTextRefraction, guiLeft + 220, guiTop + 30);
    }

    private void drawCapeInformationPages(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        renderTextLines(graphics, locTextMantle, guiLeft + 30, guiTop + 30);

        if (ResearchHelper.getClientProgress().isAtLeast(ProgressionTier.TRAIT_CRAFT)) {
            SimpleAltarRecipe recipe = RecipeHelper.findAltarRecipeResult(stack ->
                    stack.getItem() instanceof ItemMantle mantle &&
                    constellation.getRegistryName().equals(mantle.getConstellation()));
            if (recipe != null) {
                RenderPageAltarRecipe page = new RenderPageAltarRecipe(null, -1, recipe);
                page.render(graphics, guiLeft + 220, guiTop + 20, pTicks, mouseX, mouseY);
                page.postRender(graphics, guiLeft + 220, guiTop + 20, pTicks, mouseX, mouseY);
            }
        }
    }

    private void drawConstellationPaperRecipePage(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        if (ResearchHelper.getClientProgress().isAtLeast(ProgressionTier.TRAIT_CRAFT)) {
            SimpleAltarRecipe recipe = RecipeHelper.findAltarRecipeResult(stack ->
                    stack.getItem() instanceof ItemConstellationPaper &&
                    constellation.getRegistryName().equals(ItemConstellationPaper.getConstellation(stack)));
            if (recipe != null) {
                RenderPageAltarRecipe page = new RenderPageAltarRecipe(null, -1, recipe);
                page.render(graphics, guiLeft + 30, guiTop + 20, pTicks, mouseX, mouseY);
                page.postRender(graphics, guiLeft + 30, guiTop + 20, pTicks, mouseX, mouseY);
            }
        }
    }

    private void renderTextLines(GuiGraphics graphics, List<FormattedCharSequence> lines,
                                  int startX, int startY) {
        var font = Minecraft.getInstance().font;
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), startX, startY + i * 10, 0xFFCCCCCC, true);
        }
    }

    private void drawPageExtendedInformation(GuiGraphics graphics) {
        var font = Minecraft.getInstance().font;
        Component info = detailed
                ? constellation.getConstellationTag()
                : Component.translatable("astralsorcery.journal.constellation.unknown");

        int nameW = font.width(info);
        graphics.drawString(font, info, guiLeft + 305 - nameW / 2, guiTop + 44, 0xFFCCCCCC, true);

        if (detailed && !locTextMain.isEmpty()) {
            int offsetY = guiTop + 77;
            for (FormattedCharSequence line : locTextMain) {
                graphics.drawString(font, line, guiLeft + 220, offsetY, 0xFFCCCCCC, true);
                offsetY += 13;
            }
        }
    }

    private void drawPageMoonPhases(GuiGraphics graphics) {
        int size = 19;
        int totalW = MoonPhase.values().length * (size + 2);
        int offsetX = 95 + guiLeft + guiWidth / 2 - totalW / 2;
        int offsetY = guiTop + 199;

        // Ask SkyHandler for the active phase map — falls back to uniform 50% if not available
        WorldContext ctx = SkyHandler.getContext(Minecraft.getInstance().level);

        MoonPhase[] phases = MoonPhase.values();
        Matrix4f matrix = graphics.pose().last().pose();

        for (int i = 0; i < phases.length; i++) {
            MoonPhase phase = phases[i];
            boolean active = ctx != null && ctx.getConstellationHandler().isActiveInPhase(constellation, phase);
            float brightness = active ? 1.0f : 0.35f;
            RenderSystem.setShaderTexture(0, phase.getTexture().getKey());
            Blending.DEFAULT.apply();
            RenderingUtils.drawColorTex(matrix,
                    offsetX + i * (size + 2), offsetY, size, size,
                    brightness, brightness, brightness, active ? 1.0f : 0.5f);
        }
        RenderSystem.disableBlend();
    }

    private void drawPageConstellation(GuiGraphics graphics, float partial) {
        var font = Minecraft.getInstance().font;
        Component cstName = constellation.getConstellationName();
        int nameW = font.width(cstName);

        graphics.pose().pushPose();
        graphics.pose().translate(guiLeft + 305 - nameW * 1.8f / 2f, guiTop + 26, 0);
        graphics.pose().scale(1.8f, 1.8f, 1f);
        graphics.drawString(font, cstName, 0, 0, 0xFFC3C3C3, true);
        graphics.pose().popPose();

        Component typeDesc = detailed
                ? constellation.getConstellationTypeDescription()
                : Component.translatable("astralsorcery.journal.constellation.unknown");
        int typeW = font.width(typeDesc);
        graphics.drawString(font, typeDesc, guiLeft + 305 - typeW / 2, guiTop + 219, 0xFFDDDDDD, true);

        long tick = ClientScheduler.getClientTick();
        float brightness = 0.6f + 0.4f * RenderingConstellationUtils.getTwinkleBrightness(0, (int) tick, partial);
        Color cstColor = detailed ? constellation.getConstellationColor() : new Color(0.4f, 0.4f, 0.4f);
        // Tint draw by setting shader color
        RenderSystem.setShaderColor(
                cstColor.getRed() / 255f, cstColor.getGreen() / 255f,
                cstColor.getBlue() / 255f, brightness);
        RenderingConstellationUtils.renderConstellationLarge(graphics, constellation,
                guiLeft + 40, guiTop + 60, 150, 150, brightness);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawNavArrows(GuiGraphics graphics, float pTicks, int mouseX, int mouseY) {
        rectBack = drawArrow(graphics, guiLeft + 197, guiTop + 230, Type.LEFT, mouseX, mouseY, pTicks);
        rectPrev = null;
        rectNext = null;
        if (doublePageID - 1 >= 0) {
            rectPrev = drawArrow(graphics, guiLeft + 25, guiTop + 220, Type.LEFT, mouseX, mouseY, pTicks);
        }
        if (doublePageID + 1 <= doublePages) {
            rectNext = drawArrow(graphics, guiLeft + 367, guiTop + 220, Type.RIGHT, mouseX, mouseY, pTicks);
        }
    }

    private void drawCstBackground(GuiGraphics graphics) {
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShaderTexture(0, TexturesAS.TEX_BLACK.getKey());
        RenderingUtils.drawColorTex(matrix,
                guiLeft + 15, guiTop + 10, 185, 230, 1f, 1f, 1f, 1f);

        RenderSystem.setShaderTexture(0, TexturesAS.TEX_GUI_BACKGROUND_CONSTELLATIONS.getKey());
        Blending.DEFAULT.apply();
        RenderingUtils.drawColorTex(matrix,
                guiLeft + 15, guiTop + 10, 185, 230,
                0.8f, 0.8f, 1f, 0.5f,
                0.3f, 0.1f, 0.7f, 0.9f);
        RenderSystem.disableBlend();
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(origin);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        if (handleBookmarkClick(mouseX, mouseY)) return true;
        if (rectBack != null && rectBack.contains(mouseX, mouseY)) {
            Minecraft.getInstance().setScreen(origin);
            return true;
        }
        if (rectPrev != null && rectPrev.contains(mouseX, mouseY) && doublePageID >= 1) {
            doublePageID--;
            return true;
        }
        if (rectNext != null && rectNext.contains(mouseX, mouseY) && doublePageID < doublePages) {
            doublePageID++;
            return true;
        }
        return false;
    }
}
