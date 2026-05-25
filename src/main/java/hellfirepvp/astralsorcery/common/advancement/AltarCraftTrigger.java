/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.AltarRecipeInstance;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class AltarCraftTrigger extends SimpleCriterionTrigger<AltarRecipeInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("altar_craft");

    @Override
    @Nonnull
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    @Nonnull
    protected AltarRecipeInstance createInstance(@Nonnull JsonObject json,
                                                  @Nonnull ContextAwarePredicate player,
                                                  @Nonnull DeserializationContext context) {
        return AltarRecipeInstance.deserialize(getId(), json);
    }

    public void trigger(@Nonnull ServerPlayer player, @Nonnull SimpleAltarRecipe recipe,
                        @Nonnull ItemStack output) {
        super.trigger(player, instance -> instance.test(recipe, output));
    }
}
