/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.astralsorcery.common.tile.BlockEntityInfuser;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Context for matching {@link LiquidInfusion} recipes.
 *
 * <p>1.16 → 1.20: TileInfuser → BlockEntityInfuser, PlayerEntity → Player</p>
 */
public class LiquidInfusionContext extends RecipeCraftingContext<LiquidInfusion, IItemHandler> {

    private final BlockEntityInfuser infuser;
    @Nullable
    private final Player crafter;
    private final LogicalSide side;

    public LiquidInfusionContext(BlockEntityInfuser infuser, @Nullable Player crafter, LogicalSide side) {
        this.infuser = infuser;
        this.crafter = crafter;
        this.side = side;
    }

    public BlockEntityInfuser getInfuser() {
        return infuser;
    }

    @Nullable
    public Player getCrafter() {
        return crafter;
    }

    public LogicalSide getSide() {
        return side;
    }
}
