/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.item.ItemStardust;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityCelestialCrystals;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
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
 * Transforms a Stardust item entity and a crystal item entity dropped together in
 * liquid starlight into a Celestial Crystal Cluster block.
 *
 * <p>Trigger: the Stardust entity triggers the recipe. Requires exactly one other
 * entity in the same block — a crystal. After 50–69 ticks both are consumed and a
 * CELESTIAL_CRYSTAL_CLUSTER block is placed.</p>
 *
 * <p>1.16 → 1.20: MathHelper.getPositionRandom(at) → RandomSource.create(at.asLong()),
 * isTopSolid() → isFaceSturdy(level, pos, Direction.UP),
 * world.setBlockState() → level.setBlock().</p>
 */
public class FormCelestialCrystalClusterRecipe extends LiquidStarlightRecipe {

    public FormCelestialCrystalClusterRecipe() {
        super(AstralSorcery.key("form_celestial_crystal_cluster"));
    }

    @Override
    @Nonnull
    public List<Ingredient> getInputForRender() {
        return Arrays.asList(
                Ingredient.of(ItemsAS.STARDUST.get()),
                Ingredient.of(ItemsAS.ROCK_CRYSTAL.get()));
    }

    @Override
    @Nonnull
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(Ingredient.of(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get()));
    }

    @Override
    public boolean doesStartRecipe(@Nonnull ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof ItemStardust;
    }

    @Override
    public boolean matches(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
            return false;
        }
        List<Entity> others = getEntitiesInBlock(level, at);
        others.remove(trigger);
        if (others.size() != 1) return false;
        int purityThreshold = CommonConfig.CONFIG.celestialPurityThreshold.get();
        boolean hasCrystal = others.stream()
                .filter(e -> e instanceof ItemEntity)
                .anyMatch(e -> {
                    ItemStack stack = ((ItemEntity) e).getItem();
                    if (!(stack.getItem() instanceof CrystalAttributeItem crystalItem)) return false;
                    if (!(stack.getItem() instanceof ItemCrystalBase)) return false;
                    CrystalAttributes attrs = crystalItem.getAttributes(stack);
                    if (attrs == null) return purityThreshold == 0;
                    CrystalProperties props = CrystalProperties.deriveFrom(attrs);
                    return props.getPurity() >= purityThreshold;
                });
        return hasCrystal;
    }

    @Override
    public void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        RandomSource r = RandomSource.create(at.asLong());
        if (getAndIncrementCraftingTick(trigger) <= 50 + r.nextInt(20)) return;

        ItemStack stardust = consumeItemEntityInBlock(level, at, 1,
                stack -> stack.getItem() instanceof ItemStardust);
        if (stardust == null) return;

        ItemStack crystal = consumeItemEntityInBlock(level, at, 1,
                stack -> stack.getItem() instanceof ItemCrystalBase);
        if (crystal == null) return;

        level.setBlock(at, BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get().defaultBlockState(), 3);
        if (crystal.getItem() instanceof CrystalAttributeItem) {
            BlockEntityCelestialCrystals cluster = MiscUtils.getTileAt(level, at,
                    BlockEntityCelestialCrystals.class, true);
            if (cluster != null) {
                CrystalAttributes attr = CrystalGenerator.upgradeProperties(crystal);
                cluster.setAttributes(attr);
            }
        }
    }
}
