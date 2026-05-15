package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

/**
 * Tool tier for Crystal tools — between diamond and netherite.
 *
 * <p>1.16 -> 1.20 changes:
 * IItemTier -> Tier interface,
 * getHarvestLevel() removed (tag-based in 1.20),
 * getTag() added to define mineable blocks</p>
 */
public class CrystalToolTier implements Tier {

    public static final CrystalToolTier INSTANCE = new CrystalToolTier();

    private CrystalToolTier() {}

    @Override
    public int getUses() {
        return 1024; // Base durability (modified by crystal size)
    }

    @Override
    public float getSpeed() {
        return 9.0F; // Between diamond (8) and netherite (9)
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.0F; // Same as diamond
    }

    @Override
    public int getLevel() {
        return 3; // Same as diamond (used for legacy compatibility only)
    }

    @Override
    public int getEnchantmentValue() {
        return 18; // Higher than diamond (10), lower than gold (22)
    }

    @Nonnull
    @Override
    public Ingredient getRepairIngredient() {
        // Repaired with rock crystals
        return Ingredient.of(ItemsAS.ROCK_CRYSTAL.get());
    }

    @Nonnull
    @Override
    public TagKey<Block> getTag() {
        // Crystal tools can mine everything diamond can
        return BlockTags.NEEDS_DIAMOND_TOOL;
    }
}
