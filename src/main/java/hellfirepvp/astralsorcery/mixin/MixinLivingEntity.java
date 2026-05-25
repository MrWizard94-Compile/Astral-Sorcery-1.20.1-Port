package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectOctans;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(
        method = "getWaterSlowDown",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    public void preventWaterSlowdown(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity)(Object) this;
        if (MantleEffectOctans.shouldPreventWaterSlowdown(entity)) {
            cir.setReturnValue(0.92F);
        }
    }
}