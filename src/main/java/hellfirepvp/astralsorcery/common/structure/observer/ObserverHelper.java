package hellfirepvp.astralsorcery.common.structure.observer;

import hellfirepvp.astralsorcery.common.lib.structure.PatternBlockArray;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Replacement for ObserverLib's ObserverHelper.
 * Singleton that manages structure observations in the world.
 * Tracks active ChangeSubscriber instances keyed by dimension + position.
 *
 * <p>Structure patterns are resolved via a registered resolver function
 * (set by the structure registration system in later phases).</p>
 */
public class ObserverHelper {

    private static final ObserverHelper INSTANCE = new ObserverHelper();

    /**
     * Maps dimension key + block position to active subscribers.
     * Key format: "dimension:path|x,y,z"
     */
    private final Map<String, ChangeSubscriber<ChangeObserverStructure>> subscribers = new HashMap<>();

    /**
     * Resolves a structure registry name to its pattern.
     * Set during mod initialization by the structure registration system.
     */
    @Nullable
    private Function<ResourceLocation, PatternBlockArray> patternResolver;

    private ObserverHelper() {}

    /**
     * Get the singleton helper instance.
     *
     * @return the helper
     */
    @Nonnull
    public static ObserverHelper getHelper() {
        return INSTANCE;
    }

    /**
     * Register a function that resolves structure registry names to patterns.
     * Called during mod setup once the structure registry is populated.
     *
     * @param resolver maps ResourceLocation to PatternBlockArray (or null if unknown)
     */
    public void setPatternResolver(@Nonnull Function<ResourceLocation, PatternBlockArray> resolver) {
        this.patternResolver = resolver;
    }

    /**
     * Begin observing a structure at the given position.
     *
     * @param level    the world
     * @param pos      the origin position (typically the block entity's position)
     * @param provider the structure provider identifying which structure to observe
     * @return the subscriber tracking this observation
     */
    @Nonnull
    public ChangeSubscriber<ChangeObserverStructure> observeArea(@Nonnull Level level,
                                                                  @Nonnull BlockPos pos,
                                                                  @Nonnull ObserverProviderStructure provider) {
        String key = makeKey(level, pos);
        PatternBlockArray pattern = resolvePattern(provider.getProviderRegistryName());
        ChangeObserverStructure observer = new ChangeObserverStructure(provider.getProviderRegistryName());
        ChangeSubscriber<ChangeObserverStructure> subscriber =
                new ChangeSubscriber<>(observer, pos.immutable(), pattern);
        this.subscribers.put(key, subscriber);
        return subscriber;
    }

    /**
     * Remove an active observer at the given position.
     *
     * @param level the world
     * @param pos   the observed position
     */
    public void removeObserver(@Nonnull Level level, @Nonnull BlockPos pos) {
        this.subscribers.remove(makeKey(level, pos));
    }

    /**
     * Get the active subscriber at the given position, or null if none.
     *
     * @param level the world
     * @param pos   the position to check
     * @return the subscriber, or null
     */
    @Nullable
    public ChangeSubscriber<ChangeObserverStructure> getSubscriber(@Nonnull Level level,
                                                                    @Nonnull BlockPos pos) {
        return this.subscribers.get(makeKey(level, pos));
    }

    /**
     * Clear all tracked subscribers. Called on server shutdown.
     */
    public void clearAll() {
        this.subscribers.clear();
    }

    @Nullable
    private PatternBlockArray resolvePattern(@Nonnull ResourceLocation registryName) {
        if (this.patternResolver != null) {
            return this.patternResolver.apply(registryName);
        }
        return null;
    }

    @Nonnull
    private String makeKey(@Nonnull Level level, @Nonnull BlockPos pos) {
        ResourceLocation dimKey = level.dimension().location();
        return dimKey + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
