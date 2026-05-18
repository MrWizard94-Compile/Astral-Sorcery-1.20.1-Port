/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.meltable;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

public class FurnaceMeltableRecipe extends ItemMeltableRecipe {

    public FurnaceMeltableRecipe() {
        super(AstralSorcery.key("all_furnace_meltable"),
                (level, pos, state) -> RecipeHelper.findSmeltingResult(level, state).isPresent(),
                (worldPos, state) -> RecipeHelper.findSmeltingResult(worldPos.getWorld(), state)
                        .map(Tuple::getA)
                        .orElse(ItemStack.EMPTY));
    }
}
