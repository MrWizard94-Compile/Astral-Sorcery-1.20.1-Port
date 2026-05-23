package hellfirepvp.astralsorcery.common.constellation.engraving;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory registry of {@link EngravingEffect}s keyed by constellation ResourceLocation.
 * Replaces the 1.16 ForgeRegistry&lt;EngravingEffect&gt; approach.
 * Effects are registered during FMLCommonSetupEvent via {@link hellfirepvp.astralsorcery.common.lib.EngravingEffectsAS}.
 */
public final class EngravingEffectRegistry {

    private static final Map<ResourceLocation, EngravingEffect> REGISTRY = new HashMap<>();

    private EngravingEffectRegistry() {}

    public static void register(@Nonnull EngravingEffect effect) {
        REGISTRY.put(effect.getKey(), effect);
    }

    @Nullable
    public static EngravingEffect get(@Nonnull ResourceLocation key) {
        return REGISTRY.get(key);
    }

    public static boolean isEmpty() {
        return REGISTRY.isEmpty();
    }
}
