package hellfirepvp.astralsorcery.common.data.research;

/**
 * The player's progression tier through Astral Sorcery's content.
 * Each tier unlocks access to new constellations and mechanics.
 *
 * <p>No 1.16 → 1.20 changes — pure Java enum.</p>
 */
public enum ProgressionTier {

    DISCOVERY,
    BASIC_CRAFT,
    ATTUNEMENT,
    CONSTELLATION_CRAFT,
    TRAIT_CRAFT,
    BRILLIANCE;

    public boolean hasNextTier() {
        return ordinal() < values().length - 1;
    }

    public ProgressionTier next() {
        return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    public boolean isThisLaterOrEqual(ProgressionTier other) {
        return this.ordinal() >= other.ordinal();
    }

    public boolean isThisLater(ProgressionTier other) {
        return this.ordinal() > other.ordinal();
    }
}
