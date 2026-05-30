package hellfirepvp.astralsorcery.common.base;

import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.BindableResource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public BindableResource getTexture() {
        String name = switch (this) {
            case FULL        -> "moon_full";
            case NEW         -> "moon_new";
            case WANING_3_4  -> "moon_waning_3_4";
            case WANING_1_2  -> "moon_waning_1_2";
            case WANING_1_4  -> "moon_waning_1_4";
            case WAXING_1_4  -> "moon_waxing_1_4";
            case WAXING_1_2  -> "moon_waxing_1_2";
            case WAXING_3_4  -> "moon_waxing_3_4";
        };
        return AssetLoader.loadTexture(AssetLoader.TextureLocation.MISC, name);
    }
}
