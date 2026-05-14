package hellfirepvp.astralsorcery.common.util;

import net.minecraft.util.Mth;

/**
 * Time-based multiplier that diminishes on use and recovers over time.
 * Used for damage/effect frequency limiting.
 */
public class DiminishingMultiplier {

    private final long gainMsTime;
    private final float gainRate;

    private final float dropRate;

    private final float min;

    private float multiplier = 1F;
    private long lastGain = 0L;

    private int recoveryStack = 0;

    public DiminishingMultiplier(long gainMsTime, float gainRate, float dropRate, float min) {
        this.gainMsTime = gainMsTime;
        this.gainRate = gainRate;
        this.dropRate = dropRate;
        this.min = min;
    }

    public float getMultiplier() {
        this.recalcMultiplier();
        return this.multiplier;
    }

    private void recalcMultiplier() {
        long now = System.currentTimeMillis();

        long diff = now - this.lastGain;
        long times = (diff * (this.recoveryStack + 1)) / this.gainMsTime;
        if (times > 0) {
            this.lastGain = now;
            this.recoveryStack = Math.min(this.recoveryStack + 1, 3);
            this.multiplier = Mth.clamp(this.multiplier + times * gainRate, this.min, 1F);
        } else {
            this.multiplier = Math.max(this.multiplier - this.dropRate, this.min);
            this.recoveryStack = 0;
        }
    }
}
