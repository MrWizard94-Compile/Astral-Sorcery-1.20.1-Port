package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Container for the Radiance-tier altar (5x5 + outer relays, 25 slots).
 *
 * <p>1.16 -> 1.20 changes: Same as ContainerAltarDiscovery.</p>
 */
public class ContainerAltarRadiance extends ContainerAltarBase {

    public ContainerAltarRadiance(int containerId, @Nonnull Inventory playerInv,
                                   @Nonnull BlockEntityAltar altar) {
        super(MenuTypesAS.ALTAR_RADIANCE.get(), containerId, playerInv, altar);
    }

    public static ContainerAltarRadiance fromNetwork(int containerId,
                                                      @Nonnull Inventory playerInv,
                                                      @Nonnull FriendlyByteBuf buf) {
        BlockEntity be = playerInv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof BlockEntityAltar altar) {
            return new ContainerAltarRadiance(containerId, playerInv, altar);
        }
        throw new IllegalStateException("Expected BlockEntityAltar at position from buffer");
    }
}
