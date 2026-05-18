/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class CustomRecipeRegistry<R extends CustomRecipe> {

    private final Map<ResourceLocation, R> recipes = new HashMap<>();

    public abstract void init();

    public void register(@Nonnull R recipe) {
        this.recipes.put(recipe.getKey(), recipe);
    }

    @Nullable
    public R getRecipe(ResourceLocation key) {
        return this.recipes.get(key);
    }

    @Nonnull
    public Collection<R> getRecipes() {
        return recipes.values();
    }
}
