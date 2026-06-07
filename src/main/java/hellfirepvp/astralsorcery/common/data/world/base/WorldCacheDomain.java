package hellfirepvp.astralsorcery.common.data.world.base;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Replacement for ObserverLib's WorldCacheDomain.
 * Manages typed, world-scoped persistent data keyed by SaveKey instances.
 * Each SaveKey maps to exactly one data instance per ServerLevel.
 */
public class WorldCacheDomain {

    private final ResourceLocation domainName;
    private final Map<SaveKey<?>, Function<SaveKey<?>, ? extends BaseWorldData>> factories = new HashMap<>();

    public WorldCacheDomain(@Nonnull ResourceLocation domainName) {
        this.domainName = domainName;
    }

    @Nonnull
    public ResourceLocation getDomainName() {
        return this.domainName;
    }

    /**
     * Register a data key with its factory.
     *
     * @param key     the save key
     * @param factory a function that creates a new data instance given the key
     * @param <T>     the data type
     * @return the same key (for chaining)
     */
    @Nonnull
    public <T extends BaseWorldData> SaveKey<T> register(@Nonnull SaveKey<T> key,
                                                          @Nonnull Function<SaveKey<?>, T> factory) {
        this.factories.put(key, factory);
        return key;
    }

    /**
     * Get or create data for the given key in the given level.
     * Uses Forge's SavedData system under the hood.
     *
     * @param level the server level
     * @param key   the data key
     * @param <T>   the data type
     * @return the data instance
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <T extends BaseWorldData> T getData(@Nonnull ServerLevel level, @Nonnull SaveKey<T> key) {
        // ResourceLocation.toString() produces "namespace:path" — colon is illegal in
        // Windows file names. Use underscores throughout to stay cross-platform safe.
        String dataId = this.domainName.getNamespace() + "_"
                + this.domainName.getPath() + "_" + key.getIdentifier();
        return level.getDataStorage().computeIfAbsent(
                tag -> {
                    T data = ((Function<SaveKey<?>, T>) this.factories.get(key)).apply(key);
                    data.load(tag);
                    return data;
                },
                () -> ((Function<SaveKey<?>, T>) this.factories.get(key)).apply(key),
                dataId
        );
    }

    /**
     * A typed key for world-scoped persistent data.
     *
     * @param <T> the data type this key is associated with
     */
    public static class SaveKey<T extends BaseWorldData> {

        private final String identifier;

        public SaveKey(@Nonnull String identifier) {
            this.identifier = identifier;
        }

        @Nonnull
        public String getIdentifier() {
            return this.identifier;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SaveKey<?> that = (SaveKey<?>) o;
            return this.identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            return this.identifier.hashCode();
        }
    }
}
