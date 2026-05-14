package hellfirepvp.astralsorcery.common.constellation.star;

import hellfirepvp.astralsorcery.common.util.data.BiDiPair;

/**
 * A connection between two stars in a constellation pattern.
 * Equality is bidirectional: (A→B) equals (B→A).
 *
 * <p>No 1.16 → 1.20 changes — pure Java.</p>
 */
public class StarConnection extends BiDiPair<StarLocation, StarLocation> {

    public final StarLocation from;
    public final StarLocation to;

    public StarConnection(StarLocation from, StarLocation to) {
        super(from, to);
        this.from = from;
        this.to = to;
    }
}
