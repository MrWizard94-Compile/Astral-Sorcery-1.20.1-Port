package hellfirepvp.astralsorcery.common.util.block;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * A block predicate that matches either specific BlockStates or all states of a Block.
 * Supports config serialization via {@link BlockStateHelper}.
 *
 * <p>1.16 → 1.20 changes: World → Level, Block import path</p>
 */
public class SimpleBlockPredicate implements BlockPredicate, Predicate<BlockState> {

    final Either<List<BlockState>, Block> validMatch;

    public SimpleBlockPredicate(@Nonnull Block block) {
        this.validMatch = Either.right(block);
    }

    public SimpleBlockPredicate(@Nonnull BlockState... states) {
        this.validMatch = Either.left(Arrays.asList(states));
    }

    @Nonnull
    public List<String> getAsConfigList() {
        List<String> out = new ArrayList<>();
        this.validMatch
                .ifLeft(states -> states.stream()
                        .map(BlockStateHelper::serialize)
                        .forEach(out::add))
                .ifRight(block -> out.add(BlockStateHelper.serialize(block)));
        return out;
    }

    @Nullable
    public static SimpleBlockPredicate fromConfig(@Nonnull String serialized) {
        if (BlockStateHelper.isMissingStateInformation(serialized)) {
            Block b = BlockStateHelper.deserializeBlock(serialized);
            if (b != Blocks.AIR) {
                return new SimpleBlockPredicate(b);
            }
        } else {
            BlockState state = BlockStateHelper.deserialize(serialized);
            if (state.getBlock() != Blocks.AIR) {
                return new SimpleBlockPredicate(state);
            }
        }
        return null;
    }

    @Override
    public boolean test(BlockState state) {
        Either<Boolean, Boolean> matchResult = this.validMatch.mapBoth(
                states -> states.contains(state),
                block -> state.getBlock().equals(block));
        return matchResult.left().orElse(matchResult.right().orElse(false));
    }

    @Override
    public boolean test(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return this.test(state);
    }
}
