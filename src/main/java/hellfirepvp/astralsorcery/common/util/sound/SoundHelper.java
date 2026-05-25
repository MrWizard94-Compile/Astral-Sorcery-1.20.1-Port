package hellfirepvp.astralsorcery.common.util.sound;

import hellfirepvp.astralsorcery.client.util.sound.FadeLoopSound;
import hellfirepvp.astralsorcery.client.util.sound.FadeSound;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Sound playback utilities for server and client side.
 * Client-side loop/fade sound factory methods delegate to PositionedLoopSound, FadeLoopSound, FadeSound.
 *
 * <p>1.16 → 1.20 changes:
 * SoundCategory → SoundSource, World → Level, Vector3i → Vec3i,
 * ClientPlayerEntity → LocalPlayer,
 * getSoundHandler → getSoundManager,
 * gameSettings.getSoundLevel → options.getSoundSourceVolume,
 * Minecraft.getInstance().world → level</p>
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

    @OnlyIn(Dist.CLIENT)
    public static PositionedLoopSound playSoundLoopClient(@Nonnull CategorizedSoundEvent sound,
                                                          @Nonnull Vec3 pos,
                                                          float volume, float pitch,
                                                          boolean isGlobal,
                                                          @Nonnull java.util.function.Predicate<PositionedLoopSound> func) {
        PositionedLoopSound s = new PositionedLoopSound(sound, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeLoopSound playSoundLoopFadeInClient(@Nonnull CategorizedSoundEvent sound,
                                                          @Nonnull Vec3 pos,
                                                          float volume, float pitch,
                                                          boolean isGlobal,
                                                          @Nonnull java.util.function.Predicate<PositionedLoopSound> func) {
        FadeLoopSound s = new FadeLoopSound(sound, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeSound playSoundFadeInClient(@Nonnull CategorizedSoundEvent sound,
                                                  @Nonnull Vec3 pos,
                                                  float volume, float pitch,
                                                  boolean isGlobal,
                                                  @Nonnull java.util.function.Predicate<FadeSound> func) {
        FadeSound s = new FadeSound(sound, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeLoopSound playSoundLoopFadeInClient(@Nonnull SoundEvent sound,
                                                          @Nonnull SoundSource source,
                                                          @Nonnull Vec3 pos,
                                                          float volume, float pitch,
                                                          boolean isGlobal,
                                                          @Nonnull java.util.function.Predicate<PositionedLoopSound> func) {
        FadeLoopSound s = new FadeLoopSound(sound, source, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    @OnlyIn(Dist.CLIENT)
    public static float getSoundVolume(@Nonnull SoundSource cat) {
        return Minecraft.getInstance().options.getSoundSourceVolume(cat);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClient(@Nonnull SoundEvent sound, float volume, float pitch) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(sound, volume, pitch);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClientWorld(@Nonnull CategorizedSoundEvent sound,
                                            @Nonnull BlockPos pos,
                                            float volume, float pitch) {
        playSoundClientWorld(sound.getEvent(), sound.getCategory(), pos, volume, pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClientWorld(@Nonnull SoundEvent sound, @Nonnull SoundSource cat,
                                            @Nonnull BlockPos pos,
                                            float volume, float pitch) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().level.playSound(
                    Minecraft.getInstance().player,
                    pos.getX(), pos.getY(), pos.getZ(),
                    sound, cat, volume, pitch);
        }
    }
}
