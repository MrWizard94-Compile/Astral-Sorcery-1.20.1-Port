/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.gen;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Generates vanilla crafting and stonecutting recipe JSON files
 * for Astral Sorcery blocks. Mod-specific recipes (altar crafting,
 * infusion, etc.) are handled by their own data-driven systems.
 */
public class AstralRecipeProvider extends RecipeProvider {

    public AstralRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        // --- Marble crafting conversions ---
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_BRICKS.get(), 4)
                .pattern("MM")
                .pattern("MM")
                .define('M', BlocksAS.MARBLE_RAW.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_PILLAR.get(), 2)
                .pattern("M")
                .pattern("M")
                .define('M', BlocksAS.MARBLE_RAW.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_ARCH.get(), 3)
                .pattern("MMM")
                .define('M', BlocksAS.MARBLE_RAW.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer);

        // --- Stairs and slabs ---
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_STAIRS.get(), 4)
                .pattern("M  ")
                .pattern("MM ")
                .pattern("MMM")
                .define('M', BlocksAS.MARBLE_RAW.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_SLAB.get(), 6)
                .pattern("MMM")
                .define('M', BlocksAS.MARBLE_RAW.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer);

        // --- Stonecutter recipes for marble ---
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(BlocksAS.MARBLE_RAW.get()), RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_BRICKS.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer, "astralsorcery:marble_bricks_from_stonecutting");

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(BlocksAS.MARBLE_RAW.get()), RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_PILLAR.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer, "astralsorcery:marble_pillar_from_stonecutting");

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(BlocksAS.MARBLE_RAW.get()), RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_SLAB.get(), 2)
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer, "astralsorcery:marble_slab_from_stonecutting");

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(BlocksAS.MARBLE_RAW.get()), RecipeCategory.BUILDING_BLOCKS, BlocksAS.MARBLE_STAIRS.get())
                .unlockedBy("has_marble", has(BlocksAS.MARBLE_RAW.get()))
                .save(consumer, "astralsorcery:marble_stairs_from_stonecutting");
    }
}
