/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Merges two crystals in liquid starlight: after 40–60 ticks the starlight block is consumed,
 * the higher-tier crystal absorbs as many properties from the lower-tier one as fit within
 * the item's max property cap (with diminishing probability per transferred tier).
 *
 * <p>1.16 → 1.20: World → Level, setBlockState → setBlock,
 * MathHelper.getPositionRandom → RandomSource.create,
 * getPosX/Y/Z → getX/Y/Z, ItemUtils.dropItemNaturally unchanged.</p>
 */
public class MergeCrystalsRecipe extends LiquidStarlightRecipe {

    public MergeCrystalsRecipe() {
        super(AstralSorcery.key("merge_crystals"));
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
        List<Entity> others = getEntitiesInBlock(level, at);
        others.remove(trigger);
        Optional<Entity> crystalEntity = others.stream()
                .filter(e -> e instanceof ItemEntity)
                .filter(e -> ((ItemEntity) e).getItem().getItem() instanceof ItemCrystalBase)
                .findFirst();
        return crystalEntity.isPresent() && others.size() == 1;
    }

    @Override
    public void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        long seed = at.asLong();
        RandomSource r = RandomSource.create(seed);
        if (getAndIncrementCraftingTick(trigger) <= 40 + r.nextInt(20)) return;

        ItemStack stackOne = consumeItemEntityInBlock(level, at, 1,
                stack -> stack.getItem() instanceof ItemCrystalBase);
        if (stackOne == null) return;
        ItemStack stackTwo = consumeItemEntityInBlock(level, at, 1,
                stack -> stack.getItem() instanceof ItemCrystalBase);
        if (stackTwo == null) return;
        if (!level.setBlock(at, Blocks.AIR.defaultBlockState(), 3)) return;

        ItemCrystalBase crystalOne = (ItemCrystalBase) stackOne.getItem();
        CrystalAttributes attrOne = crystalOne.getAttributes(stackOne);
        attrOne = attrOne != null ? attrOne : CrystalAttributes.Builder.newBuilder(false).build();

        ItemCrystalBase crystalTwo = (ItemCrystalBase) stackTwo.getItem();
        CrystalAttributes attrTwo = crystalTwo.getAttributes(stackTwo);
        attrTwo = attrTwo != null ? attrTwo : CrystalAttributes.Builder.newBuilder(false).build();

        // Higher-tier crystal is the base; lower-tier donates properties
        CrystalAttributes mergeTo   = attrOne.getTotalTierLevel() >= attrTwo.getTotalTierLevel() ? attrOne : attrTwo;
        CrystalAttributes mergeFrom = attrOne.getTotalTierLevel() >= attrTwo.getTotalTierLevel() ? attrTwo : attrOne;
        ItemStack resultStack       = attrOne.getTotalTierLevel() >= attrTwo.getTotalTierLevel() ? stackOne.copy() : stackTwo.copy();
        ItemCrystalBase resultCrystal = (ItemCrystalBase) resultStack.getItem();

        int maxTiers = ((CrystalAttributeGenItem) resultCrystal).getMaxPropertyTiers();
        int freeProperties = maxTiers - mergeTo.getTotalTierLevel();
        int copyAmount = Math.min(freeProperties, mergeFrom.getTotalTierLevel());

        CrystalAttributes.Builder resultBuilder = CrystalAttributes.Builder.newBuilder(false).addAll(mergeTo);
        int mergeCount = 0;
        for (int i = 0; i < copyAmount; i++) {
            CrystalAttributes.Attribute attr = MiscUtils.getWeightedRandomEntry(
                    mergeFrom.getCrystalAttributes(), rand, CrystalAttributes.Attribute::getTier);
            if (attr != null) {
                mergeFrom = mergeFrom.modifyLevel(attr.getProperty(), -1);
                if (rand.nextFloat() <= (1F - Math.min(mergeCount, 3) * 0.25F)) {
                    resultBuilder.addProperty(attr.getProperty(), 1);
                }
                mergeCount++;
            }
        }

        resultCrystal.setAttributes(resultStack, resultBuilder.build());
        ItemUtils.dropItemNaturally(level, trigger.getX(), trigger.getY(), trigger.getZ(), resultStack);
    }
}
