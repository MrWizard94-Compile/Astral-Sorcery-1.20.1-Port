/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side cache for the player's starlight charge, updated by PktSyncStarlightCharge.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientStarlightCache {

    private static float chargeLevel = 0.0f;
    private static float maxCharge = 1.0f;
    private static boolean charging = false;

    private ClientStarlightCache() {}

    public static void update(float charge, float max, boolean isCharging) {
        chargeLevel = charge;
        maxCharge = max;
        charging = isCharging;
    }

    /** Fill fraction [0, 1]. */
    public static float getFillFraction() {
        return maxCharge > 0 ? Math.min(chargeLevel / maxCharge, 1.0f) : 0.0f;
    }

    public static float getChargeLevel() { return chargeLevel; }
    public static float getMaxCharge()   { return maxCharge; }
    public static boolean isCharging()   { return charging; }
}
