/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.storage;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Interface for block entities that participate in the AS storage network
 * (lightwells, chalices). Each tile can resolve its master core and query
 * the network it belongs to.
 *
 * <p>1.16 → 1.20: World → Level, RegistryKey → ResourceKey.</p>
 *
 * @param <T> the concrete type of the storage core tile entity
 */
public interface IStorageNetworkTile<T extends IStorageNetworkTile<T>> extends ILocatable {

    /**
     * Returns the network core this tile is associated with.
     * May chain through other cores that ultimately resolve via this instance.
     */
    T getAssociatedCore();

    /** The level the network lives in. Typically the tile entity's own level. */
    Level getNetworkWorld();

    /** Called when the network mapping changes (e.g., cores added or removed). */
    void receiveMappingChange(StorageNetworkHandler.MappingChange newMapping);

    /** Looks up the {@link StorageNetwork} this tile belongs to, or null if not in one. */
    @Nullable
    default StorageNetwork getNetwork() {
        StorageNetworkHandler.NetworkHelper handler =
                StorageNetworkHandler.getHandler(getNetworkWorld());
        if (handler == null) return null;
        return handler.getNetwork(getAssociatedCore().getLocationPos());
    }

    /**
     * Walks the association chain until a fixed point is reached,
     * returning the root (master) core tile for this network.
     */
    default T resolveMasterCore() {
        T assoc = getAssociatedCore();
        T next;
        while (assoc != (next = assoc.getAssociatedCore())) {
            assoc = next;
        }
        return assoc;
    }
}
