/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.freezing;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicates;
import hellfirepvp.astralsorcery.common.util.block.WorldBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class BlockFreezingRecipe extends WorldFreezingRecipe {

    private final BiFunction<WorldBlockPos, BlockState, BlockState> outputGenerator;

    public BlockFreezingRecipe(@Nonnull ResourceLocation key, @Nonnull BlockPredicate matcher,
                               @Nonnull BlockState output) {
        this(key, matcher, (worldPos, state) -> output);
    }

    public BlockFreezingRecipe(@Nonnull ResourceLocation key, @Nonnull BlockPredicate matcher,
                               @Nonnull BiFunction<WorldBlockPos, BlockState, BlockState> outputGenerator) {
        super(key, matcher);
        this.outputGenerator = outputGenerator;
    }

    @Nonnull
    public static BlockFreezingRecipe of(@Nonnull BlockState stateIn, @Nonnull BlockState stateOut) {
        return new BlockFreezingRecipe(
                AstralSorcery.key(stateIn.getBlock().builtInRegistryHolder().key().location().getPath()),
                BlockPredicates.isState(stateIn), stateOut);
    }

    @Nonnull
    public static BlockFreezingRecipe of(@Nonnull Block blockIn, @Nonnull BlockState stateOut) {
        return new BlockFreezingRecipe(
                AstralSorcery.key(blockIn.builtInRegistryHolder().key().location().getPath()),
                BlockPredicates.isBlock(blockIn), stateOut);
    }

    @Nonnull
    public static BlockFreezingRecipe of(@Nonnull TagKey<Block> blockTagIn, @Nonnull BlockState stateOut) {
        return new BlockFreezingRecipe(
                AstralSorcery.key(String.format("tag_%s", blockTagIn.location().getPath())),
                BlockPredicates.isInTag(blockTagIn), stateOut);
    }

    @Override
    public void doOutput(@Nonnull Level level, @Nonnull BlockPos pos,
                         @Nonnull BlockState state, @Nonnull Consumer<ItemStack> itemOutput) {
        BlockState generated = this.outputGenerator.apply(WorldBlockPos.wrapServer(level, pos), state);
        if (generated != state) {
            level.setBlock(pos, generated, Block.UPDATE_ALL);
        }
    }
}
