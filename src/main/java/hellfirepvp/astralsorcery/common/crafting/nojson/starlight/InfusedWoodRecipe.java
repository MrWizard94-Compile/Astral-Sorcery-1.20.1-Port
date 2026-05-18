/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class InfusedWoodRecipe extends LiquidStarlightRecipe {

    public InfusedWoodRecipe() {
        super(AstralSorcery.key("infused_wood"));
    }

    @Override
    @Nonnull
    public List<Ingredient> getInputForRender() {
        return Collections.singletonList(Ingredient.of(ItemTags.LOGS));
    }

    @Override
    @Nonnull
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(Ingredient.of(BlocksAS.INFUSED_WOOD.get()));
    }

    @Override
    public boolean doesStartRecipe(@Nonnull ItemStack item) {
        return item.is(ItemTags.LOGS);
    }

    @Override
    public boolean matches(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        return true;
    }

    @Override
    public void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        if (getAndIncrementCraftingTick(trigger) > 5) {
            if (consumeItemEntityInBlock(level, at, 1, stack -> stack.is(ItemTags.LOGS)) != null) {
                ItemUtils.dropItemNaturally(level, trigger.getX(), trigger.getY(), trigger.getZ(),
                        new ItemStack(BlocksAS.INFUSED_WOOD.get()));
            }
        }
    }
}
