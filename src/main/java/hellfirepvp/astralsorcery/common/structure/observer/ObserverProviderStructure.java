package hellfirepvp.astralsorcery.common.structure.observer;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's ObserverProviderStructure.
 * Provides structure observation keyed by a registry name.
 * Created by StructureType.observe() and passed to ObserverHelper.
 */
public class ObserverProviderStructure implements ObserverProvider {

    private final ResourceLocation registryName;

    public ObserverProviderStructure(@Nonnull ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Override
    @Nonnull
    public ResourceLocation getProviderRegistryName() {
        return this.registryName;
    }
}
