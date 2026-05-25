package hellfirepvp.astralsorcery.common.effect;

import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.util.DamageSourceAS;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

/**
 * Bleed — deals periodic physical damage to the target.
 * Applied by the Discidia mantle effect and the KeyBleed perk.
 *
 * <p>1.16 → 1.20: performEffect → applyEffectTick, isReady → isDurationEffectTick,
 * DamageSource now data-driven via DamageTypesAS.BLEED</p>
 */
public class EffectBleed extends EffectCustomTexture {

    public EffectBleed() {
        super(MobEffectCategory.HARMFUL, ColorsAS.EFFECT_BLEED.getRGB());
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity entity, int amplifier) {
        // Skip players when PvP is disabled on the server
        if (entity instanceof Player
                && entity.level() instanceof ServerLevel serverLevel
                && !serverLevel.getServer().isPvpAllowed()) {
            return;
        }
        DamageUtil.shotgunAttack(entity, e ->
                DamageUtil.attackEntityFrom(e, DamageSourceAS.bleed(e.level()), 0.5F * (amplifier + 1)));
    }
}
