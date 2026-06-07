package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageStructure;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Journal page that displays a structure schematic via the 3D isometric renderer.
 */
public class JournalPageStructure implements JournalPage {

    @Nullable
    private final StructureTemplate structure;

    public JournalPageStructure(@Nullable StructureTemplate structure) {
        this.structure = structure;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        return new RenderPageStructure(structure);
    }
}
