/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all perk attribute types. Types are registered once
 * during mod initialization and looked up by key when perks are loaded
 * or effects are applied.
 *
 * <p>Constants in {@code PerkAttributeTypesAS} hold references to the
 * registered instances. New types can be added by other mods via
 * {@link #register(PerkAttributeType)} during common setup.</p>
 */
public final class AttributeTypeRegistry {

    private AttributeTypeRegistry() {}

    private static final Map<ResourceLocation, PerkAttributeType> REGISTRY = new HashMap<>();
    private static final Map<ResourceLocation, PerkAttributeReader> READERS = new HashMap<>();

    /**
     * Registers an attribute type. Duplicate keys are rejected with a warning.
     *
     * @param type the attribute type to register
     * @return the registered type (for chaining)
     */
    @Nonnull
    public static PerkAttributeType register(@Nonnull PerkAttributeType type) {
        if (REGISTRY.containsKey(type.getKey())) {
            AstralSorcery.log.warn("Duplicate perk attribute type registration: {}", type.getKey());
            return REGISTRY.get(type.getKey());
        }
        REGISTRY.put(type.getKey(), type);
        return type;
    }

    /**
     * Looks up an attribute type by key.
     *
     * @param key the attribute type's resource location
     * @return the attribute type, or null if not registered
     */
    @Nullable
    public static PerkAttributeType getType(@Nonnull ResourceLocation key) {
        return REGISTRY.get(key);
    }

    /**
     * Looks up an attribute type by key, throwing if not found.
     *
     * @param key the attribute type's resource location
     * @return the attribute type
     * @throws IllegalArgumentException if not registered
     */
    @Nonnull
    public static PerkAttributeType getTypeOrThrow(@Nonnull ResourceLocation key) {
        PerkAttributeType type = REGISTRY.get(key);
        if (type == null) {
            throw new IllegalArgumentException("Unknown perk attribute type: " + key);
        }
        return type;
    }

    /**
     * Returns all registered attribute types.
     */
    @Nonnull
    public static Collection<PerkAttributeType> getAllTypes() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Whether a type with the given key is registered.
     */
    public static boolean isRegistered(@Nonnull ResourceLocation key) {
        return REGISTRY.containsKey(key);
    }

    /**
     * Returns the number of registered types.
     */
    public static int size() {
        return REGISTRY.size();
    }

    /**
     * Registers a reader for a specific attribute type, enabling stat display in the perk tree UI.
     */
    public static void registerReader(@Nonnull PerkAttributeReader reader) {
        READERS.put(reader.getType().getKey(), reader);
    }

    /**
     * Returns the registered reader for the given type, or null if none is registered.
     */
    @Nullable
    public static PerkAttributeReader getReader(@Nonnull PerkAttributeType type) {
        return READERS.get(type.getKey());
    }

    /**
     * Clears the registry. Only for testing purposes.
     */
    public static void clearForTesting() {
        REGISTRY.clear();
        READERS.clear();
    }
}
