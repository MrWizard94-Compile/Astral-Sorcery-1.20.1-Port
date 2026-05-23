package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Crystal Pickaxe — high-durability pickaxe made from rock crystals.
 * Crystal properties (size, purity, cutting) modify tool stats.
 *
 * <p>1.16 -> 1.20 changes:
 * ItemTier enum → Tiers enum,
 * PickaxeItem constructor uses (Tier, int attackDamage, float attackSpeed, Properties),
 * IItemTier → Tier interface,
 * Item.Properties.group removed (creative tab system)</p>
 */
public class ItemCrystalPickaxe extends PickaxeItem {

    public ItemCrystalPickaxe() {
        super(CrystalToolTier.INSTANCE, 1, -2.8F, new Properties());
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        float base = super.getDestroySpeed(stack, state);
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null ? base * CrystalCalculations.getToolSpeedMultiplier(props) : base;
    }

    @Override
    @SuppressWarnings("null")
    public int getMaxDamage(ItemStack stack) {
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null
                ? CrystalCalculations.getToolDurability(props, super.getMaxDamage(stack))
                : super.getMaxDamage(stack);
    }

    @Override
    @SuppressWarnings("null")
    public int getEnchantmentValue(ItemStack stack) {
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        if (props == null) return super.getEnchantmentValue(stack);
        // Scale from 10 (purity 0) to 25 (purity MAX_PURITY), matching purity influence
        return 10 + (int) ((props.getPurity() / (float) CrystalProperties.MAX_PURITY) * 15);
    }
}
