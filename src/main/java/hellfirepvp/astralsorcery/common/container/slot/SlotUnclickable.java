package hellfirepvp.astralsorcery.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import javax.annotation.Nonnull;

/**
 * A display-only slot — the player cannot take items from it.
 * Used in {@link hellfirepvp.astralsorcery.common.container.ContainerTome}
 * to lock the tome's own slot so it cannot be moved while the GUI is open.
 *
 * <p>1.16 → 1.20: PlayerEntity → Player, IInventory → Container.</p>
 */
public class SlotUnclickable extends Slot {

    public SlotUnclickable(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(@Nonnull Player player) {
        return false;
    }
}
