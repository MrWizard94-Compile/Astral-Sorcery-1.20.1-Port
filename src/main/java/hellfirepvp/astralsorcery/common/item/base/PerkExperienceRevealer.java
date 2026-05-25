/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.world.item.ItemStack;

/**
 * Marker interface for items that should reveal the perk experience HUD
 * overlay while held. Implement on items that are closely related to
 * the perk system (e.g. the tome, resonator).
 */
public interface PerkExperienceRevealer {

    default boolean shouldReveal(ItemStack stack) {
        return true;
    }
}
