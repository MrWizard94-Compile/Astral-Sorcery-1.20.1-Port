package hellfirepvp.astralsorcery.common.perk.node.socket;

import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Implemented by items that can be inserted into a
 * {@link hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk}.
 * The modifiers returned here are merged with the socket perk's own base
 * modifiers to determine the effective bonuses granted to the player.
 */
public interface IPerkGem {

    /**
     * Returns the perk attribute modifiers provided by this gem when socketed.
     *
     * @param stack the gem item stack (may carry NBT-rolled stats)
     * @return the list of modifiers; empty if the gem provides none
     */
    @Nonnull
    List<PerkAttributeModifier> getPerkModifiers(@Nonnull ItemStack stack);
}
