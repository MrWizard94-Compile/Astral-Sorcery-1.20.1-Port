package hellfirepvp.astralsorcery.common.util.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Functional interface for testing blocks in the world.
 *
 * <p>1.16 → 1.20 renames: World → Level.</p>
 */
public interface BlockPredicate {

    boolean test(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state);
}
