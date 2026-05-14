package hellfirepvp.astralsorcery.common.structure.observer;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the structure observer classes.
 * These are pure-data classes with no Minecraft world dependency.
 */
class ChangeObserverStructureTest {

    @Test
    void observerProviderStoresRegistryName() {
        ResourceLocation name = new ResourceLocation("astralsorcery", "altar_attunement");
        ObserverProviderStructure provider = new ObserverProviderStructure(name);

        assertEquals(name, provider.getProviderRegistryName());
    }

    @Test
    void changeObserverStoresRegistryName() {
        ResourceLocation name = new ResourceLocation("astralsorcery", "ritual_pedestal");
        ChangeObserverStructure observer = new ChangeObserverStructure(name);

        assertEquals(name, observer.getProviderRegistryName());
    }

    @Test
    void providerImplementsObserverProvider() {
        ResourceLocation name = new ResourceLocation("astralsorcery", "infuser");
        ObserverProviderStructure provider = new ObserverProviderStructure(name);

        // Should be assignable to ObserverProvider
        ObserverProvider asInterface = provider;
        assertEquals(name, asInterface.getProviderRegistryName());
    }
}
