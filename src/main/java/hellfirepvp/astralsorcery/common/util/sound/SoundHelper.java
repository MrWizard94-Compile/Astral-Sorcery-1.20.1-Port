package hellfirepvp.astralsorcery.common.util.sound;

import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Common (server-compatible) sound playback utilities.
 * Client-only loop/fade methods live in
 * {@link hellfirepvp.astralsorcery.client.util.sound.ClientSoundHelper}.
 *
 * <p>1.16 → 1.20 changes:
 * SoundCategory → SoundSource, World → Level, Vector3i → Vec3i</p>
 */
public class SoundHelper {

    public static void playSoundAround(@Nonnull SoundEvent sound, @Nonnull Level level,
                                       @Nonnull Vec3i position,
                                       float volume, float pitch) {
        playSoundAround(sound, SoundSource.MASTER, level,
                position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(@Nonnull SoundEvent sound, @Nonnull SoundSource category,
                                       @Nonnull Level level, @Nonnull Vec3i position,
                                       float volume, float pitch) {
        playSoundAround(sound, category, level,
                position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(@Nonnull SoundEvent sound, @Nonnull SoundSource category,
                                       @Nonnull Level level,
                                       double posX, double posY, double posZ,
                                       float volume, float pitch) {
        level.playSound(null, posX, posY, posZ, sound, category, volume, pitch);
    }

    /**
     * Play a CategorizedSoundEvent using its embedded category.
     */
    public static void playSoundAround(@Nonnull CategorizedSoundEvent sound,
                                       @Nonnull Level level, @Nonnull Vec3i position,
                                       float volume, float pitch) {
        playSoundAround(sound.getEvent(), sound.getCategory(), level,
                position.getX(), position.getY(), position.getZ(), volume, pitch);
    }
}
