package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageText;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Journal page that renders a localized text block. */
public class JournalPageText implements JournalPage {

    private final String translationKey;

    public JournalPageText(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        return new RenderPageText(translationKey);
    }
}
