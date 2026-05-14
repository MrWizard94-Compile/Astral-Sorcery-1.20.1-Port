package hellfirepvp.astralsorcery.common.util;

import net.minecraft.util.Mth;

/**
 * Tracks an incrementing/decrementing counter clamped to [0, cap].
 * Used for smooth visual transitions.
 */
public class EffectIncrementer {

    private final int cap;
    private int current = 0;

    public EffectIncrementer(int max) {
        this.cap = max;
    }

    public void update(boolean increment) {
        if (increment) {
            this.current++;
        } else {
            this.current--;
        }
        this.current = Mth.clamp(this.current, 0, this.cap);
    }

    public int get() {
        return current;
    }

    public float getAsPercentage() {
        return this.current / ((float) this.cap);
    }
}
