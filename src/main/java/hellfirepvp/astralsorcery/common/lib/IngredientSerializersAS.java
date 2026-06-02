package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredient;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

/**
 * Public constants for Astral Sorcery custom recipe ingredient serializers.
 *
 * <p>Registered with Forge via {@code CraftingHelper.register(ResourceLocation, IIngredientSerializer)}
 * during {@code FMLCommonSetupEvent} in {@link hellfirepvp.astralsorcery.common.registry.RegistryIngredientTypes}.</p>
 */
public final class IngredientSerializersAS {

    private IngredientSerializersAS() {}

    /** Serializes {@link CrystalIngredient} to/from JSON and network. */
    public static final IIngredientSerializer<CrystalIngredient> CRYSTAL_SERIALIZER =
            CrystalIngredient.SERIALIZER;

    /** Serializes {@link FluidIngredient} to/from JSON and network. */
    public static final IIngredientSerializer<FluidIngredient> FLUID_SERIALIZER =
            FluidIngredient.SERIALIZER;
}
