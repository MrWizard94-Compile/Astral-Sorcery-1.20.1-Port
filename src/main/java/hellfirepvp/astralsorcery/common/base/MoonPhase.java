package hellfirepvp.astralsorcery.common.base;

import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Moon phase enum (8 phases). Determines constellation visibility
 * and various celestial mechanics.
 *
 * <p>1.16 → 1.20 change: replaced obfuscated world.func_241851_ab() + getDimensionType().getMoonPhase()
 * with Level.getMoonPhase() which returns 0-7 directly.</p>
 */
public enum MoonPhase {

    FULL,
    WANING_3_4,
    WANING_1_2,
    WANING_1_4,
    NEW,
    WAXING_1_4,
    WAXING_1_2,
    WAXING_3_4;

    private static final MoonPhase[] VALUES = values();

    @Nonnull
    public static MoonPhase fromWorld(@Nonnull Level level) {
        int phase = level.getMoonPhase();
        if (phase < 0 || phase >= VALUES.length) {
            return FULL;
        }
        return VALUES[phase];
    }

    // Client-side texture method will be added in Phase 12 (rendering)
    // when AssetLibrary and AbstractRenderableTexture are ported.
}
