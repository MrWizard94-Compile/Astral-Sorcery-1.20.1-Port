/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Context for matching {@link BlockTransmutation} recipes.
 *
 * <p>1.16 → 1.20: IWorld → LevelAccessor, BlockState unchanged, constellation nullable</p>
 */
public class BlockTransmutationContext extends RecipeCraftingContext<BlockTransmutation, IItemHandler> {

    private final LevelAccessor world;
    private final BlockPos pos;
    private final BlockState state;
    @Nullable
    private final IWeakConstellation constellation;

    public BlockTransmutationContext(LevelAccessor world, BlockPos pos, BlockState state,
                                     @Nullable IWeakConstellation constellation) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.constellation = constellation;
    }

    public LevelAccessor getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    @Nullable
    public IWeakConstellation getConstellation() {
        return constellation;
    }
}
