/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal.calc;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Identifies a specific usage context for crystal calculations (e.g. collector crystal, ritual).
 * 1.16 → 1.20: ForgeRegistryEntry removed; carry ResourceLocation directly.
 * TranslationTextComponent → Component.translatable.
 */
public class PropertyUsage {

    private final ResourceLocation registryName;

    public PropertyUsage(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public Component getName() {
        return Component.translatable(String.format("crystal.usage.%s.%s.name",
                registryName.getNamespace(), registryName.getPath()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyUsage that = (PropertyUsage) o;
        return Objects.equals(registryName, that.registryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryName);
    }
}
