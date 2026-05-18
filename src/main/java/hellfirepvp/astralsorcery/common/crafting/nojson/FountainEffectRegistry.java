/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson;

import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffect;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for fountain effects. Full effect implementations (liquid, vortex)
 * are deferred to Phase 12 (client rendering / block entity behavior).
 */
public class FountainEffectRegistry {

    private static final Map<ResourceLocation, FountainEffect<?>> registry = new HashMap<>();

    private FountainEffectRegistry() {}

    public static void register(@Nullable FountainEffect<?> effect) {
        if (effect != null) {
            registry.put(effect.getId(), effect);
        }
    }

    @Nullable
    public static FountainEffect<?> getEffect(@Nullable ResourceLocation key) {
        return key == null ? null : registry.get(key);
    }
}
