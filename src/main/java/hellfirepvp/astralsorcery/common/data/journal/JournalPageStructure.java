package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Journal page that displays a structure schematic.
 * Renders a text description; full 3D schematic rendering is not implemented.
 */
public class JournalPageStructure implements JournalPage {

    private static final int COLOR_NOTE = 0x88AAAACC;

    @Nullable
    private final StructureTemplate structure;

    public JournalPageStructure(@Nullable StructureTemplate structure) {
        this.structure = structure;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        return new StructurePlaceholderPage(node);
    }

    @OnlyIn(Dist.CLIENT)
    private static class StructurePlaceholderPage implements RenderablePage {

        private final List<net.minecraft.util.FormattedCharSequence> lines;

        @SuppressWarnings("null")
        StructurePlaceholderPage(ResearchNode node) {
            var font = Minecraft.getInstance().font;
            Component header = Component.translatable("astralsorcery.journal.structure.header");
            Component body   = Component.translatable("astralsorcery.journal.structure.body");
            lines = new java.util.ArrayList<>();
            lines.addAll(font.split(header, DEFAULT_WIDTH));
            lines.add(net.minecraft.util.FormattedCharSequence.EMPTY);
            lines.addAll(font.split(body,   DEFAULT_WIDTH));
        }

        @Override
        @SuppressWarnings("null")
        public void render(GuiGraphics graphics, int offsetX, int offsetY,
                           float partialTick, int mouseX, int mouseY) {
            var font = Minecraft.getInstance().font;
            int y = offsetY + 10;
            for (var line : lines) {
                graphics.drawString(font, line, offsetX, y, COLOR_NOTE, false);
                y += 10;
            }
        }
    }
}
