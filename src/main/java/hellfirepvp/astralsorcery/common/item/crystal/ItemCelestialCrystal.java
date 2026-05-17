package hellfirepvp.astralsorcery.common.item.crystal;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Celestial Crystal — an upgraded version of Rock Crystal obtained by
 * growing crystals in liquid starlight under starlight exposure.
 *
 * <p>Has the same property system (size, purity, cutting) as Rock Crystal
 * but with higher base values. Celestial crystals don't degrade during
 * crafting (fracture-proof) and have a 20% quality bonus.</p>
 *
 * <p>1.16 -> 1.20 changes: Same as ItemRockCrystalSimple.</p>
 */
public class ItemCelestialCrystal extends ItemRockCrystalSimple {

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.astralsorcery.celestial_crystal.fracture_proof"));
    }

    @Nonnull
    @Override
    public Rarity getRarity(@Nonnull ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return true; // Always enchanted glint
    }

    /**
     * Celestial crystals have a higher quality multiplier (20% bonus).
     */
    public static float getCelestialQualityFactor(@Nonnull ItemStack stack) {
        return Math.min(1.0f, getQualityFactor(stack) * 1.2f);
    }
}
