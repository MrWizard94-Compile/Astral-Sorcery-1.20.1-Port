package hellfirepvp.astralsorcery.client.util;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client-only utilities for working with {@link Ingredient} display.
 *
 * <p>1.16 -> 1.20 changes:
 * MathHelper -> Mth,
 * ingredient.hasNoMatchingItems() -> ingredient.isEmpty(),
 * ingredient.getMatchingStacks() -> ingredient.getItems()</p>
 */
public class IngredientHelper {

    @Nonnull
    public static ItemStack getRandomVisibleStack(@Nonnull Ingredient ingredient) {
        return getRandomVisibleStack(ingredient, 0);
    }

    @Nonnull
    public static ItemStack getRandomVisibleStack(@Nonnull Ingredient ingredient, long tick) {
        List<ItemStack> applicable = getVisibleItemStacks(ingredient);
        if (applicable.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int mod = (int) ((tick / 20L) % applicable.size());
        return applicable.get(Mth.clamp(mod, 0, applicable.size() - 1));
    }

    @Nonnull
    public static List<ItemStack> getVisibleItemStacks(@Nonnull Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(ingredient.getItems());
    }
}
