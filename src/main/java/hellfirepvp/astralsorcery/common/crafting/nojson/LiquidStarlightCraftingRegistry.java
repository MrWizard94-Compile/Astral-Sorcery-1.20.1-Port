/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson;

import hellfirepvp.astralsorcery.common.crafting.nojson.starlight.FormCelestialCrystalClusterRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.starlight.FormGemCrystalClusterRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.starlight.GrowCrystalSizeRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.starlight.InfusedWoodRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.starlight.LiquidStarlightRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.starlight.MergeCrystalsRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LiquidStarlightCraftingRegistry extends CustomRecipeRegistry<LiquidStarlightRecipe> {

    public static final LiquidStarlightCraftingRegistry INSTANCE = new LiquidStarlightCraftingRegistry();

    @Override
    public void init() {
        this.register(new InfusedWoodRecipe());
        this.register(new GrowCrystalSizeRecipe());
        this.register(new FormCelestialCrystalClusterRecipe());
        this.register(new FormGemCrystalClusterRecipe());
        this.register(new MergeCrystalsRecipe());
    }

    @Nullable
    public LiquidStarlightRecipe getRecipeFor(@Nonnull ItemEntity itemEntity, @Nonnull Level level,
                                              @Nonnull BlockPos at) {
        return this.getRecipes()
                .stream()
                .filter(recipe -> recipe.doesStartRecipe(itemEntity.getItem()))
                .filter(recipe -> recipe.matches(itemEntity, level, at))
                .findFirst()
                .orElse(null);
    }

    public static void tryCraft(@Nonnull ItemEntity itemEntity, @Nonnull BlockPos at) {
        if (!itemEntity.isAlive()) {
            return;
        }
        Level level = itemEntity.level();
        LiquidStarlightRecipe recipe = INSTANCE.getRecipeFor(itemEntity, level, at);
        if (recipe != null && !level.isClientSide()) {
            recipe.doServerCraftTick(itemEntity, level, at);
        }
    }
}
