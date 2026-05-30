package hellfirepvp.astralsorcery.common.item.base;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Marker for items that can serve as a constellation focus in an altar.
 * The focus biases recipe matching toward a specific constellation,
 * enabling constellation-locked recipes at the Constellation and Radiance tiers.
 *
 * <p>1.16 → 1.20: ItemStack unchanged.</p>
 */
public interface IConstellationFocus {

    @Nullable
    IConstellation getFocusConstellation(ItemStack stack);
}
