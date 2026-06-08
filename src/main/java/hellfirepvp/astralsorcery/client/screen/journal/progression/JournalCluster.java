package hellfirepvp.astralsorcery.client.screen.journal.progression;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;

import java.awt.Rectangle;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalCluster other)) return false;
        return super.equals(o)
                && maxX == other.maxX
                && maxY == other.maxY
                && Objects.equals(cloudTexture, other.cloudTexture)
                && Objects.equals(clusterBackgroundTexture, other.clusterBackgroundTexture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cloudTexture, clusterBackgroundTexture, maxX, maxY);
    }
}
