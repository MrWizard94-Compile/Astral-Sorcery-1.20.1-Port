/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple map-based registry for all {@link CrystalProperty} instances.
 * 1.16 → 1.20: replaced Forge registry lookup with an internal LinkedHashMap.
 */
public class CrystalPropertyRegistry {

    public static final CrystalPropertyRegistry INSTANCE = new CrystalPropertyRegistry();

    private final Map<ResourceLocation, CrystalProperty> registry = new LinkedHashMap<>();

    private CrystalPropertyRegistry() {}

    public void register(@Nonnull CrystalProperty property) {
        registry.put(property.getRegistryName(), property);
    }

    @Nullable
    public CrystalProperty getProperty(@Nonnull ResourceLocation key) {
        return registry.get(key);
    }

    @Nonnull
    public Collection<CrystalProperty> getAllProperties() {
        return Collections.unmodifiableCollection(registry.values());
    }
}
