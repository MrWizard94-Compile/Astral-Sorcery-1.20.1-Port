package hellfirepvp.astralsorcery.common.constellation.star;

import java.awt.*;

/**
 * A star position on the 32x32 constellation grid (0-indexed, max index 31).
 *
 * <p>No 1.16 → 1.20 changes — pure Java.</p>
 */
public class StarLocation {

    public final int x;
    public final int y;

    public StarLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getDistanceToOrigin() {
        return x + y;
    }

    public Point asPoint() {
        return new Point(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StarLocation tuple = (StarLocation) o;
        return x == tuple.x && y == tuple.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
