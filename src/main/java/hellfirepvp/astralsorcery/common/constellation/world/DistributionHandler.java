package hellfirepvp.astralsorcery.common.constellation.world;

import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellationSpecialShowup;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-{@link WorldContext} handler that computes the starlight distribution
 * multiplier for each constellation based on the current moon phase.
 *
 * <p>The distribution is a sine-based curve: constellations peak on the night
 * that matches their assigned moon phase offset and fall off over the 8-day
 * lunar cycle. Special show-up constellations (e.g. Horologium) override the
 * base value when their condition is active.</p>
 *
 * <p>1.16 → 1.20: World → Level; MathHelper → Mth.</p>
 */
public class DistributionHandler {

    private final WorldContext ctx;

    private final Map<Integer, Map<IConstellation, Float>> dayDistributionMap = Maps.newHashMap();
    private Map<IConstellation, Float> activeDistribution = Maps.newHashMap();

    private int lastRecordedDay = -1;

    DistributionHandler(WorldContext ctx) {
        this.ctx = ctx;
    }

    public void tick(Level level) {
        ConstellationHandler cst = ctx.getConstellationHandler();
        int tracked = cst.getLastTrackedDay();

        if (dayDistributionMap.isEmpty()) {
            initialize();
        }

        if (lastRecordedDay == tracked) {
            return;
        }

        lastRecordedDay = tracked;
        updateDistribution(level);
    }

    /** Returns the distribution multiplier [0.0 – 1.0] for the given constellation. */
    public float getDistribution(IConstellation cst) {
        return activeDistribution.getOrDefault(cst, 0F);
    }

    private void updateDistribution(Level level) {
        MoonPhase current = MoonPhase.fromWorld(level);
        Map<IConstellation, Float> distribution = new HashMap<>(dayDistributionMap.get(current.ordinal()));

        long dayNumber = level.getDayTime() / 24000L;
        for (IConstellationSpecialShowup special : ConstellationRegistry.getSpecialShowupConstellations()) {
            boolean showing = special.doesShowUp(level, dayNumber);
            distribution.put(special, Mth.clamp(
                    special.getDistribution(level, dayNumber, showing), 0F, 1F));
        }

        activeDistribution = distribution;
    }

    private void initialize() {
        dayDistributionMap.clear();
        for (MoonPhase ph : MoonPhase.values()) {
            dayDistributionMap.put(ph.ordinal(), Maps.newHashMap());
        }
        int phaseCount = MoonPhase.values().length;

        for (IConstellation cst : ConstellationRegistry.getAllConstellations()) {
            if (cst instanceof IWeakConstellation) {
                MoonPhase offsetPhase = ctx.getConstellationHandler().getOffset(cst);
                if (offsetPhase == null) {
                    return;
                }
                int offset = offsetPhase.ordinal();
                for (MoonPhase ph : MoonPhase.values()) {
                    int index = (offset + ph.ordinal()) % phaseCount;
                    float distr = sineDistance(offset, index);
                    dayDistributionMap.get(index).put(cst, distr);
                }
            } else {
                for (MoonPhase ph : MoonPhase.values()) {
                    dayDistributionMap.get(ph.ordinal()).put(cst, 0F);
                }
            }
        }
    }

    private float sineDistance(int dayStart, int dayIn) {
        int phaseCount = MoonPhase.values().length;
        int dist = Math.min(
                Math.abs(dayStart - dayIn),
                Math.abs(dayStart - (dayIn + phaseCount)));
        float part = ((float) dist) / ((float) (phaseCount / 2));
        return Mth.cos((float) ((part / 2) * Math.PI)) * 0.5F + 0.5F;
    }
}
