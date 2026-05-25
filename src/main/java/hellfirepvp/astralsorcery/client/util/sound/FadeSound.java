package hellfirepvp.astralsorcery.client.util.sound;

import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;

/**
 * A one-shot positioned sound with fade-in and fade-out.
 * Like {@link FadeLoopSound} but does not loop — suitable for sustained effects.
 * Predicate convention: returning {@code true} triggers the fade-out.
 */
@OnlyIn(Dist.CLIENT)
public class FadeSound extends AbstractTickableSoundInstance {

    private Predicate<FadeSound> func = null;
    private float volumeMultiplier = 1F;

    private float fadeInTicks = 40;
    private float fadeOutTicks = 1;

    private int ticks = 0;
    private int stopTick = 0;
    private boolean shouldStop = false;

    public FadeSound(CategorizedSoundEvent sound, float volume, float pitch,
                     Vec3 pos, boolean isGlobal) {
        this(sound.getEvent(), sound.getCategory(), volume, pitch, pos, isGlobal);
    }

    public FadeSound(SoundEvent sound, SoundSource source, float volume, float pitch,
                     Vec3 pos, boolean isGlobal) {
        super(sound, source, RandomSource.create());
        this.volume = volume;
        this.pitch = pitch;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.looping = true;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.relative = isGlobal;
    }

    public void setRefreshFunction(Predicate<FadeSound> func) {
        this.func = func;
    }

    @SuppressWarnings("unchecked")
    public <T extends FadeSound> T setFadeInTicks(float t) {
        this.fadeInTicks = t;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends FadeSound> T setFadeOutTicks(float t) {
        this.fadeOutTicks = t;
        return (T) this;
    }

    @Override
    public void tick() {
        ticks++;
        if (!shouldStop && func != null && func.test(this)) {
            shouldStop = true;
        }
        if (shouldStop) {
            stopTick++;
            if (stopTick > fadeOutTicks) {
                stop();
            }
        }
    }

    public boolean hasStoppedPlaying() {
        return isStopped() || !Minecraft.getInstance().getSoundManager().isActive(this);
    }

    public void setVolumeMultiplier(float multiplier) {
        this.volumeMultiplier = Mth.clamp(multiplier, 0F, 1F);
    }

    @Override
    public float getVolume() {
        float mulFadeIn = Mth.clamp(ticks / fadeInTicks, 0F, 1F);
        float mulFadeOut = Mth.clamp(1F - stopTick / fadeOutTicks, 0F, 1F);
        return mulFadeIn * mulFadeOut * super.getVolume() * volumeMultiplier;
    }
}
