package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Crystal Shovel — durability and dig speed scale with crystal properties.
 */
public class ItemCrystalShovel extends ShovelItem {

    public ItemCrystalShovel() {
        super(CrystalToolTier.INSTANCE, 1.5F, -3.0F, new Properties());
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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        if (props != null) {
            tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.size",    props.getSize()));
            tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.purity",  props.getPurity()));
            tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.cutting", props.getCutting()));
        }
    }
}
