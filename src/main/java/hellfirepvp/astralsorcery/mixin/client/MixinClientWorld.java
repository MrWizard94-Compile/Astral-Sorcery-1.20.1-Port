package hellfirepvp.astralsorcery.mixin.client;

import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks into {@link ClientLevel#getSkyDarken(float)} to apply solar eclipse
 * client-side sun brightness reduction.
 *
 * <p>1.16 → 1.20: ClientWorld.getSunBrightness → ClientLevel.getSkyDarken.</p>
 */
@OnlyIn(Dist.CLIENT)
@Mixin(ClientLevel.class)
public class MixinClientWorld {

    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true, remap = false)
    public void solarEclipseSunBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        ClientLevel world = (ClientLevel)(Object) this;

        WorldContext ctx = SkyHandler.getContext(world);
        if (ctx != null && ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            float perc = ctx.getCelestialEventHandler().getSolarEclipsePercent();
            perc = 0.05F + (perc * 0.95F);
            cir.setReturnValue(cir.getReturnValueF() * perc);
        }
    }
}
