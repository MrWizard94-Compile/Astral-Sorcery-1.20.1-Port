/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.astralsorcery.common.tile.BlockEntityWell;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Context for matching {@link WellLiquefaction} recipes.
 *
 * <p>1.16 → 1.20: TileWell → BlockEntityWell, getCatalyst() → getCatalystStack()</p>
 */
public class WellLiquefactionContext extends RecipeCraftingContext<WellLiquefaction, IItemHandler> {

    private final ItemStack input;

    public WellLiquefactionContext(BlockEntityWell well) {
        this(well.getCatalystStack());
    }

    public WellLiquefactionContext(ItemStack input) {
        this.input = input;
    }

    public ItemStack getInput() {
        return input;
    }
}
