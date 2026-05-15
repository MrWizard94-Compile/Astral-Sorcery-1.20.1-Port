package hellfirepvp.astralsorcery.common.util;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for working with {@link Ingredient} display on clients.
 *
 * <p>1.16 -> 1.20 changes:
 * MathHelper -> Mth,
 * ingredient.hasNoMatchingItems() -> ingredient.isEmpty(),
 * ingredient.getMatchingStacks() -> ingredient.getItems(),
 * Tag/ITag guessing removed (tag system restructured; use TagKey directly),
 * ItemStack unchanged</p>
 */
public class IngredientHelper {

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public static ItemStack getRandomVisibleStack(@Nonnull Ingredient ingredient) {
        return getRandomVisibleStack(ingredient, 0);
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public static ItemStack getRandomVisibleStack(@Nonnull Ingredient ingredient, long tick) {
        List<ItemStack> applicable = getVisibleItemStacks(ingredient);
        if (applicable.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int mod = (int) ((tick / 20L) % applicable.size());
        return applicable.get(Mth.clamp(mod, 0, applicable.size() - 1));
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public static List<ItemStack> getVisibleItemStacks(@Nonnull Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(ingredient.getItems());
    }
}
