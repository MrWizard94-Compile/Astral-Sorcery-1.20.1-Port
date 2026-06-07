package hellfirepvp.astralsorcery.client.screen.journal.page;

import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.journal.RenderablePage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Journal page renderer for lore/description text.
 * Wraps a translation key into lines at {@link JournalPage#DEFAULT_WIDTH}.
 *
 * <p>1.16 → 1.20: IReorderingProcessor → FormattedCharSequence;
 * LanguageMap → I18n; font.trimStringToWidth → font.split;
 * RenderingDrawUtils.renderStringAt → GuiGraphics.drawString.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderPageText implements RenderablePage {

    private final List<FormattedCharSequence> lines;

    public RenderPageText(String translationKey) {
        this.lines = buildLines(translationKey);
    }

    private static List<FormattedCharSequence> buildLines(String key) {
        String raw = net.minecraft.client.resources.language.I18n.get(key);
        List<FormattedCharSequence> out = new ArrayList<>();
        for (String segment : raw.split("<NL>")) {
            out.addAll(Minecraft.getInstance().font.split(
                    Component.literal(segment), JournalPage.DEFAULT_WIDTH));
            out.add(FormattedCharSequence.EMPTY);
        }
        return out;
    }

    @Override
    public void render(GuiGraphics graphics, int offsetX, int offsetY,
                       float partialTick, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;
        int y = offsetY;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, offsetX, y, 0x00CCCCCC, false);
            y += 10;
        }
    }
}
