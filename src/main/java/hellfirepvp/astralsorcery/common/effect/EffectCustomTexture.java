package hellfirepvp.astralsorcery.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.Random;

/**
 * Base class for Astral Sorcery mob effects that use custom icon textures.
 * In 1.20.1 the vanilla sprite atlas automatically loads
 * {@code textures/mob_effect/<path>.png} for each registered effect,
 * so no custom render override is needed.
 *
 * <p>1.16 → 1.20: Effect → MobEffect, EffectType → MobEffectCategory.
 * SpriteSheetResource/SpriteQuery animated rendering dropped; static PNGs suffice.</p>
 */
public abstract class EffectCustomTexture extends MobEffect {

    protected static final Random rand = new Random();

    protected EffectCustomTexture(MobEffectCategory category, int color) {
        super(category, color);
    }
}
