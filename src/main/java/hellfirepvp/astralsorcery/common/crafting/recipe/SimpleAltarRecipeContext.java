/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Context for matching {@link SimpleAltarRecipe} recipes.
 *
 * <p>1.16 → 1.20: TileAltar → BlockEntityAltar, PlayerEntity → Player</p>
 */
public class SimpleAltarRecipeContext extends RecipeCraftingContext<SimpleAltarRecipe, IItemHandler> {

    private final BlockEntityAltar altar;
    @Nullable
    private final Player crafter;
    private final LogicalSide side;
    private boolean ignoreStarlightRequirement = false;

    public SimpleAltarRecipeContext(BlockEntityAltar altar, @Nullable Player crafter, LogicalSide side) {
        this.altar = altar;
        this.crafter = crafter;
        this.side = side;
    }

    public BlockEntityAltar getAltar() {
        return altar;
    }

    @Nullable
    public Player getCrafter() {
        return crafter;
    }

    public LogicalSide getSide() {
        return side;
    }

    public boolean isIgnoreStarlightRequirement() {
        return ignoreStarlightRequirement;
    }

    public void setIgnoreStarlightRequirement(boolean ignoreStarlightRequirement) {
        this.ignoreStarlightRequirement = ignoreStarlightRequirement;
    }
}
