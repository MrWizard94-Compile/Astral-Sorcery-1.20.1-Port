package hellfirepvp.astralsorcery.common.item.crystal;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Celestial Crystal — higher tier crystal grown from Rock Crystal in liquid starlight.
 * Has the same property system as Rock Crystal but with better base stats.
 *
 * <p>1.16 -> 1.20 changes: Same as ItemRockCrystalSimple.</p>
 */
public class ItemCelestialCrystal extends ItemRockCrystalSimple {

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // TODO: Display enhanced crystal properties
    }
}
