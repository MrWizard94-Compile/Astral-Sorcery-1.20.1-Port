/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.armor;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Mantle of Discidia — offense constellation.
 * Passive: Grants Strength I while worn.
 * Night bonus: Strength II under open sky at night.
 */
public class ItemMantleDiscidia extends ItemMantle {

    public ItemMantleDiscidia() {
        super(AstralSorcery.key("discidia"));
    }

    @Override
    public void onMantleTick(@Nonnull ItemStack stack, @Nonnull Level level,
                                 @Nonnull Player player) {
        int amplifier = 0;
        if (isNighttime(level) && isUnderOpenSky(player)) {
            amplifier = 1; // Strength II at night
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                40, amplifier, true, false));
    }
}
