/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.meltable;

import hellfirepvp.astralsorcery.common.crafting.nojson.CustomRecipe;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class WorldMeltableRecipe extends CustomRecipe {

    private final BlockPredicate matcher;

    public WorldMeltableRecipe(@Nonnull ResourceLocation key, @Nonnull BlockPredicate matcher) {
        super(key);
        this.matcher = matcher;
    }

    public boolean canMelt(@Nonnull Level level, @Nonnull BlockPos pos) {
        return this.matcher.test(level, pos, level.getBlockState(pos));
    }

    public abstract void doOutput(@Nonnull Level level, @Nonnull BlockPos pos,
                                  @Nonnull BlockState state, @Nonnull Consumer<ItemStack> itemOutput);
}
