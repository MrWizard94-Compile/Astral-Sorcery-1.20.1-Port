package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;

/**
 * Static accessor constants for all built-in mantle (cape) effects.
 *
 * <p>In 1.16 these were mutable statics populated by RegistryMantleEffects.
 * In the port they delegate to {@link MantleEffectRegistry} which is
 * populated during {@code FMLCommonSetupEvent}. Valid after
 * {@code MantleEffectRegistry.init()} has run.</p>
 */
public final class MantleEffectsAS {

    private MantleEffectsAS() {}

    public static MantleEffect AEVITAS() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.AEVITAS);
    }

    public static MantleEffect ARMARA() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.ARMARA);
    }

    public static MantleEffect BOOTES() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.BOOTES);
    }

    public static MantleEffect DISCIDIA() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.DISCIDIA);
    }

    public static MantleEffect EVORSIO() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.EVORSIO);
    }

    public static MantleEffect FORNAX() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.FORNAX);
    }

    public static MantleEffect HOROLOGIUM() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.HOROLOGIUM);
    }

    public static MantleEffect LUCERNA() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.LUCERNA);
    }

    public static MantleEffect MINERALIS() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.MINERALIS);
    }

    public static MantleEffect OCTANS() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.OCTANS);
    }

    public static MantleEffect PELOTRIO() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.PELOTRIO);
    }

    public static MantleEffect VICIO() {
        return MantleEffectRegistry.getEffect(ConstellationsAS.VICIO);
    }
}
