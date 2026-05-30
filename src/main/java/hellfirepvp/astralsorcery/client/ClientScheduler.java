package hellfirepvp.astralsorcery.client;

import hellfirepvp.astralsorcery.client.effect.EffectManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side tick counter. Delegates to {@link EffectManager#getClientTick()}
 * so that all client-side animation code uses a single monotone counter.
 *
 * <p>1.16 → 1.20: ObserverLib ITickHandler replaced by EffectManager which
 * increments its own counter each ClientTickEvent.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientScheduler {

    private ClientScheduler() {}

    public static long getClientTick() {
        return EffectManager.getClientTick();
    }
}
