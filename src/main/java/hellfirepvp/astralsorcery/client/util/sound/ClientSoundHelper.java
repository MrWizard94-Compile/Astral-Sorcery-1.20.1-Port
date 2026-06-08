package hellfirepvp.astralsorcery.client.util.sound;

import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Client-only sound factory methods for looping and fade sounds.
 * Common (server-compatible) sound methods remain in
 * {@link hellfirepvp.astralsorcery.common.util.sound.SoundHelper}.
 */
public class ClientSoundHelper {

    public static PositionedLoopSound playSoundLoopClient(@Nonnull CategorizedSoundEvent sound,
                                                          @Nonnull Vec3 pos,
                                                          float volume, float pitch,
                                                          boolean isGlobal,
                                                          @Nonnull Predicate<PositionedLoopSound> func) {
        PositionedLoopSound s = new PositionedLoopSound(sound, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    public static FadeLoopSound playSoundLoopFadeInClient(@Nonnull CategorizedSoundEvent sound,
                                                          @Nonnull Vec3 pos,
                                                          float volume, float pitch,
                                                          boolean isGlobal,
                                                          @Nonnull Predicate<PositionedLoopSound> func) {
        FadeLoopSound s = new FadeLoopSound(sound, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    public static FadeSound playSoundFadeInClient(@Nonnull CategorizedSoundEvent sound,
                                                  @Nonnull Vec3 pos,
                                                  float volume, float pitch,
                                                  boolean isGlobal,
                                                  @Nonnull Predicate<FadeSound> func) {
        FadeSound s = new FadeSound(sound, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    public static FadeLoopSound playSoundLoopFadeInClient(@Nonnull SoundEvent sound,
                                                          @Nonnull SoundSource source,
                                                          @Nonnull Vec3 pos,
                                                          float volume, float pitch,
                                                          boolean isGlobal,
                                                          @Nonnull Predicate<PositionedLoopSound> func) {
        FadeLoopSound s = new FadeLoopSound(sound, source, volume, pitch, pos, isGlobal);
        s.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(s);
        return s;
    }

    public static float getSoundVolume(@Nonnull SoundSource cat) {
        return Minecraft.getInstance().options.getSoundSourceVolume(cat);
    }

    public static void playSoundClient(@Nonnull SoundEvent sound, float volume, float pitch) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(sound, volume, pitch);
        }
    }

    public static void playSoundClientWorld(@Nonnull CategorizedSoundEvent sound,
                                            @Nonnull BlockPos pos,
                                            float volume, float pitch) {
        playSoundClientWorld(sound.getEvent(), sound.getCategory(), pos, volume, pitch);
    }

    public static void playSoundClientWorld(@Nonnull SoundEvent sound, @Nonnull SoundSource cat,
                                            @Nonnull BlockPos pos,
                                            float volume, float pitch) {
        net.minecraft.client.multiplayer.ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel != null) {
            clientLevel.playSound(
                    Minecraft.getInstance().player,
                    pos.getX(), pos.getY(), pos.getZ(),
                    sound, cat, volume, pitch);
        }
    }
}
