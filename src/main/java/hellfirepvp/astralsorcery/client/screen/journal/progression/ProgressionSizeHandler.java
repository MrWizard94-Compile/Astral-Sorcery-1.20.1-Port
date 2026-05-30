package hellfirepvp.astralsorcery.client.screen.journal.progression;

import hellfirepvp.astralsorcery.client.screen.helper.SizeHandler;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/** Computes the node bounding rectangle for a single research cluster's zoom view. */
@OnlyIn(Dist.CLIENT)
public class ProgressionSizeHandler extends SizeHandler {

    private final ResearchProgression part;

    public ProgressionSizeHandler(ResearchProgression part) {
        this.part = part;
    }

    @Override
    @Nullable
    public float[] buildRequiredRectangle() {
        float leftMost = 0, rightMost = 0, upperMost = 0, lowerMost = 0;
        for (ResearchNode node : part.getResearchNodes()) {
            float x = node.renderPosX;
            float y = node.renderPosZ;
            if (x < leftMost)  leftMost  = x;
            if (x > rightMost) rightMost = x;
            if (y > lowerMost) lowerMost = y;
            if (y < upperMost) upperMost = y;
        }
        return new float[] { leftMost, rightMost, upperMost, lowerMost };
    }
}
