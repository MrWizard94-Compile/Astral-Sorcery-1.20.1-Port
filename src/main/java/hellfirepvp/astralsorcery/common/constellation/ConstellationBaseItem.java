package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Marker interface for items that carry a single associated constellation
 * (e.g. constellation paper, constellation-attuned crystals).
 *
 * <p>Used by the tome/container system to detect which papers or crystals
 * hold constellation data.</p>
 *
 * <p>1.16 → 1.20: ItemStack unchanged.</p>
 */
public interface ConstellationBaseItem {

    @Nullable
    IConstellation getConstellation(ItemStack stack);

    boolean setConstellation(ItemStack stack, @Nullable IConstellation cst);
}
