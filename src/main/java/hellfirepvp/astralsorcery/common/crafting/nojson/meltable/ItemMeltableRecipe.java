/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.meltable;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicates;
import hellfirepvp.astralsorcery.common.util.block.WorldBlockPos;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
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

public class ItemMeltableRecipe extends WorldMeltableRecipe {

    private final BiFunction<WorldBlockPos, BlockState, ItemStack> outputGenerator;

    public ItemMeltableRecipe(@Nonnull ResourceLocation key, @Nonnull BlockPredicate matcher,
                              @Nonnull ItemStack output) {
        this(key, matcher, (worldPos, state) -> ItemUtils.copyStackWithSize(output, output.getCount()));
    }

    public ItemMeltableRecipe(@Nonnull ResourceLocation key, @Nonnull BlockPredicate matcher,
                              @Nonnull BiFunction<WorldBlockPos, BlockState, ItemStack> outputGenerator) {
        super(key, matcher);
        this.outputGenerator = outputGenerator;
    }

    @Nonnull
    public static ItemMeltableRecipe of(@Nonnull BlockState stateIn, @Nonnull ItemStack itemOut) {
        return new ItemMeltableRecipe(
                AstralSorcery.key(stateIn.getBlock().builtInRegistryHolder().key().location().getPath()),
                BlockPredicates.isState(stateIn), itemOut);
    }

    @Nonnull
    public static ItemMeltableRecipe of(@Nonnull TagKey<Block> blockTagIn, @Nonnull ItemStack itemOut) {
        return new ItemMeltableRecipe(
                AstralSorcery.key(String.format("tag_%s", blockTagIn.location().getPath())),
                BlockPredicates.isInTag(blockTagIn), itemOut);
    }

    @Override
    public void doOutput(@Nonnull Level level, @Nonnull BlockPos pos,
                         @Nonnull BlockState state, @Nonnull Consumer<ItemStack> itemOutput) {
        if (level.removeBlock(pos, false)) {
            ItemStack generated = this.outputGenerator.apply(WorldBlockPos.wrapServer(level, pos), state);
            if (!generated.isEmpty()) {
                itemOutput.accept(generated);
            }
        }
    }
}
