package hellfirepvp.astralsorcery.common.util.tile;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

/**
 * Interface for block entities that provide a display name for their inventory.
 *
 * <p>1.16 -> 1.20 changes:
 * ITextComponent -> Component</p>
 */
public interface NamedInventoryTile {

    @Nonnull
    Component getDisplayName();
}
