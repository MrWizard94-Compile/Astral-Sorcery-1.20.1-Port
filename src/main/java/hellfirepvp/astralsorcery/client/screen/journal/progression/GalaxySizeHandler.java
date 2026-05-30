package hellfirepvp.astralsorcery.client.screen.journal.progression;

import hellfirepvp.astralsorcery.client.screen.helper.SizeHandler;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Computes the bounding rectangle of all unlocked research clusters
 * for the top-level galaxy zoom view.
 */
@OnlyIn(Dist.CLIENT)
public class GalaxySizeHandler extends SizeHandler {

    @Override
    @Nullable
    public float[] buildRequiredRectangle() {
        float leftMost = 0, rightMost = 0, upperMost = 0, lowerMost = 0;

        PlayerProgress progress = ResearchHelper.getClientProgress();
        for (ResearchProgression prog : progress.getResearchProgression()) {
            JournalCluster cluster = JournalProgressionClusterMapping.getClusterMapping(prog);
            if (cluster.x      < leftMost)  leftMost  = cluster.x;
            if (cluster.x      > rightMost) rightMost = cluster.x;
            if (cluster.y      > lowerMost) lowerMost = cluster.y;
            if (cluster.y      < upperMost) upperMost = cluster.y;
            if (cluster.maxX   < leftMost)  leftMost  = cluster.maxX;
            if (cluster.maxX   > rightMost) rightMost = cluster.maxX;
            if (cluster.maxY   > lowerMost) lowerMost = cluster.maxY;
            if (cluster.maxY   < upperMost) upperMost = cluster.maxY;
        }
        return new float[] { leftMost, rightMost, upperMost, lowerMost };
    }
}
