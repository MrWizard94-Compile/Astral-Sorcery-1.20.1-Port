package hellfirepvp.astralsorcery.common.constellation;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * A constellation drawn at a specific position on the screen/paper.
 * Used by the journal and constellation paper rendering.
 *
 * <p>No 1.16 → 1.20 changes — pure Java data class.</p>
 */
public class DrawnConstellation {

    public static final int CONSTELLATION_DRAW_SIZE = 30;
    public static final float CONSTELLATION_SIZE_PART =
            (CONSTELLATION_DRAW_SIZE * 2F) / IConstellation.STAR_GRID_WIDTH_HEIGHT;
    public static final float CONSTELLATION_STAR_SIZE = CONSTELLATION_SIZE_PART * 2.5F;

    @Nonnull
    private final Point point;
    @Nonnull
    private final IConstellation constellation;

    public DrawnConstellation(@Nonnull Point point, @Nonnull IConstellation constellation) {
        this.point = point;
        this.constellation = constellation;
    }

    @Nonnull
    public IConstellation getConstellation() {
        return constellation;
    }

    @Nonnull
    public Point getPoint() {
        return point;
    }
}
