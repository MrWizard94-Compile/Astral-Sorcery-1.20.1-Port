package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Container for the Discovery-tier altar (3x3 grid, 9 slots).
 *
 * <p>1.16 -> 1.20 changes:
 * ContainerType -> MenuType, PacketBuffer -> FriendlyByteBuf</p>
 */
public class ContainerAltarDiscovery extends ContainerAltarBase {

    public ContainerAltarDiscovery(int containerId, @Nonnull Inventory playerInv,
                                   @Nonnull BlockEntityAltar altar) {
        super(MenuTypesAS.ALTAR_DISCOVERY.get(), containerId, playerInv, altar);
    }

    /**
     * Client-side factory — reads block entity position from network buffer.
     */
    public static ContainerAltarDiscovery fromNetwork(int containerId,
                                                       @Nonnull Inventory playerInv,
                                                       @Nonnull FriendlyByteBuf buf) {
        BlockEntity be = playerInv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof BlockEntityAltar altar) {
            return new ContainerAltarDiscovery(containerId, playerInv, altar);
        }
        throw new IllegalStateException("Expected BlockEntityAltar at position from buffer");
    }
}
