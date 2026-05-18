package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A weak constellation — has ritual effects and mantle effects.
 * Major constellations extend this.
 *
 * <p>1.16 → 1.20 changes:
 * ITextComponent → Component,
 * TranslationTextComponent → Component.translatable(),
 * Registry lookups deferred until registries are populated</p>
 */
public interface IWeakConstellation extends IConstellation {

    @Nullable
    default ConstellationEffectProvider getConstellationEffect() {
        return ConstellationEffectRegistry.getProvider(this);
    }

    @Nullable
    default MantleEffect getMantleEffect() {
        return MantleEffectRegistry.getEffect(this);
    }

    @Nonnull
    default MutableComponent getInfoRitualEffect() {
        return Component.translatable(this.getTranslationKey() + ".ritual");
    }

    @Nonnull
    default MutableComponent getInfoCorruptedRitualEffect() {
        return Component.translatable(this.getTranslationKey() + ".corruption");
    }

    @Nonnull
    default MutableComponent getInfoMantleEffect() {
        return Component.translatable(this.getTranslationKey() + ".mantle");
    }
}
