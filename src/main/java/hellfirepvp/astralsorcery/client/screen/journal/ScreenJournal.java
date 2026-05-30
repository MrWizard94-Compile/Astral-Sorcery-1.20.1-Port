package hellfirepvp.astralsorcery.client.screen.journal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.screen.journal.bookmark.BookmarkProvider;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Base class for all journal screens. Renders the shared right-side bookmark
 * tab strip and handles click-navigation between journal sub-screens.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; displayGuiScreen → setScreen;
 * IFormattableTextComponent → Component.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournal extends WidthHeightScreen {

    public static final int NO_BOOKMARK = -1;
    protected static List<BookmarkProvider> bookmarks = Lists.newArrayList();

    protected final int bookmarkIndex;
    protected Map<Rectangle, BookmarkProvider> drawnBookmarks = Maps.newHashMap();

    protected ScreenJournal(Component title, int bookmarkIndex) {
        this(title, 270, 420, bookmarkIndex);
    }

    public ScreenJournal(Component title, int guiHeight, int guiWidth, int bookmarkIndex) {
        super(title, guiHeight, guiWidth);
        this.bookmarkIndex = bookmarkIndex;
    }

    public static boolean addBookmark(BookmarkProvider bookmarkProvider) {
        int index = bookmarkProvider.getIndex();
        for (BookmarkProvider bm : bookmarks) {
            if (bm.getIndex() == index) return false;
        }
        bookmarks.add(bookmarkProvider);
        return true;
    }

    protected void drawDefault(GuiGraphics graphics, AbstractRenderableTexture texture, int mouseX, int mouseY) {
        drawWHRect(graphics, texture);
        drawBookmarks(graphics, mouseX, mouseY);
    }

    private void drawBookmarks(GuiGraphics graphics, int mouseX, int mouseY) {
        drawnBookmarks.clear();

        int bookmarkWidth  = 67;
        int bookmarkHeight = 15;
        float bookmarkGap  = 18;

        float offsetX = guiLeft + guiWidth - 17.25f;
        float offsetY = guiTop + 20;

        bookmarks.sort(Comparator.comparing(BookmarkProvider::getIndex));

        for (BookmarkProvider bm : bookmarks) {
            if (bm.canSee()) {
                Rectangle r = drawBookmark(graphics, offsetX, offsetY,
                        bookmarkWidth, bookmarkHeight,
                        bookmarkWidth + (bookmarkIndex == bm.getIndex() ? 0 : 5),
                        bm.getUnlocalizedName(), 0xDDDDDDDD, mouseX, mouseY,
                        bm.getTextureBookmark(), bm.getTextureBookmarkStretched());
                drawnBookmarks.put(r, bm);
                offsetY += bookmarkGap;
            }
        }
    }

    private Rectangle drawBookmark(GuiGraphics graphics,
                                   float offsetX, float offsetY,
                                   int width, int height, int mouseOverWidth,
                                   @Nonnull Component title, int titleRGBColor,
                                   int mouseX, int mouseY,
                                   AbstractRenderableTexture texture,
                                   AbstractRenderableTexture textureStretched) {
        Rectangle r = new Rectangle(Mth.floor(offsetX), Mth.floor(offsetY), width, height);
        AbstractRenderableTexture tex = texture;
        if (r.contains(mouseX, mouseY)) {
            if (mouseOverWidth > width) {
                tex = textureStretched;
            }
            width = mouseOverWidth;
            r = new Rectangle(Mth.floor(offsetX), Mth.floor(offsetY), width, height);
        }

        RenderingDrawUtils.drawTexturedRect(graphics, tex.getKey(),
                Mth.floor(offsetX), Mth.floor(offsetY), width, height);

        float scale = 0.7f;
        int textX = Mth.floor(offsetX + 2 + 2 / scale);
        int textY = Mth.floor(offsetY + 4 + 4 / scale);
        graphics.pose().pushPose();
        graphics.pose().translate(offsetX + 2, offsetY + 4, 50);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(Minecraft.getInstance().font, title, 0, 0, titleRGBColor, false);
        graphics.pose().popPose();

        return r;
    }

    protected boolean handleBookmarkClick(double mouseX, double mouseY) {
        for (Map.Entry<Rectangle, BookmarkProvider> entry : drawnBookmarks.entrySet()) {
            BookmarkProvider provider = entry.getValue();
            if (bookmarkIndex != provider.getIndex() && entry.getKey().contains((int) mouseX, (int) mouseY)) {
                ScreenJournalProgression.resetJournal();
                Minecraft.getInstance().setScreen(provider.getGuiScreen());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
