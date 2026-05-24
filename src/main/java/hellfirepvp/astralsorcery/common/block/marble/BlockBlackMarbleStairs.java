package hellfirepvp.astralsorcery.common.block.marble;

import hellfirepvp.astralsorcery.common.block.BlackMarble;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class BlockBlackMarbleStairs extends StairBlock {

    public BlockBlackMarbleStairs(Supplier<BlockState> baseState) {
        super(baseState, BlackMarble.defaultProperties());
    }
}
