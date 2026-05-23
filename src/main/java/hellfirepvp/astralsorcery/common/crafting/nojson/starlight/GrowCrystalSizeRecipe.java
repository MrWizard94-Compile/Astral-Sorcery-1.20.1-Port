/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Grows a crystal's Size property by 1 when it soaks alone in liquid starlight.
 * After 80-120 ticks the starlight block is consumed and the crystal gains one Size tier
 * (unless it is already at max property tiers).
 *
 * <p>1.16 → 1.20: World → Level, MathHelper.getPositionRandom → RandomSource.create(seed),
 * Blocks.AIR.getDefaultState() → Blocks.AIR.defaultBlockState(),
 * world.setBlockState → level.setBlock.</p>
 */
public class GrowCrystalSizeRecipe extends LiquidStarlightRecipe {

    public GrowCrystalSizeRecipe() {
        super(AstralSorcery.key("crystal_grow"));
    }

    @Override
    @Nonnull
    public List<Ingredient> getInputForRender() {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<Ingredient> getOutputForRender() {
        return Collections.emptyList();
    }

    @Override
    public boolean doesStartRecipe(@Nonnull ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof ItemCrystalBase;
    }

    @Override
    public boolean matches(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        List<net.minecraft.world.entity.Entity> others = getEntitiesInBlock(level, at);
        others.remove(trigger);
        return others.isEmpty();
    }

    @Override
    public void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        long seed = at.asLong();
        RandomSource r = RandomSource.create(seed);
        if (getAndIncrementCraftingTick(trigger) <= 80 + r.nextInt(40)) return;

        ItemStack stack = trigger.getItem();
        if (!(stack.getItem() instanceof ItemCrystalBase crystal)) return;

        CrystalAttributes attr = crystal.getAttributes(stack);
        if (attr == null) return;
        if (!level.setBlock(at, Blocks.AIR.defaultBlockState(), 3)) return;

        int maxTiers = ((CrystalAttributeGenItem) crystal).getMaxPropertyTiers();
        if (attr.getTotalTierLevel() >= maxTiers) return;

        CrystalAttributes grown = attr.modifyLevel(CrystalPropertiesAS.Properties.PROPERTY_SIZE, 1);
        crystal.setAttributes(stack, grown);
    }
}
