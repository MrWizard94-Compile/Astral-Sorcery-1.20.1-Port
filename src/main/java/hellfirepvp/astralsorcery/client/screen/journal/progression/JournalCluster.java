package hellfirepvp.astralsorcery.client.screen.journal.progression;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;

import java.awt.*;

/**
 * Defines a rectangular cluster region in the journal galaxy view.
 * Coordinates use a logical grid (not pixels), translated by {@link SizeHandler}.
 */
public class JournalCluster extends Rectangle {

    public final AbstractRenderableTexture cloudTexture;
    public final AbstractRenderableTexture clusterBackgroundTexture;
    public int maxX, maxY;

    public JournalCluster(AbstractRenderableTexture cloudTexture,
                          AbstractRenderableTexture clusterBackgroundTexture,
                          int leftMost, int upperMost, int rightMost, int lowerMost) {
        super(leftMost, upperMost, rightMost - leftMost, lowerMost - upperMost);
        this.cloudTexture = cloudTexture;
        this.clusterBackgroundTexture = clusterBackgroundTexture;
        this.maxX = rightMost;
        this.maxY = lowerMost;
    }
}
