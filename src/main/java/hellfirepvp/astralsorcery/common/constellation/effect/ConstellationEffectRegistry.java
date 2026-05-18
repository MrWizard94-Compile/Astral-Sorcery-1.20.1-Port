/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.*;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps each weak constellation to its {@link ConstellationEffectProvider} instance.
 *
 * <p>Replaces the 1.16 approach of using a Forge custom registry
 * ({@code RegistriesAS.REGISTRY_CONSTELLATION_EFFECT}). In the port the mapping
 * is a plain static map initialised during common setup.</p>
 *
 * <p>The ENTITY_TAG_LUCERNA_SKIP_ENTITY constant is kept here so callers
 * that checked the 1.16 class do not need to change their import.</p>
 */
public final class ConstellationEffectRegistry {

    public static final String ENTITY_TAG_LUCERNA_SKIP_ENTITY = "skip.spawn.deny";

    private static final Map<ResourceLocation, ConstellationEffectProvider> REGISTRY = new HashMap<>();

    private ConstellationEffectRegistry() {}

    /**
     * Registers the built-in effect providers for all 12 weak/major constellations.
     * Called once from {@code ConstellationsAS.init()} (inside
     * {@code FMLCommonSetupEvent.enqueueWork}).
     */
    public static void init() {
        register(ConstellationsAS.AEVITAS,    new CEffectAevitas());
        register(ConstellationsAS.ARMARA,     new CEffectArmara());
        register(ConstellationsAS.BOOTES,     new CEffectBootes());
        register(ConstellationsAS.DISCIDIA,   new CEffectDiscidia());
        register(ConstellationsAS.EVORSIO,    new CEffectEvorsio());
        register(ConstellationsAS.FORNAX,     new CEffectFornax());
        register(ConstellationsAS.HOROLOGIUM, new CEffectHorologium());
        register(ConstellationsAS.LUCERNA,    new CEffectLucerna());
        register(ConstellationsAS.MINERALIS,  new CEffectMineralis());
        register(ConstellationsAS.OCTANS,     new CEffectOctans());
        register(ConstellationsAS.PELOTRIO,   new CEffectPelotrio());
        register(ConstellationsAS.VICIO,      new CEffectVicio());
    }

    private static void register(IWeakConstellation constellation, ConstellationEffectProvider provider) {
        if (constellation != null) {
            REGISTRY.put(constellation.getRegistryName(), provider);
        }
    }

    /**
     * Returns the effect provider for the given constellation, or {@code null}
     * if none is registered.
     */
    @Nullable
    public static ConstellationEffectProvider getProvider(@Nullable IWeakConstellation constellation) {
        if (constellation == null) {
            return null;
        }
        return REGISTRY.get(constellation.getRegistryName());
    }

    /**
     * Creates a new effect instance for {@code constellation} using its provider.
     * Returns {@code null} if no provider is registered.
     */
    @Nullable
    public static ConstellationEffectProvider createInstance(@Nullable IWeakConstellation constellation) {
        return getProvider(constellation);
    }
}
