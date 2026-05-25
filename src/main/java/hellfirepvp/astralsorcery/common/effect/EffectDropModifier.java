package hellfirepvp.astralsorcery.common.effect;

import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Drop Modifier — multiplies or voids the drops of a mob on death.
 * Amplifier 0 → void all drops; amplifier N → roll loot table N extra times.
 * Applied by the Fornax ritual effect.
 *
 * <p>The drop-modification logic lives in EventHandlerEffects.onDropModifier,
 * because MobEffect has no built-in drop hook in 1.20.</p>
 *
 * <p>1.16 → 1.20: drop hook moved to Forge LivingDropsEvent subscriber</p>
 */
public class EffectDropModifier extends EffectCustomTexture {

    public EffectDropModifier() {
        super(MobEffectCategory.BENEFICIAL, ColorsAS.EFFECT_DROP_MODIFIER.getRGB());
    }

    @Override
    @Nonnull
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }
}
