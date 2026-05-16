/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Generates vanilla crafting recipes for AS items.
 * Custom AS recipe types (altar, infusion, etc.) are data-driven
 * via handwritten JSON files.
 */
public class AstralRecipeProvider extends RecipeProvider {

    public AstralRecipeProvider(@Nonnull PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> writer) {
        // Marble conversions
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_BRICKS.get(), 4)
                .define('M', BlocksAS.MARBLE_RAW.get())
                .pattern("MM")
                .pattern("MM")
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_PILLAR.get(), 2)
                .define('M', BlocksAS.MARBLE_RAW.get())
                .pattern("M")
                .pattern("M")
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_CHISELED.get(), 4)
                .define('S', BlocksAS.MARBLE_SLAB.get())
                .pattern("SS")
                .pattern("SS")
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_ARCH.get(), 3)
                .define('M', BlocksAS.MARBLE_RAW.get())
                .pattern("MMM")
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(writer);

        // Marble stairs & slabs
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_STAIRS.get(), 4)
                .define('M', BlocksAS.MARBLE_RAW.get())
                .pattern("M  ")
                .pattern("MM ")
                .pattern("MMM")
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_SLAB.get(), 6)
                .define('M', BlocksAS.MARBLE_RAW.get())
                .pattern("MMM")
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(writer);

        // Wand crafting
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ItemsAS.WAND.get())
                .define('A', ItemsAS.AQUAMARINE.get())
                .define('M', BlocksAS.MARBLE_RAW.get())
                .pattern("  A")
                .pattern(" M ")
                .pattern("M  ")
                .unlockedBy("has_aquamarine", has(ItemsAS.AQUAMARINE.get()))
                .save(writer);

        // Illumination powder
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemsAS.ILLUMINATION_POWDER.get(), 4)
                .requires(ItemsAS.STARDUST.get())
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_stardust", has(ItemsAS.STARDUST.get()))
                .save(writer);
    }
}
