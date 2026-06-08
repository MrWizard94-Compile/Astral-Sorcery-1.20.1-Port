/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * World-saved data managing the Celestial Gateway network.
 * Stores all registered gateway positions across all dimensions.
 *
 * <p>Gateways form a teleportation network — any gateway can teleport
 * the player to any other gateway in the network. Cross-dimension
 * teleportation is supported.</p>
 *
 * <p>Gateways register on first tick and unregister when broken.
 * The network persists across server restarts via SavedData.</p>
 *
 * <p>1.16 → 1.20: WorldSavedData → SavedData, overworld reference via server.</p>
 */
public class GatewayHandler extends SavedData {

    private static final String DATA_ID = AstralSorcery.MODID + "_gateway_network";

    private final Map<UUID, GatewayEntry> gateways = new LinkedHashMap<>();

    public GatewayHandler() {}

    // ========================================================================
    // Registry operations
    // ========================================================================

    /**
     * Registers a new gateway in the network.
     *
     * @param id          unique ID for this gateway
     * @param dimension   the dimension key where the gateway exists
     * @param pos         the block position
     * @param displayName optional display name
     * @return the created gateway entry
     */
    @Nonnull
    public GatewayEntry registerGateway(@Nonnull UUID id,
                                         @Nonnull ResourceKey<Level> dimension,
                                         @Nonnull BlockPos pos,
                                         @Nullable String displayName) {
        GatewayEntry entry = new GatewayEntry(id, dimension, pos, displayName);
        gateways.put(id, entry);
        setDirty();
        AstralSorcery.log.debug("Registered gateway {} at {} in {}",
                id, pos, dimension.location());
        return entry;
    }

    /**
     * Removes a gateway from the network.
     *
     * @param id the gateway ID to remove
     * @return true if removed, false if not found
     */
    public boolean unregisterGateway(@Nonnull UUID id) {
        GatewayEntry removed = gateways.remove(id);
        if (removed != null) {
            setDirty();
            AstralSorcery.log.debug("Unregistered gateway {} at {}",
                    id, removed.pos);
            return true;
        }
        return false;
    }

    /**
     * Removes a gateway at a specific position.
     *
     * @param dimension the dimension
     * @param pos       the block position
     * @return true if removed
     */
    public boolean unregisterGatewayAt(@Nonnull ResourceKey<Level> dimension,
                                        @Nonnull BlockPos pos) {
        UUID toRemove = null;
        for (Map.Entry<UUID, GatewayEntry> entry : gateways.entrySet()) {
            GatewayEntry gw = entry.getValue();
            if (gw.dimension.equals(dimension) && gw.pos.equals(pos)) {
                toRemove = entry.getKey();
                break;
            }
        }
        if (toRemove != null) {
            return unregisterGateway(toRemove);
        }
        return false;
    }

    // ========================================================================
    // Queries
    // ========================================================================

    /**
     * Gets all registered gateways in the network.
     */
    @Nonnull
    public Collection<GatewayEntry> getAllGateways() {
        return Collections.unmodifiableCollection(gateways.values());
    }

    /**
     * Gets all gateways in a specific dimension.
     */
    @Nonnull
    public List<GatewayEntry> getGatewaysInDimension(@Nonnull ResourceKey<Level> dimension) {
        List<GatewayEntry> result = new ArrayList<>();
        for (GatewayEntry entry : gateways.values()) {
            if (entry.dimension.equals(dimension)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Finds a gateway by position.
     */
    @Nullable
    public GatewayEntry getGatewayAt(@Nonnull ResourceKey<Level> dimension,
                                      @Nonnull BlockPos pos) {
        for (GatewayEntry entry : gateways.values()) {
            if (entry.dimension.equals(dimension) && entry.pos.equals(pos)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gets the number of registered gateways.
     */
    public int size() {
        return gateways.size();
    }

    // ========================================================================
    // Serialization
    // ========================================================================

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag list = new ListTag();
        for (GatewayEntry entry : gateways.values()) {
            list.add(entry.writeToNBT());
        }
        compound.put("gateways", list);
        return compound;
    }

    @Nonnull
    public static GatewayHandler load(@Nonnull CompoundTag compound) {
        GatewayHandler handler = new GatewayHandler();
        if (compound.contains("gateways")) {
            ListTag list = compound.getList("gateways", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                GatewayEntry entry = GatewayEntry.readFromNBT(list.getCompound(i));
                if (entry != null) {
                    handler.gateways.put(entry.id, entry);
                }
            }
        }
        return handler;
    }

    /**
     * Gets or creates the GatewayHandler for the given server.
     * Stored in the overworld's saved data.
     */
    @Nonnull
    public static GatewayHandler get(@Nonnull MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(GatewayHandler::load, GatewayHandler::new, DATA_ID);
    }

    // ========================================================================
    // Gateway entry record
    // ========================================================================

    /**
     * Represents a single gateway in the network.
     */
    public static class GatewayEntry {
        @Nonnull
        public final UUID id;
        @Nonnull
        public final ResourceKey<Level> dimension;
        @Nonnull
        public final BlockPos pos;
        @Nullable
        public final String displayName;

        public GatewayEntry(@Nonnull UUID id,
                             @Nonnull ResourceKey<Level> dimension,
                             @Nonnull BlockPos pos,
                             @Nullable String displayName) {
            this.id = id;
            this.dimension = dimension;
            this.pos = pos;
            this.displayName = displayName;
        }

        @Nonnull
        public CompoundTag writeToNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.putString("dimension", dimension.location().toString());
            tag.putInt("posX", pos.getX());
            tag.putInt("posY", pos.getY());
            tag.putInt("posZ", pos.getZ());
            if (displayName != null) {
                tag.putString("displayName", displayName);
            }
            return tag;
        }

        @Nullable
        public static GatewayEntry readFromNBT(@Nonnull CompoundTag tag) {
            if (!tag.contains("id") || !tag.contains("dimension")) {
                return null;
            }
            UUID id = tag.getUUID("id");
            ResourceLocation dimRL = ResourceLocation.tryParse(tag.getString("dimension"));
            if (dimRL == null) return null;
            ResourceKey<Level> dimension = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION, dimRL);
            BlockPos pos = new BlockPos(
                    tag.getInt("posX"), tag.getInt("posY"), tag.getInt("posZ"));
            String name = tag.contains("displayName") ? tag.getString("displayName") : null;
            return new GatewayEntry(id, dimension, pos, name);
        }

        /**
         * Gets the display label for this gateway (name or coordinates).
         */
        @Nonnull
        public String getLabel() {
            String dn = displayName;
            if (dn != null && !dn.isEmpty()) {
                return dn;
            }
            return String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
