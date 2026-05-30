package hellfirepvp.astralsorcery.common.container.slot;

import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * Slot that only accepts constellation paper with an assigned constellation.
 * Used in the Tome container to let players store discovered constellations.
 * Fires {@link hellfirepvp.astralsorcery.common.container.ContainerTome#slotChanged()}
 * so the research manager updates the player's known constellations.
 *
 * <p>1.16 → 1.20: slot change notification unchanged.</p>
 */
public class SlotConstellationPaper extends SlotItemHandler {

    private final Runnable changeListener;

    public SlotConstellationPaper(IItemHandler handler, int index,
                                   int xPosition, int yPosition,
                                   Runnable changeListener) {
        super(handler, index, xPosition, yPosition);
        this.changeListener = changeListener;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof ItemConstellationPaper)) {
            return false;
        }
        return ItemConstellationPaper.getConstellation(stack) != null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        changeListener.run();
    }
}
