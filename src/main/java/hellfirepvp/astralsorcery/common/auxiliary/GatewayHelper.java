package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages gateway node registration for cross-dimension teleportation.
 * Gateways are registered at runtime when their block entities load
 * and unregistered when they unload.
 *
 * <p>1.16 -> 1.20 changes:
 * RegistryKey&lt;World&gt; -> ResourceKey&lt;Level&gt;,
 * World -> Level</p>
 */
public class GatewayHelper {

    private static final Map<ResourceKey<Level>, List<GatewayNode>> gatewaysByDimension = new HashMap<>();

    private GatewayHelper() {}

    /**
     * Register a gateway node.
     *
     * @param dimension the dimension the gateway is in
     * @param node      the gateway node data
     */
    public static void registerGateway(@Nonnull ResourceKey<Level> dimension, @Nonnull GatewayNode node) {
        gatewaysByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(node);
    }

    /**
     * Unregister a gateway node at the given position.
     *
     * @param dimension the dimension
     * @param pos       the position to unregister
     * @return true if a gateway was removed
     */
    public static boolean unregisterGateway(@Nonnull ResourceKey<Level> dimension, @Nonnull BlockPos pos) {
        List<GatewayNode> nodes = gatewaysByDimension.get(dimension);
        if (nodes != null) {
            return nodes.removeIf(node -> node.pos().equals(pos));
        }
        return false;
    }

    /**
     * Get all registered gateways in a dimension.
     *
     * @param dimension the dimension key
     * @return unmodifiable list of gateway nodes
     */
    @Nonnull
    public static List<GatewayNode> getGatewaysForDimension(@Nonnull ResourceKey<Level> dimension) {
        List<GatewayNode> nodes = gatewaysByDimension.get(dimension);
        return nodes != null ? Collections.unmodifiableList(nodes) : Collections.emptyList();
    }

    /**
     * Get all registered gateways across all dimensions.
     *
     * @return unmodifiable list of all gateway nodes
     */
    @Nonnull
    public static List<GatewayNode> getAllGateways() {
        List<GatewayNode> all = new ArrayList<>();
        for (List<GatewayNode> nodes : gatewaysByDimension.values()) {
            all.addAll(nodes);
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Find the nearest gateway to the given position (same dimension only).
     *
     * @param dimension the dimension
     * @param pos       the reference position
     * @return the nearest node, or null if none registered
     */
    @Nullable
    public static GatewayNode findNearest(@Nonnull ResourceKey<Level> dimension, @Nonnull BlockPos pos) {
        List<GatewayNode> nodes = gatewaysByDimension.get(dimension);
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        GatewayNode nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (GatewayNode node : nodes) {
            double dist = node.pos().distSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = node;
            }
        }
        return nearest;
    }

    /**
     * Clear all registered gateways (server stop).
     */
    public static void clearAll() {
        gatewaysByDimension.clear();
    }

    /**
     * A gateway node entry.
     *
     * @param pos       the block position of the gateway
     * @param dimension the dimension the gateway exists in
     * @param displayName optional display name for the gateway
     */
    public record GatewayNode(@Nonnull BlockPos pos,
                              @Nonnull ResourceKey<Level> dimension,
                              @Nullable String displayName) {

        @Nonnull
        public Vector3 getCenter() {
            return new Vector3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }
    }
}
