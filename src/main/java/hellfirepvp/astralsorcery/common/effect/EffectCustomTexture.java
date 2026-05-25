package hellfirepvp.astralsorcery.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.Random;

/**
 * Base class for Astral Sorcery mob effects that use custom icon textures.
 * Client-side icon rendering is deferred to Phase 12.
 *
 * <p>1.16 → 1.20: Effect → MobEffect, EffectType → MobEffectCategory,
 * client render methods deferred</p>
 */
public abstract class EffectCustomTexture extends MobEffect {

    protected static final Random rand = new Random();

    protected EffectCustomTexture(MobEffectCategory category, int color) {
        super(category, color);
    }
}
