/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.data.config.ConfigManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server → Client: syncs server-side configuration values to the client.
 * Sent on player login to ensure the client uses the same config values
 * as the server (especially for game balance settings).
 *
 * <p>Synced values include altar recipe costs, perk multipliers,
 * starlight transmission rates, and feature toggles. The client
 * overrides its local config with these server values for the
 * duration of the connection.</p>
 *
 * <p>1.16 → 1.20: CompoundTag read/write via FriendlyByteBuf stable.</p>
 */
public class PktSyncConfig {

    @Nonnull
    private final CompoundTag configData;

    public PktSyncConfig(@Nonnull CompoundTag configData) {
        this.configData = configData;
    }

    public static void encode(@Nonnull PktSyncConfig pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeNbt(pkt.configData);
    }

    @Nonnull
    public static PktSyncConfig decode(@Nonnull FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new PktSyncConfig(tag != null ? tag : new CompoundTag());
    }

    public static void handle(@Nonnull PktSyncConfig pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: apply server config overrides
            ConfigManager.applySyncedConfig(pkt.configData);
        });
        ctx.get().setPacketHandled(true);
    }
}
