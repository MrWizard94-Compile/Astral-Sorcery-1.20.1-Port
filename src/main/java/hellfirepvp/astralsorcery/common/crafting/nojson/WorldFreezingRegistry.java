/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson;

import hellfirepvp.astralsorcery.common.crafting.nojson.freezing.BlockFreezingRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.freezing.FluidFreezingRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.freezing.WorldFreezingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldFreezingRegistry extends CustomRecipeRegistry<WorldFreezingRecipe> {

    public static final WorldFreezingRegistry INSTANCE = new WorldFreezingRegistry();

    @Override
    public void init() {
        this.register(BlockFreezingRecipe.of(Blocks.FIRE, Blocks.AIR.defaultBlockState()));
        this.register(BlockFreezingRecipe.of(Blocks.AIR.defaultBlockState(), Blocks.ICE.defaultBlockState()));
        this.register(BlockFreezingRecipe.of(Blocks.CAVE_AIR.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState()));
        this.register(BlockFreezingRecipe.of(BlockTags.ICE, Blocks.ICE.defaultBlockState()));
        this.register(new FluidFreezingRecipe());
    }

    @Nullable
    public WorldFreezingRecipe getRecipeFor(@Nonnull Level level, @Nonnull BlockPos pos) {
        return this.getRecipes()
                .stream()
                .filter(recipe -> recipe.canFreeze(level, pos))
                .findFirst()
                .orElse(null);
    }
}
