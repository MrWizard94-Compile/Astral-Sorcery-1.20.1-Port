/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ConstellationItem {

    @Nullable
    IWeakConstellation getAttunedConstellation(ItemStack stack);

    boolean setAttunedConstellation(ItemStack stack, @Nullable IWeakConstellation cst);

    @Nullable
    IMinorConstellation getTraitConstellation(ItemStack stack);

    boolean setTraitConstellation(ItemStack stack, @Nullable IMinorConstellation cst);
}
