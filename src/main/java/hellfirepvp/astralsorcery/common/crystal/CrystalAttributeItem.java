/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Marks an item that carries {@link CrystalAttributes} on its stack NBT.
 */
public interface CrystalAttributeItem {

    @Nullable
    CrystalAttributes getAttributes(@Nonnull ItemStack stack);

    void setAttributes(@Nonnull ItemStack stack, @Nullable CrystalAttributes attributes);
}
