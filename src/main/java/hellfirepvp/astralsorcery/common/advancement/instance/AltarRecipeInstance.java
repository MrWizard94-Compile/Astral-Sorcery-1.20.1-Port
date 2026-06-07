/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.advancement.AltarCraftTrigger;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AltarRecipeInstance extends AbstractCriterionTriggerInstance {

    private final Set<ResourceLocation> recipeNames = new HashSet<>();
    private final List<Ingredient> recipeOutputs = new ArrayList<>();

    private AltarRecipeInstance(@Nonnull ResourceLocation id, @Nonnull ContextAwarePredicate player) {
        super(id, player);
    }

    public static AltarRecipeInstance any() {
        return new AltarRecipeInstance(AltarCraftTrigger.ID, ContextAwarePredicate.ANY);
    }

    public static AltarRecipeInstance craftRecipe(ResourceLocation... recipeIds) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID, ContextAwarePredicate.ANY);
        instance.recipeNames.addAll(Arrays.asList(recipeIds));
        return instance;
    }

    public static AltarRecipeInstance craftRecipe(SimpleAltarRecipe... recipes) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID, ContextAwarePredicate.ANY);
        Arrays.asList(recipes).forEach(recipe -> instance.recipeNames.add(recipe.getId()));
        return instance;
    }

    public static AltarRecipeInstance withOutput(ItemLike... outputs) {
        return withOutput(Ingredient.of(outputs));
    }

    public static AltarRecipeInstance withOutput(ItemStack... outputs) {
        return withOutput(Ingredient.of(outputs));
    }

    @SafeVarargs
    public static AltarRecipeInstance withOutput(TagKey<Item>... outputs) {
        return withOutput(Arrays.stream(outputs).map(Ingredient::of).collect(Collectors.toList()));
    }

    public static AltarRecipeInstance withOutput(Ingredient... outputs) {
        return withOutput(Arrays.asList(outputs));
    }

    public static AltarRecipeInstance withOutput(List<Ingredient> outputs) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID, ContextAwarePredicate.ANY);
        instance.recipeOutputs.addAll(outputs);
        return instance;
    }

    @Override
    @Nonnull
    public JsonObject serializeToJson(@Nonnull SerializationContext context) {
        JsonObject out = super.serializeToJson(context);
        if (!this.recipeNames.isEmpty()) {
            JsonArray names = new JsonArray();
            for (ResourceLocation name : this.recipeNames) {
                names.add(name.toString());
            }
            out.add("recipeNames", names);
        }
        if (!this.recipeOutputs.isEmpty()) {
            JsonArray outputs = new JsonArray();
            for (Ingredient output : this.recipeOutputs) {
                outputs.add(output.toJson());
            }
            out.add("recipeOutputs", outputs);
        }
        return out;
    }

    @Nonnull
    public static AltarRecipeInstance deserialize(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
        AltarRecipeInstance instance = new AltarRecipeInstance(id, ContextAwarePredicate.ANY);
        JsonArray recipeNamesArr = GsonHelper.getAsJsonArray(json, "recipeNames", null);
        if (recipeNamesArr != null) {
            for (JsonElement element : recipeNamesArr) {
                instance.recipeNames.add(new ResourceLocation(element.getAsString()));
            }
        }
        JsonArray recipeOutputsArr = GsonHelper.getAsJsonArray(json, "recipeOutputs", null);
        if (recipeOutputsArr != null) {
            for (JsonElement element : recipeOutputsArr) {
                instance.recipeOutputs.add(Ingredient.fromJson(element));
            }
        }
        return instance;
    }

    public boolean test(@Nonnull SimpleAltarRecipe recipe, @Nonnull ItemStack output) {
        if (this.recipeNames.isEmpty() && this.recipeOutputs.isEmpty()) {
            return true;
        }
        if (this.recipeNames.contains(recipe.getId())) {
            return true;
        }
        for (Ingredient i : this.recipeOutputs) {
            if (i.test(output)) {
                return true;
            }
        }
        return false;
    }
}
