package hellfirepvp.astralsorcery.common.util.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nonnull;

/**
 * A SoundEvent paired with a preferred SoundSource (category).
 * In 1.20, SoundEvent's constructor is private so we can no longer extend it.
 * This is now a composition wrapper instead of an inheritance subclass.
 *
 * <p>1.16 → 1.20 changes:
 * SoundCategory → SoundSource,
 * extends SoundEvent → wraps SoundEvent (constructor is private in 1.20)</p>
 */
public class CategorizedSoundEvent {

    @Nonnull
    private final SoundEvent event;
    @Nonnull
    private final SoundSource category;

    public CategorizedSoundEvent(@Nonnull SoundEvent event, @Nonnull SoundSource category) {
        this.event = event;
        this.category = category;
    }

    public static CategorizedSoundEvent of(@Nonnull ResourceLocation soundName,
                                           @Nonnull SoundSource category) {
        return new CategorizedSoundEvent(
                SoundEvent.createVariableRangeEvent(soundName), category);
    }

    @Nonnull
    public SoundEvent getEvent() {
        return event;
    }

    @Nonnull
    public SoundSource getCategory() {
        return category;
    }
}
