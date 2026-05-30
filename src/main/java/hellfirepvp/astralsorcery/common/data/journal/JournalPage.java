package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A single page of content within a {@link ResearchNode}'s journal entry.
 *
 * <p>Pages are associated with a specific node and page index.
 * The client-side {@link RenderablePage} is built on demand.</p>
 */
public interface JournalPage {

    int DEFAULT_WIDTH  = 175;
    int DEFAULT_HEIGHT = 220;

    @OnlyIn(Dist.CLIENT)
    RenderablePage buildRenderPage(ResearchNode node, int page);
}
