package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Container for the Attunement-tier altar (5x5 cross, 13 slots).
 *
 * <p>1.16 -> 1.20 changes: Same as ContainerAltarDiscovery.</p>
 */
public class ContainerAltarAttunement extends ContainerAltarBase {

    public ContainerAltarAttunement(int containerId, @Nonnull Inventory playerInv,
                                    @Nonnull BlockEntityAltar altar) {
        super(MenuTypesAS.ALTAR_ATTUNEMENT.get(), containerId, playerInv, altar);
    }

    public static ContainerAltarAttunement fromNetwork(int containerId,
                                                        @Nonnull Inventory playerInv,
                                                        @Nonnull FriendlyByteBuf buf) {
        BlockEntity be = playerInv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof BlockEntityAltar altar) {
            return new ContainerAltarAttunement(containerId, playerInv, altar);
        }
        throw new IllegalStateException("Expected BlockEntityAltar at position from buffer");
    }
}
