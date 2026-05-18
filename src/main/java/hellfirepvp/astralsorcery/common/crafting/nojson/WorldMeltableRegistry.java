/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson;

import hellfirepvp.astralsorcery.common.crafting.nojson.meltable.BlockMeltableRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.meltable.FurnaceMeltableRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.meltable.WorldMeltableRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldMeltableRegistry extends CustomRecipeRegistry<WorldMeltableRecipe> {

    public static final WorldMeltableRegistry INSTANCE = new WorldMeltableRegistry();

    @Override
    public void init() {
        this.register(BlockMeltableRecipe.of(BlockTags.ICE, Blocks.WATER.defaultBlockState()));
        this.register(BlockMeltableRecipe.of(Tags.Blocks.STONE, Blocks.LAVA.defaultBlockState()));
        this.register(BlockMeltableRecipe.of(Tags.Blocks.NETHERRACK, Blocks.LAVA.defaultBlockState()));
        this.register(BlockMeltableRecipe.of(Tags.Blocks.OBSIDIAN, Blocks.LAVA.defaultBlockState()));
        this.register(BlockMeltableRecipe.of(Blocks.MAGMA_BLOCK.defaultBlockState(), Blocks.LAVA.defaultBlockState()));
        this.register(new FurnaceMeltableRecipe());
    }

    @Nullable
    public WorldMeltableRecipe getRecipeFor(@Nonnull Level level, @Nonnull BlockPos pos) {
        return this.getRecipes()
                .stream()
                .filter(recipe -> recipe.canMelt(level, pos))
                .findFirst()
                .orElse(null);
    }
}
