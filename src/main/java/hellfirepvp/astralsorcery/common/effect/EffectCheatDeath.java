package hellfirepvp.astralsorcery.common.effect;

import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Cheat Death — cancels the killing blow, restores health, blasts nearby entities.
 * Applied by the Pelotrio ritual effect.
 *
 * <p>The death-cancel logic lives in EventHandlerEffects.onCheatDeath,
 * because MobEffect has no built-in death hook in 1.20.</p>
 *
 * <p>1.16 → 1.20: death hook moved to Forge LivingDeathEvent subscriber</p>
 */
public class EffectCheatDeath extends EffectCustomTexture {

    public EffectCheatDeath() {
        super(MobEffectCategory.BENEFICIAL, ColorsAS.EFFECT_CHEAT_DEATH.getRGB());
    }

    @Override
    @Nonnull
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }
}
