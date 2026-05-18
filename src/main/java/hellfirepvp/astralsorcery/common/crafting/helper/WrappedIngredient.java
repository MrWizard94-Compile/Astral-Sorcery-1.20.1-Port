/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import hellfirepvp.astralsorcery.common.util.IngredientHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nullable;

/**
 * Wrapper around {@link Ingredient} that supports NBT round-trip serialization
 * and animated display in GUIs.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag; JsonParser usage unchanged.</p>
 */
public class WrappedIngredient {

    private final Ingredient ingredient;

    public WrappedIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getRandomMatchingStack(long tick) {
        return IngredientHelper.getRandomVisibleStack(this.getIngredient(), tick);
    }

    @Nullable
    public static WrappedIngredient deserialize(CompoundTag tag) {
        if (!tag.contains("ingredient")) {
            return null;
        }
        JsonElement jsonElement = JsonParser.parseString(tag.getString("ingredient"));
        return new WrappedIngredient(CraftingHelper.getIngredient(jsonElement, true));
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ingredient", this.ingredient.toJson().toString());
        return tag;
    }
}
