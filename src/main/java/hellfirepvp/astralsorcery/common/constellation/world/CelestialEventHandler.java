package hellfirepvp.astralsorcery.common.constellation.world;

import hellfirepvp.astralsorcery.common.constellation.world.event.CelestialEvent;
import hellfirepvp.astralsorcery.common.constellation.world.event.LunarEclipse;
import hellfirepvp.astralsorcery.common.constellation.world.event.SolarEclipse;
import hellfirepvp.astralsorcery.common.constellation.world.event.StarFall;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks active celestial events for a given {@link WorldContext}.
 * Ticked once per world tick from {@link WorldContext#tick(Level)}.
 *
 * <p>1.16 → 1.20: World → Level.</p>
 */
public class CelestialEventHandler {

    private final WorldContext ctx;
    private final Set<CelestialEvent> events = new HashSet<>();

    private final SolarEclipse solarEclipseEvent;
    private final LunarEclipse lunarEclipseEvent;
    private final StarFall starFallNight;

    CelestialEventHandler(WorldContext context) {
        this.ctx = context;
        this.solarEclipseEvent = addTrackedEvent(new SolarEclipse());
        this.lunarEclipseEvent = addTrackedEvent(new LunarEclipse());
        this.starFallNight     = addTrackedEvent(new StarFall());
    }

    public <T extends CelestialEvent> T addTrackedEvent(T event) {
        this.events.add(event);
        return event;
    }

    public SolarEclipse getSolarEclipse() {
        return solarEclipseEvent;
    }

    public LunarEclipse getLunarEclipse() {
        return lunarEclipseEvent;
    }

    public StarFall getStarFallEvent() {
        return starFallNight;
    }

    public float getSolarEclipsePercent() {
        SolarEclipse solar = getSolarEclipse();
        if (!solar.isActiveNow()) {
            return 0F;
        }
        float halfDuration = solar.getEventDuration() / 2F;
        float tick = solar.getEffectTick(0F) - halfDuration;
        tick /= halfDuration;
        return Math.abs(tick);
    }

    void tick(Level level) {
        for (CelestialEvent event : events) {
            event.tick(level, ctx.getRandom(event.getSeedModifier()), ctx);
        }
    }
}
