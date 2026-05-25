package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.RecipeDyeableChangeColor;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.AltarUpgradeRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ResonatorUpgradeRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All Astral Sorcery custom recipe serializer registrations.
 *
 * <p>1.16 -> 1.20 changes:
 * IRecipeSerializer -> RecipeSerializer</p>
 */
public class RecipeSerializersAS {

    private RecipeSerializersAS() {}

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AstralSorcery.MODID);

    public static final RegistryObject<RecipeSerializer<WellLiquefaction>> WELL_LIQUEFACTION =
            RECIPE_SERIALIZERS.register("well_liquefaction",
                    WellLiquefaction.Serializer::new);

    public static final RegistryObject<RecipeSerializer<LiquidInfusion>> LIQUID_INFUSION =
            RECIPE_SERIALIZERS.register("liquid_infusion",
                    LiquidInfusion.Serializer::new);

    public static final RegistryObject<RecipeSerializer<BlockTransmutation>> BLOCK_TRANSMUTATION =
            RECIPE_SERIALIZERS.register("block_transmutation",
                    BlockTransmutation.Serializer::new);

    public static final RegistryObject<RecipeSerializer<SimpleAltarRecipe>> ALTAR =
            RECIPE_SERIALIZERS.register("altar",
                    SimpleAltarRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<LiquidInteraction>> LIQUID_INTERACTION =
            RECIPE_SERIALIZERS.register("liquid_interaction",
                    LiquidInteraction.Serializer::new);

    public static final RegistryObject<RecipeSerializer<SimpleAltarRecipe>> ALTAR_UPGRADE =
            RECIPE_SERIALIZERS.register("altar_upgrade",
                    AltarUpgradeRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<SimpleAltarRecipe>> RESONATOR_UPGRADE =
            RECIPE_SERIALIZERS.register("resonator_upgrade",
                    ResonatorUpgradeRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<RecipeDyeableChangeColor>> CHANGE_WAND_COLOR =
            RECIPE_SERIALIZERS.register("change_wand_color",
                    RecipeDyeableChangeColor.IlluminationWandColorSerializer::new);

    public static final RegistryObject<RecipeSerializer<RecipeDyeableChangeColor>> CHANGE_GATEWAY_COLOR =
            RECIPE_SERIALIZERS.register("change_gateway_color",
                    RecipeDyeableChangeColor.GatewayColorSerializer::new);
}
