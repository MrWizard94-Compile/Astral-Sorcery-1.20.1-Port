package hellfirepvp.astralsorcery.common.auxiliary.book;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Holds a pointer to a specific page within a {@link ResearchNode}
 * that can be opened directly (e.g. when right-clicking an item that
 * has a journal entry).
 *
 * <p>1.16 → 1.20: displayGuiScreen → setScreen; hasResearch now delegates to
 * {@link PlayerProgress#hasResearch(ResearchProgression)}.</p>
 */
public class BookLookupInfo {

    private final ResearchNode node;
    private final int pageIndex;
    private final ResearchProgression neededKnowledge;

    public BookLookupInfo(ResearchNode node, int pageIndex, ResearchProgression neededKnowledge) {
        this.node = node;
        this.pageIndex = pageIndex;
        this.neededKnowledge = neededKnowledge;
    }

    public ResearchNode getResearchNode() {
        return node;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public boolean canSee(PlayerProgress progress) {
        return this.getResearchNode().canSee(progress) && progress.hasResearch(this.neededKnowledge);
    }

    @OnlyIn(Dist.CLIENT)
    public void openGui() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPages(
                mc.screen, this.getResearchNode(), this.getPageIndex()));
    }
}
