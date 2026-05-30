package hellfirepvp.astralsorcery.common.constellation.world.event;

import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * Lunar eclipse — occurs once every 68 in-game days at midnight.
 * During the eclipse, the moon turns blood-red and starlight is amplified.
 *
 * <p>1.16 → 1.20: World → Level.</p>
 */
public class LunarEclipse extends CelestialEvent {

    private boolean active = false;
    private boolean dayOfEvent = false;
    private int prevEventTick = 0;
    private int eventTick = 0;

    @Override
    public void tick(Level level, Random rand, WorldContext ctx) {
        for (int i = 0; i < 12 + rand.nextInt(12); i++) {
            rand.nextLong();
        }
        int halfTime = getEventDuration() / 2;

        int repeat = 68;
        long wTime = level.getDayTime();
        int suggestedDayLength = CommonConfig.CONFIG.dayLength.get();
        int lunarTime = (int) (wTime % ((long) repeat * suggestedDayLength));
        dayOfEvent = lunarTime >= 0 && lunarTime < suggestedDayLength;
        int midLOffset = Math.round(suggestedDayLength * 0.75F);

        if (wTime > suggestedDayLength
                && lunarTime > (midLOffset - halfTime)
                && lunarTime < (midLOffset + halfTime)) {
            active = true;
            prevEventTick = eventTick;
            eventTick = lunarTime - (midLOffset - halfTime);
        } else {
            active = false;
            prevEventTick = 0;
            eventTick = 0;
        }
    }

    @Override
    public boolean isActiveNow() {
        return active;
    }

    @Override
    public boolean isActiveDay() {
        return dayOfEvent;
    }

    @Override
    public float getEffectTick(float pTicks) {
        return prevEventTick + ((eventTick - prevEventTick) * pTicks);
    }

    @Override
    public long getSeedModifier() {
        return 0xA595C3B735D5BFB9L;
    }

    public int getEventDuration() {
        return CommonConfig.CONFIG.dayLength.get() / 5;
    }
}
