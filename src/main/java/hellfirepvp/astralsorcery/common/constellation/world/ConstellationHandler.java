package hellfirepvp.astralsorcery.common.constellation.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellationSpecialShowup;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Tracks which constellations are active in each moon phase for a given world seed.
 * Built once on first tick via a seeded shuffle that distributes weak/minor constellations
 * across the 8 moon phases. Special show-up constellations are checked daily.
 *
 * <p>1.16 → 1.20: World → Level; getDayTime() API unchanged.</p>
 */
public class ConstellationHandler {

    private final WorldContext ctx;

    private final Map<MoonPhase, LinkedList<IConstellation>> activeMap = Maps.newHashMap();
    private final Map<IConstellation, MoonPhase> directOffsetMap = Maps.newHashMap();

    private int lastRecordedDay = -1;
    private final List<IConstellation> visibleSpecialConstellations = Lists.newArrayList();

    ConstellationHandler(WorldContext context) {
        this.ctx = context;
    }

    @Nullable
    public MoonPhase getOffset(IConstellation cst) {
        return this.directOffsetMap.get(cst);
    }

    public boolean isActiveCurrently(IConstellation cst, MoonPhase phase) {
        return isActiveInPhase(cst, phase) || this.visibleSpecialConstellations.contains(cst);
    }

    public boolean isActiveInPhase(IConstellation cst, MoonPhase phase) {
        LinkedList<IConstellation> list = this.activeMap.get(phase);
        return list != null && list.contains(cst);
    }

    public int getLastTrackedDay() {
        return lastRecordedDay;
    }

    public void tick(Level level) {
        if (activeMap.isEmpty()) {
            initialize();
        }

        long dayLength = hellfirepvp.astralsorcery.common.data.config.CommonConfig.CONFIG.dayLength.get();
        int currentDay = (int) (level.getDayTime() / dayLength);
        if (currentDay != lastRecordedDay) {
            lastRecordedDay = currentDay;
            updateActiveConstellations(level);
        }
    }

    private void updateActiveConstellations(Level level) {
        this.visibleSpecialConstellations.clear();
        long dayNumber = level.getDayTime() / 24000L;

        for (IConstellationSpecialShowup cst : ConstellationRegistry.getSpecialShowupConstellations()) {
            if (cst.doesShowUp(level, dayNumber)) {
                this.visibleSpecialConstellations.add(cst);
            }
        }
    }

    private void initialize() {
        this.activeMap.clear();
        this.directOffsetMap.clear();
        for (MoonPhase ph : MoonPhase.values()) {
            this.activeMap.put(ph, Lists.newLinkedList());
        }

        Random rand = ctx.getRandom();

        boolean[] occupiedSlots = new boolean[MoonPhase.values().length];
        Arrays.fill(occupiedSlots, false);

        LinkedList<IWeakConstellation> weakAndMajor = Lists.newLinkedList(ConstellationRegistry.getWeakConstellations());
        Collections.shuffle(weakAndMajor, rand);
        weakAndMajor.forEach(c -> addConstellationCycle(c, rand, occupiedSlots));

        LinkedList<IConstellation> minors = Lists.newLinkedList(ConstellationRegistry.getMinorConstellations());
        Collections.shuffle(minors, rand);
        minors.forEach(c -> addConstellationCycle(c, rand, occupiedSlots));
    }

    private void addConstellationCycle(IConstellation cst, Random rand, boolean[] slots) {
        if (cst instanceof IConstellationSpecialShowup) return;

        if (cst instanceof IMinorConstellation minor) {
            for (MoonPhase ph : minor.getShowupMoonPhases(ctx.getSeed())) {
                LinkedList<IConstellation> list = this.activeMap.get(ph);
                if (list != null) list.add(cst);
            }
        } else {
            int start = searchForSpot(rand, slots);
            occupySlots(start, slots);
            if (getFreeSlots(slots) <= 0) Arrays.fill(slots, false);

            for (int i = 0; i < 5; i++) {
                MoonPhase ph = getPhase(start + i);
                LinkedList<IConstellation> list = this.activeMap.get(ph);
                if (list != null) list.add(cst);
            }
            this.directOffsetMap.put(cst, getPhase(start));
        }
    }

    private MoonPhase getPhase(int rIndex) {
        int count = MoonPhase.values().length;
        while (rIndex < 0) rIndex += count;
        return MoonPhase.values()[rIndex % count];
    }

    private int searchForSpot(Random r, boolean[] occupied) {
        int start = 0;
        boolean foundFree = false;
        int tries = 5;
        do {
            tries--;
            start = r.nextInt(8);
            if (getFreeSlots(occupied) >= 3) {
                foundFree = true;
            }
        } while (!foundFree && tries > 0);
        return start;
    }

    private void occupySlots(int start, boolean[] occupied) {
        for (int i = 0; i < 5; i++) {
            occupied[(start + i) % 8] = true;
        }
    }

    private int getFreeSlots(boolean[] array) {
        int count = 0;
        for (boolean b : array) { if (!b) count++; }
        return count;
    }
}
