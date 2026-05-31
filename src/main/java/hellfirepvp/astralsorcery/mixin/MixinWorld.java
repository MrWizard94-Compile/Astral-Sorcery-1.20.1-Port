package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow(remap = false) private int skyDarken;

    @Inject(method = "updateSkyBrightness", at = @At("RETURN"), remap = false)
    public void solarEclipseSunBrightnessServer(CallbackInfo ci) {
        Level level = (Level)(Object) this;
        if (level.isClientSide()) {
            return;
        }

        WorldContext ctx = SkyHandler.getContext(level);
        if (ctx != null && ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            this.skyDarken = 11 - Math.round(ctx.getCelestialEventHandler().getSolarEclipsePercent() * 11F);
        }
    }
}
