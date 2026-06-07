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
 * Mantle of Evorsio — mining/breaking constellation.
 * Passive: Grants Haste I while worn.
 * Night bonus: Haste II under open sky at night.
 */
public class ItemMantleEvorsio extends ItemMantle {

    public ItemMantleEvorsio() {
        super(AstralSorcery.key("evorsio"));
    }

    @Override
    public void onMantleTick(@Nonnull ItemStack stack, @Nonnull Level level,
                                 @Nonnull Player player) {
        int amplifier = 0;
        if (isNighttime(level) && isUnderOpenSky(player)) {
            amplifier = 1;
        }
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,
                40, amplifier, true, false));
    }
}
