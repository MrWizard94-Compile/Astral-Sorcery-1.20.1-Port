package hellfirepvp.astralsorcery.common.structure.observer;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's ObserverProvider.
 * Base interface for structure observation providers.
 * Each provider is identified by a registry name that maps to a StructureType.
 */
public interface ObserverProvider {

    /**
     * Get the registry name that identifies this provider's structure.
     *
     * @return the provider's registry name
     */
    @Nonnull
    ResourceLocation getProviderRegistryName();
}
