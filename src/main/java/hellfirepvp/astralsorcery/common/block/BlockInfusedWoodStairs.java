package hellfirepvp.astralsorcery.common.block;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class BlockInfusedWoodStairs extends StairBlock {

    public BlockInfusedWoodStairs(Supplier<BlockState> baseState) {
        super(baseState, BlockInfusedWood.defaultProperties());
    }
}
