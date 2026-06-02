package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;

/**
 * Static accessor constants for all built-in constellation effect providers.
 *
 * <p>In 1.16 these were mutable statics populated by RegistryConstellationEffects.
 * In the port they delegate to {@link ConstellationEffectRegistry} which is
 * populated during {@code FMLCommonSetupEvent}. Constants here are convenience
 * aliases; callers that already use {@link ConstellationEffectRegistry#getProvider}
 * directly are also fine.</p>
 *
 * <p>Valid after {@code ConstellationEffectRegistry.init()} has run (common setup).</p>
 */
public final class ConstellationEffectsAS {

    private ConstellationEffectsAS() {}

    public static ConstellationEffectProvider AEVITAS() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.AEVITAS);
    }

    public static ConstellationEffectProvider ARMARA() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.ARMARA);
    }

    public static ConstellationEffectProvider BOOTES() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.BOOTES);
    }

    public static ConstellationEffectProvider DISCIDIA() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.DISCIDIA);
    }

    public static ConstellationEffectProvider EVORSIO() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.EVORSIO);
    }

    public static ConstellationEffectProvider FORNAX() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.FORNAX);
    }

    public static ConstellationEffectProvider HOROLOGIUM() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.HOROLOGIUM);
    }

    public static ConstellationEffectProvider LUCERNA() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.LUCERNA);
    }

    public static ConstellationEffectProvider MINERALIS() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.MINERALIS);
    }

    public static ConstellationEffectProvider OCTANS() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.OCTANS);
    }

    public static ConstellationEffectProvider PELOTRIO() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.PELOTRIO);
    }

    public static ConstellationEffectProvider VICIO() {
        return ConstellationEffectRegistry.getProvider(ConstellationsAS.VICIO);
    }
}
