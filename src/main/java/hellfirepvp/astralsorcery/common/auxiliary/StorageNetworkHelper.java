package hellfirepvp.astralsorcery.common.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for interacting with adjacent inventories via capabilities.
 * Used by starlight-infused relay networks and ritual pedestals.
 *
 * <p>1.16 -> 1.20 changes:
 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> ForgeCapabilities.ITEM_HANDLER,
 * TileEntity -> BlockEntity,
 * pos.offset(dir) -> pos.relative(dir)</p>
 */
public class StorageNetworkHelper {

    private StorageNetworkHelper() {}

    /**
     * Find all adjacent item handlers around the given position.
     *
     * @param level the level
     * @param pos   the center position
     * @return list of item handlers found on adjacent faces
     */
    @Nonnull
    public static List<IItemHandler> findAdjacentHandlers(@Nonnull Level level, @Nonnull BlockPos pos) {
        List<IItemHandler> handlers = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            IItemHandler handler = getItemHandler(level, pos.relative(dir), dir.getOpposite());
            if (handler != null) {
                handlers.add(handler);
            }
        }
        return handlers;
    }

    /**
     * Get the item handler capability from a block entity on a specific side.
     *
     * @param level the level
     * @param pos   the block entity position
     * @param side  the face to query
     * @return the item handler, or null if not available
     */
    @Nullable
    public static IItemHandler getItemHandler(@Nonnull Level level,
                                              @Nonnull BlockPos pos,
                                              @Nullable Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        return be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve().orElse(null);
    }

    /**
     * Attempt to insert an item stack into any available adjacent inventory.
     *
     * @param level    the level
     * @param pos      the center position
     * @param stack    the item stack to insert
     * @param simulate if true, don't actually insert
     * @return the remainder that could not be inserted (empty if fully inserted)
     */
    @Nonnull
    public static ItemStack insertIntoAdjacent(@Nonnull Level level,
                                               @Nonnull BlockPos pos,
                                               @Nonnull ItemStack stack,
                                               boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stack.copy();
        for (Direction dir : Direction.values()) {
            if (remaining.isEmpty()) {
                break;
            }
            IItemHandler handler = getItemHandler(level, pos.relative(dir), dir.getOpposite());
            if (handler != null) {
                remaining = insertIntoHandler(handler, remaining, simulate);
            }
        }
        return remaining;
    }

    /**
     * Insert an item stack into an item handler.
     *
     * @param handler  the target item handler
     * @param stack    the stack to insert
     * @param simulate if true, don't actually insert
     * @return the remainder that could not be inserted
     */
    @Nonnull
    public static ItemStack insertIntoHandler(@Nonnull IItemHandler handler,
                                              @Nonnull ItemStack stack,
                                              boolean simulate) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (remaining.isEmpty()) {
                break;
            }
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    /**
     * Extract up to the specified count of items matching the filter from an item handler.
     *
     * @param handler  the source handler
     * @param maxCount maximum items to extract
     * @param simulate if true, don't actually extract
     * @return the extracted stack, or EMPTY if none found
     */
    @Nonnull
    public static ItemStack extractFromHandler(@Nonnull IItemHandler handler,
                                               int maxCount,
                                               boolean simulate) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack inSlot = handler.getStackInSlot(slot);
            if (!inSlot.isEmpty()) {
                return handler.extractItem(slot, Math.min(maxCount, inSlot.getCount()), simulate);
            }
        }
        return ItemStack.EMPTY;
    }
}
