/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.item.ItemIlluminationPowder;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Forms a gem crystal cluster block from an Illumination Powder item entity and
 * a crystal item entity dropped together in liquid starlight.
 *
 * <p>Trigger: the Illumination Powder entity triggers the recipe.
 * Requires exactly one other entity in the same block — a crystal.
 * After 50–69 ticks both items are consumed and a GEM_CRYSTAL_CLUSTER block is placed.</p>
 *
 * <p>1.16 → 1.20: MathHelper.getPositionRandom(at) → RandomSource.create(at.asLong()),
 * isTopSolid() → isFaceSturdy(level, pos, Direction.UP),
 * world.setBlockState() → level.setBlock().</p>
 */
public class FormGemCrystalClusterRecipe extends LiquidStarlightRecipe {

    public FormGemCrystalClusterRecipe() {
        super(AstralSorcery.key("form_gem_crystal_cluster"));
    }

    @Override
    @Nonnull
    public List<Ingredient> getInputForRender() {
        return Arrays.asList(
                Ingredient.of(ItemsAS.ILLUMINATION_POWDER.get()),
                Ingredient.of(ItemsAS.ROCK_CRYSTAL.get()));
    }

    @Override
    @Nonnull
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(Ingredient.of(BlocksAS.GEM_CRYSTAL_CLUSTER.get()));
    }

    @Override
    public boolean doesStartRecipe(@Nonnull ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof ItemIlluminationPowder;
    }

    @Override
    public boolean matches(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
            return false;
        }
        List<Entity> others = getEntitiesInBlock(level, at);
        others.remove(trigger);
        boolean hasCrystal = others.stream()
                .filter(e -> e instanceof ItemEntity)
                .anyMatch(e -> ((ItemEntity) e).getItem().getItem() instanceof ItemCrystalBase);
        return hasCrystal && others.size() == 1;
    }

    @Override
    public void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        RandomSource r = RandomSource.create(at.asLong());
        if (getAndIncrementCraftingTick(trigger) <= 50 + r.nextInt(20)) return;

        ItemStack powder = consumeItemEntityInBlock(level, at, 1,
                stack -> stack.getItem() instanceof ItemIlluminationPowder);
        if (powder == null) return;

        ItemStack crystal = consumeItemEntityInBlock(level, at, 1,
                stack -> stack.getItem() instanceof ItemCrystalBase);
        if (crystal == null) return;

        level.setBlock(at, BlocksAS.GEM_CRYSTAL_CLUSTER.get().defaultBlockState(), 3);
    }
}
