package hellfirepvp.astralsorcery.common.constellation.world.event;

import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * Solar eclipse — occurs once every 36 in-game days, centred on high noon.
 * During the eclipse, collector crystals receive a starlight bonus even at day.
 *
 * <p>1.16 → 1.20: World → Level; getDayTime() unchanged.</p>
 */
public class SolarEclipse extends CelestialEvent {

    private boolean active = false;
    private boolean dayOfEvent = false;
    private int prevEventTick = 0;
    private int eventTick = 0;

    @Override
    public void tick(Level level, Random rand, WorldContext ctx) {
        // Flush RNG to match original seeding
        for (int i = 0; i < 12 + rand.nextInt(12); i++) {
            rand.nextLong();
        }
        int rOffset = rand.nextInt(36);
        if (rOffset >= 18) {
            rOffset -= 36;
        }

        int halfTime = getEventDuration() / 2;

        int offset = 36 - rOffset;
        int repeat = 36;
        long wTime = level.getDayTime();
        int suggestedDayLength = CommonConfig.CONFIG.dayLength.get();

        int solarTime = (int) ((wTime - (long) offset * suggestedDayLength) % ((long) repeat * suggestedDayLength));
        dayOfEvent = solarTime >= 0 && solarTime < suggestedDayLength;
        int midSOffset = suggestedDayLength / 4;

        if (wTime > suggestedDayLength
                && solarTime > (midSOffset - halfTime)
                && solarTime < (midSOffset + halfTime)) {
            active = true;
            prevEventTick = eventTick;
            eventTick = solarTime - (midSOffset - halfTime);
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
        return 0x8D8692645A136C53L;
    }

    public int getEventDuration() {
        return CommonConfig.CONFIG.dayLength.get() / 5;
    }
}
