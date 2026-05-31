package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.mixin.accessor.LevelSkyDarkenAccessor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into {@link Level#updateSkyBrightness()} to apply solar eclipse
 * server-side sky darkening.
 *
 * <p>1.16 → 1.20: World.calculateInitialSkylight → Level.updateSkyBrightness;
 * skylightSubtracted field → skyDarken field.</p>
 */
@Mixin(Level.class)
public class MixinWorld {

    @Inject(method = "updateSkyBrightness", at = @At("RETURN"))
    public void solarEclipseSunBrightnessServer(CallbackInfo ci) {
        Level level = (Level)(Object) this;
        if (level.isClientSide()) {
            return;
        }

        WorldContext ctx = SkyHandler.getContext(level);
        if (ctx != null && ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            int darken = 11 - Math.round(ctx.getCelestialEventHandler().getSolarEclipsePercent() * 11F);
            ((LevelSkyDarkenAccessor) level).astralsorcery$setSkyDarken(darken);
        }
    }
}
