package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RegistriesAS {

    private RegistriesAS() {}

    public static final ResourceKey<Registry<Object>> REGISTRY_CONSTELLATIONS =
            customKey("constellations");
    public static final ResourceKey<Registry<Object>> REGISTRY_CONSTELLATION_EFFECT =
            customKey("constellation_effect");
    public static final ResourceKey<Registry<Object>> REGISTRY_MANTLE_EFFECT =
            customKey("mantle_effect");
    public static final ResourceKey<Registry<Object>> REGISTRY_ENGRAVING_EFFECT =
            customKey("engraving_effect");
    public static final ResourceKey<Registry<Object>> REGISTRY_STRUCTURE_TYPES =
            customKey("structure_types");
    public static final ResourceKey<Registry<Object>> REGISTRY_PERK_ATTRIBUTE_TYPES =
            customKey("perk_attribute_types");
    public static final ResourceKey<Registry<Object>> REGISTRY_PERK_ATTRIBUTE_CONVERTERS =
            customKey("perk_attribute_converters");
    public static final ResourceKey<Registry<Object>> REGISTRY_PERK_CUSTOM_MODIFIERS =
            customKey("perk_attribute_custom_modifiers");
    public static final ResourceKey<Registry<Object>> REGISTRY_PERK_ATTRIBUTE_READERS =
            customKey("perk_attribute_readers");
    public static final ResourceKey<Registry<Object>> REGISTRY_CRYSTAL_PROPERTIES =
            customKey("attribute_crystal_properties");
    public static final ResourceKey<Registry<Object>> REGISTRY_CRYSTAL_USAGES =
            customKey("attribute_crystal_usages");
    public static final ResourceKey<Registry<Object>> REGISTRY_ALTAR_EFFECTS =
            customKey("altar_recipe_effects");

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<Registry<T>> customKey(String name) {
        ResourceLocation location = AstralSorcery.key(name);
        return (ResourceKey<Registry<T>>) (ResourceKey<?>) ResourceKey.createRegistryKey(location);
    }
}
