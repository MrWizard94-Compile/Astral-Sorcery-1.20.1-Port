/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.socket;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Implemented by items that can be inserted into a {@link hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk}.
 * Provides modifier contributions and lifecycle callbacks.
 *
 * <p>1.16 → 1.20: DynamicAttributeModifier → PerkAttributeModifier; IFormattableTextComponent → Component.</p>
 */
public interface GemSocketItem extends IPerkGem {

    /**
     * Called server-side when this gem is inserted into a socket.
     */
    default void onInsert(@Nonnull ItemStack stack,
                           @Nonnull hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk perk,
                           @Nonnull Player player,
                           @Nonnull PlayerProgress progress) {}

    /**
     * Called server-side when this gem is removed from a socket.
     */
    default void onExtract(@Nonnull ItemStack stack,
                            @Nonnull hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk perk,
                            @Nonnull Player player,
                            @Nonnull PlayerProgress progress) {}

    /**
     * Whether this gem can currently be inserted into the given socket.
     */
    default boolean canBeInserted(@Nonnull ItemStack stack,
                                   @Nonnull hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk perk,
                                   @Nonnull Player player,
                                   @Nonnull PlayerProgress progress,
                                   @Nonnull LogicalSide side) {
        return true;
    }

    /**
     * Adds tooltip lines when the gem is viewed in the perk UI.
     */
    default void addGemTooltip(@Nonnull ItemStack stack,
                                @Nonnull hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk perk,
                                @Nonnull List<Component> tooltip) {}
}
