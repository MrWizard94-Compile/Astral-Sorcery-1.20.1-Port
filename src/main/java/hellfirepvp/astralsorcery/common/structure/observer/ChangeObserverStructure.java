package hellfirepvp.astralsorcery.common.structure.observer;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Replacement for ObserverLib's ChangeObserverStructure.
 * Represents the observation state for a structure at a given position.
 * Holds the provider registry name so callers can identify which structure
 * is being observed.
 */
public class ChangeObserverStructure {

    private final ResourceLocation providerRegistryName;

    public ChangeObserverStructure(@Nonnull ResourceLocation providerRegistryName) {
        this.providerRegistryName = providerRegistryName;
    }

    /**
     * Get the registry name of the structure being observed.
     *
     * @return the provider's registry name
     */
    @Nonnull
    public ResourceLocation getProviderRegistryName() {
        return this.providerRegistryName;
    }
}
