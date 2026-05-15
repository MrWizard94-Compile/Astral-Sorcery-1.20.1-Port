package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All Astral Sorcery custom recipe type registrations.
 *
 * <p>1.16 -> 1.20 changes:
 * IRecipeType -> RecipeType,
 * RecipeType.register(String) -> RecipeType.simple(ResourceLocation)</p>
 */
public class RecipeTypesAS {

    private RecipeTypesAS() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<RecipeType<WellLiquefaction>> WELL_LIQUEFACTION =
            RECIPE_TYPES.register("well_liquefaction",
                    () -> RecipeType.simple(AstralSorcery.key("well_liquefaction")));

    public static final RegistryObject<RecipeType<LiquidInfusion>> LIQUID_INFUSION =
            RECIPE_TYPES.register("liquid_infusion",
                    () -> RecipeType.simple(AstralSorcery.key("liquid_infusion")));

    public static final RegistryObject<RecipeType<BlockTransmutation>> BLOCK_TRANSMUTATION =
            RECIPE_TYPES.register("block_transmutation",
                    () -> RecipeType.simple(AstralSorcery.key("block_transmutation")));

    public static final RegistryObject<RecipeType<SimpleAltarRecipe>> ALTAR =
            RECIPE_TYPES.register("altar",
                    () -> RecipeType.simple(AstralSorcery.key("altar")));

    public static final RegistryObject<RecipeType<LiquidInteraction>> LIQUID_INTERACTION =
            RECIPE_TYPES.register("liquid_interaction",
                    () -> RecipeType.simple(AstralSorcery.key("liquid_interaction")));
}
