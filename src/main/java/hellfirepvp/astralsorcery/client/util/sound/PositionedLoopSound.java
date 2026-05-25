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
 * A looping sound anchored at a world position.
 * Stops when the supplied predicate returns true (or when no predicate is set).
 * Predicate convention: returning {@code true} stops the sound.
 *
 * <p>1.16 → 1.20: SimpleSound + ITickableSound → AbstractTickableSoundInstance;
 * getSoundHandler → getSoundManager; SoundCategory → SoundSource;
 * Vector3 → Vec3; MathHelper.clamp → Mth.clamp;
 * stopped field is private — use stop() method.</p>
 */
@OnlyIn(Dist.CLIENT)
public class PositionedLoopSound extends AbstractTickableSoundInstance {

    protected Predicate<PositionedLoopSound> func = null;
    private float volumeMultiplier = 1F;

    public PositionedLoopSound(CategorizedSoundEvent sound, float volume, float pitch,
                               Vec3 pos, boolean isGlobal) {
        this(sound.getEvent(), sound.getCategory(), volume, pitch, pos, isGlobal);
    }

    public PositionedLoopSound(SoundEvent sound, SoundSource source, float volume, float pitch,
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

    public void setRefreshFunction(Predicate<PositionedLoopSound> func) {
        this.func = func;
    }

    @Override
    public void tick() {
        if (isStopped()) return;
        if (func != null && func.test(this)) {
            stop();
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
        return super.getVolume() * volumeMultiplier;
    }
}
