package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageEmpty;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** A blank journal page. */
public class JournalPageEmpty implements JournalPage {

    public static final JournalPageEmpty INSTANCE = new JournalPageEmpty();

    private JournalPageEmpty() {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        return RenderPageEmpty.INSTANCE;
    }
}
