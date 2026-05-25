package hellfirepvp.astralsorcery.client.util.sound;

import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A looping positioned sound that fades in on start and fades out when stopped.
 * The sound won't actually stop until the fade-out completes.
 */
@OnlyIn(Dist.CLIENT)
public class FadeLoopSound extends PositionedLoopSound {

    private float fadeInTicks = 40;
    private float fadeOutTicks = 1;

    private int ticks = 0;
    private int stopTick = 0;
    private boolean shouldStop = false;

    public FadeLoopSound(CategorizedSoundEvent sound, float volume, float pitch,
                         Vec3 pos, boolean isGlobal) {
        super(sound, volume, pitch, pos, isGlobal);
    }

    public FadeLoopSound(SoundEvent sound, SoundSource source, float volume, float pitch,
                         Vec3 pos, boolean isGlobal) {
        super(sound, source, volume, pitch, pos, isGlobal);
    }

    @SuppressWarnings("unchecked")
    public <T extends FadeLoopSound> T setFadeInTicks(float t) {
        this.fadeInTicks = t;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends FadeLoopSound> T setFadeOutTicks(float t) {
        this.fadeOutTicks = t;
        return (T) this;
    }

    @Override
    public void tick() {
        ticks++;
        // Check stop predicate directly (don't call super.tick() which would call stop())
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

    @Override
    public float getVolume() {
        float mulFadeIn = Mth.clamp(ticks / fadeInTicks, 0F, 1F);
        float mulFadeOut = Mth.clamp(1F - stopTick / fadeOutTicks, 0F, 1F);
        return mulFadeIn * mulFadeOut * super.getVolume();
    }
}
