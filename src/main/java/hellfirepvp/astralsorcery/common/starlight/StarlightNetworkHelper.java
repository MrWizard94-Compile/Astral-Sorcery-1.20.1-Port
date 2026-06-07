/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Static helper for block entities to register/unregister themselves
 * with their dimension's {@link WorldNetworkHandler}.
 *
 * <p>Block entities call these methods in their {@code onFirstTick()},
 * {@code setRemoved()}, and {@code onChunkUnloaded()} overrides to
 * participate in the starlight network.</p>
 *
 * <p>All methods are server-side only; client-side calls are silently ignored.</p>
 */
public final class StarlightNetworkHelper {

    private StarlightNetworkHelper() {}

    /**
     * Registers a starlight source with the network.
     * Call from the source block entity's first tick or when its state changes.
     *
     * @param level    the block entity's level
     * @param pos      the block entity's position
     * @param source   the source interface implementation
     */
    public static void registerSource(@Nullable Level level,
                                      @Nonnull BlockPos pos,
                                      @Nonnull IStarlightSource source) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean autoLink = source instanceof IIndependentStarlightSource independentSource
                && independentSource.providesAutoLink();

        WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(serverLevel);
        handler.registerSource(pos, source.getAttunedConstellationKey(), autoLink);

        if (source instanceof IIndependentStarlightSource independentSource) {
            handler.storeIndependentSourceData(pos, independentSource.serializeSourceNBT());
        }
    }

    /**
     * Registers a starlight receiver with the network.
     *
     * @param level    the block entity's level
     * @param pos      the block entity's position
     * @param receiver the receiver interface implementation
     */
    public static void registerReceiver(@Nullable Level level,
                                        @Nonnull BlockPos pos,
                                        @Nonnull IStarlightReceiver receiver) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(serverLevel);
        handler.registerReceiver(pos, receiver.getMaxStarlightInput());
    }

    /**
     * Registers a starlight transmission node with the network.
     *
     * @param level        the block entity's level
     * @param pos          the block entity's position
     * @param transmission the transmission interface implementation
     */
    public static void registerTransmission(@Nullable Level level,
                                            @Nonnull BlockPos pos,
                                            @Nonnull IStarlightTransmission transmission) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(serverLevel);
        handler.registerTransmission(pos, transmission.getTransmissionEfficiency());
    }

    /**
     * Removes a node (any type) from the network.
     * Call from the block entity's {@code setRemoved()} or when the block breaks.
     *
     * @param level the block entity's level
     * @param pos   the block entity's position
     */
    public static void removeNode(@Nullable Level level, @Nonnull BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(serverLevel);
        handler.removeNode(pos);
    }

    /**
     * Adds a link between two nodes in the starlight network.
     * Called by the linking tool (wand) when the player completes a link action.
     *
     * @param level the level where both nodes exist
     * @param from  the source/transmission node position
     * @param to    the transmission/receiver node position
     * @return true if the link was successfully added
     */
    public static boolean addLink(@Nullable Level level,
                                  @Nonnull BlockPos from,
                                  @Nonnull BlockPos to) {
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(serverLevel);
        return handler.addLink(from, to);
    }

    /**
     * Removes a link between two nodes in the starlight network.
     *
     * @param level the level where both nodes exist
     * @param from  the source/transmission node position
     * @param to    the transmission/receiver node position
     * @return true if the link existed and was removed
     */
    public static boolean removeLink(@Nullable Level level,
                                     @Nonnull BlockPos from,
                                     @Nonnull BlockPos to) {
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(serverLevel);
        return handler.removeLink(from, to);
    }

    /**
     * Gets the network handler for a level, or null if client-side.
     *
     * @param level the level to query
     * @return the handler, or null on client side
     */
    @Nullable
    public static WorldNetworkHandler getHandler(@Nullable Level level) {
        if (level == null || level.isClientSide()) {
            return null;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return WorldNetworkHandler.getOrCreate(serverLevel);
    }
}
