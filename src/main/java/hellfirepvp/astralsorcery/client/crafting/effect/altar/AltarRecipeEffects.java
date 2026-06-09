package hellfirepvp.astralsorcery.client.crafting.effect.altar;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Registry of the built-in {@link AltarRecipeEffect} singletons.
 * Each effect is a stateless object (effects use no instance fields) so
 * sharing a single instance per type is safe.
 *
 * <p>Effects are assigned to recipes in the altar recipe JSON via the
 * {@code "effect"} field. The {@link hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe}
 * serializer maps the string key to the corresponding singleton here.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class AltarRecipeEffects {

    private AltarRecipeEffects() {}

    public static final EffectAltarDefaultSparkle     DEFAULT_SPARKLE        = new EffectAltarDefaultSparkle();
    public static final EffectAltarRandomSparkle      RANDOM_SPARKLE         = new EffectAltarRandomSparkle();
    public static final EffectAltarDefaultLightbeams  DEFAULT_LIGHTBEAMS     = new EffectAltarDefaultLightbeams();
    public static final EffectAltarFocusSparkle       FOCUS_SPARKLE          = new EffectAltarFocusSparkle();
    public static final EffectPillarSparkle           PILLAR_SPARKLE         = new EffectPillarSparkle();
    public static final EffectPillarLightbeams        PILLAR_LIGHTBEAMS      = new EffectPillarLightbeams();
    public static final EffectFocusDustSwirl          FOCUS_DUST_SWIRL       = new EffectFocusDustSwirl();
    public static final EffectLargeDustSwirl          LARGE_DUST_SWIRL       = new EffectLargeDustSwirl();
    public static final EffectFocusEdge               FOCUS_EDGE             = new EffectFocusEdge();
    public static final EffectGatewayEdge             GATEWAY_EDGE           = new EffectGatewayEdge();
    public static final EffectLiquidBurst             LIQUID_BURST           = new EffectLiquidBurst();
    public static final EffectLuminescenceFlare       LUMINESCENCE_FLARE     = new EffectLuminescenceFlare();
    public static final EffectVortexPlane             VORTEX_PLANE           = new EffectVortexPlane();
    public static final EffectUpgradeAltar            UPGRADE_ALTAR          = new EffectUpgradeAltar();

    public static final BuiltInEffectDiscoveryCentralBeam  DISCOVERY_BEAM         = new BuiltInEffectDiscoveryCentralBeam();
    public static final BuiltInEffectAttunementSparkle     ATTUNEMENT_SPARKLE     = new BuiltInEffectAttunementSparkle();
    public static final BuiltInEffectConstellationLines    CONSTELLATION_LINES    = new BuiltInEffectConstellationLines();
    public static final BuiltInEffectConstellationFinish   CONSTELLATION_FINISH   = new BuiltInEffectConstellationFinish();
    public static final BuiltInEffectTraitFocusCircle      TRAIT_FOCUS_CIRCLE     = new BuiltInEffectTraitFocusCircle();
    public static final BuiltInEffectTraitRelayHighlight   TRAIT_RELAY_HIGHLIGHT  = new BuiltInEffectTraitRelayHighlight();

    /**
     * Returns the default composite effect for a given altar tier.
     * Used when a recipe does not specify a custom effect.
     */
    @Nullable
    public static AltarRecipeEffect getDefaultEffect(BlockAltar.AltarType tier) {
        return switch (tier) {
            case DISCOVERY    -> DISCOVERY_BEAM;
            case ATTUNEMENT   -> ATTUNEMENT_SPARKLE;
            case CONSTELLATION -> CONSTELLATION_LINES;
            case RADIANCE     -> TRAIT_FOCUS_CIRCLE;
        };
    }
}
