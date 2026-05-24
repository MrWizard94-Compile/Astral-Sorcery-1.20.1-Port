package hellfirepvp.astralsorcery.common.crafting.nojson;

import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffect;
import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffectLiquid;
import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffectVortex;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FountainEffectRegistry {

    public static final FountainEffectLiquid EFFECT_LIQUID = new FountainEffectLiquid();
    public static final FountainEffectVortex EFFECT_VORTEX = new FountainEffectVortex();

    private static final Map<ResourceLocation, FountainEffect<?>> registry = new HashMap<>();

    private FountainEffectRegistry() {}

    public static void registerAll() {
        register(EFFECT_LIQUID);
        register(EFFECT_VORTEX);
    }

    private static void register(@Nullable FountainEffect<?> effect) {
        if (effect != null) {
            registry.put(effect.getId(), effect);
        }
    }

    @Nullable
    public static FountainEffect<?> getEffect(@Nullable ResourceLocation key) {
        return key == null ? null : registry.get(key);
    }
}
