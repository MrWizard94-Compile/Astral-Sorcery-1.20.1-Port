package hellfirepvp.astralsorcery.common.data.research;

import javax.annotation.Nonnull;

/**
 * Stub for player research/progression data.
 * Full implementation deferred to Phase 9 (Perk & Attunement System).
 *
 * <p>This stub provides the minimal API used by constellation discovery checks.</p>
 */
public class PlayerProgress {

    @Nonnull
    private ProgressionTier tierReached = ProgressionTier.DISCOVERY;
    private boolean onceAttuned = false;

    @Nonnull
    public ProgressionTier getTierReached() {
        return tierReached;
    }

    public void setTierReached(@Nonnull ProgressionTier tier) {
        this.tierReached = tier;
    }

    public boolean wasOnceAttuned() {
        return onceAttuned;
    }

    public void setOnceAttuned(boolean attuned) {
        this.onceAttuned = attuned;
    }
}
