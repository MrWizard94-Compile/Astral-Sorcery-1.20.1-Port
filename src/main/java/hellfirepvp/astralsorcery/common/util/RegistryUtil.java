package hellfirepvp.astralsorcery.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replacement for ObserverLib's RegistryUtil.
 * Provides convenience methods for looking up values in Minecraft's
 * dynamic registries (biomes, damage types, etc.).
 *
 * <p>In 1.20, dynamic registries are accessed via
 * {@code level.registryAccess().registryOrThrow(registryKey)}.</p>
 */
public final class RegistryUtil {

    private static final RegistryUtil CLIENT_INSTANCE = new RegistryUtil();

    private RegistryUtil() {}

    /**
     * Get the client-side registry utility instance.
     *
     * @return the client instance
     */
    @Nonnull
    public static RegistryUtil client() {
        return CLIENT_INSTANCE;
    }

    /**
     * Look up a value in a dynamic registry by its resource key.
     * Uses the client level's registry access.
     *
     * @param registryKey the registry to look in (e.g. Registries.BIOME)
     * @param valueKey    the specific value to retrieve (e.g. Biomes.PLAINS)
     * @param <T>         the registry value type
     * @return the value, or null if the client level is unavailable or the key is missing
     */
    @Nullable
    public <T> T getValue(@Nonnull ResourceKey<? extends Registry<T>> registryKey,
                          @Nonnull ResourceKey<T> valueKey) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        Registry<T> registry = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(registryKey);
        return registry.get(valueKey);
    }

    /**
     * Look up a value in a dynamic registry by ResourceLocation.
     *
     * @param registryKey the registry to look in
     * @param location    the resource location of the value
     * @param <T>         the registry value type
     * @return the value, or null if not found
     */
    @Nullable
    public <T> T getValue(@Nonnull ResourceKey<? extends Registry<T>> registryKey,
                          @Nonnull ResourceLocation location) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        Registry<T> registry = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(registryKey);
        return registry.get(location);
    }
}
