package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageEmpty;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Journal page that displays a structure schematic.
 * Full 3D rendering is deferred — returns an empty page until RenderPageStructure is ported.
 *
 * <p>1.16 → 1.20: StructurePlacementData removed; StructureTemplate API stable.
 * RenderPageStructure not yet ported — falls back to empty.</p>
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
        // RenderPageStructure not yet ported — return blank page
        return RenderPageEmpty.INSTANCE;
    }
}
