package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Rock Crystal — the primary crafting resource mined from ore.
 * Has crystal properties (size, purity, cutting) stored in NBT.
 *
 * <p>1.16 -> 1.20 changes:
 * ITextComponent -> Component,
 * addInformation -> appendHoverText,
 * ITooltipFlag -> TooltipFlag,
 * World -> Level</p>
 */
public class ItemRockCrystalSimple extends ItemAS {

    public ItemRockCrystalSimple() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // TODO: Display crystal properties (size, purity, cutting) from NBT
    }
}
