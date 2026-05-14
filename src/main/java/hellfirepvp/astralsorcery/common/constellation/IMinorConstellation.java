package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A minor constellation — appears only during specific moon phases,
 * used as trait modifiers on rituals and equipment.
 *
 * <p>1.16 → 1.20 changes:
 * ITextComponent → Component,
 * TranslationTextComponent → Component.translatable()</p>
 */
public interface IMinorConstellation extends IConstellation {

    @Nonnull
    List<MoonPhase> getShowupMoonPhases(long rSeed);

    @Nonnull
    default MutableComponent getInfoTraitEffect() {
        return Component.translatable(this.getTranslationKey() + ".trait");
    }

    default void affectConstellationEffect(@Nonnull ConstellationEffectProperties properties) {}
}
