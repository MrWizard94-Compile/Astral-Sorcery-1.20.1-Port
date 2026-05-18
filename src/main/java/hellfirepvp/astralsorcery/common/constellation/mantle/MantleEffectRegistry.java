/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.*;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps each weak/major constellation to its {@link MantleEffect} instance.
 *
 * <p>Replaces the 1.16 Forge custom registry approach. Call {@link #init()}
 * once from common setup after {@code ConstellationsAS.init()}.</p>
 */
public final class MantleEffectRegistry {

    private static final Map<ResourceLocation, MantleEffect> REGISTRY = new HashMap<>();

    private MantleEffectRegistry() {}

    public static void init() {
        register(ConstellationsAS.AEVITAS,    new MantleEffectAevitas());
        register(ConstellationsAS.ARMARA,     new MantleEffectArmara());
        register(ConstellationsAS.BOOTES,     new MantleEffectBootes());
        register(ConstellationsAS.DISCIDIA,   new MantleEffectDiscidia());
        register(ConstellationsAS.EVORSIO,    new MantleEffectEvorsio());
        register(ConstellationsAS.FORNAX,     new MantleEffectFornax());
        register(ConstellationsAS.HOROLOGIUM, new MantleEffectHorologium());
        register(ConstellationsAS.LUCERNA,    new MantleEffectLucerna());
        register(ConstellationsAS.MINERALIS,  new MantleEffectMineralis());
        register(ConstellationsAS.OCTANS,     new MantleEffectOctans());
        register(ConstellationsAS.PELOTRIO,   new MantleEffectPelotrio());
        register(ConstellationsAS.VICIO,      new MantleEffectVicio());
    }

    private static void register(IWeakConstellation constellation, MantleEffect effect) {
        if (constellation != null) {
            REGISTRY.put(constellation.getRegistryName(), effect);
        }
    }

    @Nullable
    public static MantleEffect getEffect(@Nullable IWeakConstellation constellation) {
        if (constellation == null) {
            return null;
        }
        return REGISTRY.get(constellation.getRegistryName());
    }
}
