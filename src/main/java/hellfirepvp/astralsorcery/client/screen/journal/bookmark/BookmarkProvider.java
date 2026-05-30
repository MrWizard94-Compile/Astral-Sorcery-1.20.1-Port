package hellfirepvp.astralsorcery.client.screen.journal.bookmark;

import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * A tab bookmark shown on the right edge of any {@link hellfirepvp.astralsorcery.client.screen.journal.ScreenJournal}
 * screen. Clicking it navigates to that bookmark's screen.
 *
 * <p>1.16 → 1.20: IFormattableTextComponent → Component; TranslationTextComponent → Component.translatable().</p>
 */
@OnlyIn(Dist.CLIENT)
public class BookmarkProvider {

    private final Supplier<Screen> provider;
    private final int index;
    private final Component displayName;
    private final Supplier<Boolean> canSeeTest;

    public BookmarkProvider(String translationKey, int bookmarkIndex,
                             Supplier<Screen> guiProvider,
                             Supplier<Boolean> canSeeTest) {
        this.displayName = Component.translatable(translationKey);
        this.index = bookmarkIndex;
        this.provider = guiProvider;
        this.canSeeTest = canSeeTest;
    }

    public Screen getGuiScreen() {
        return provider.get();
    }

    public boolean canSee() {
        return canSeeTest.get();
    }

    public int getIndex() {
        return index;
    }

    public Component getUnlocalizedName() {
        return displayName;
    }

    public AbstractRenderableTexture getTextureBookmark() {
        return TexturesAS.TEX_GUI_BOOKMARK;
    }

    public AbstractRenderableTexture getTextureBookmarkStretched() {
        return TexturesAS.TEX_GUI_BOOKMARK_STRETCHED;
    }
}
