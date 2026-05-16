/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server → Client: Syncs the player's current starlight charge level.
 * Used to update the HUD starlight gauge. Sent periodically when the
 * player's starlight level changes (standing under open sky at night,
 * near collector crystals, etc.).
 */
public class PktSyncStarlightCharge {

    /** Current charge [0, 1]. */
    private final float chargeLevel;
    /** Maximum charge capacity (can grow with perks). */
    private final float maxCharge;
    /** Whether the player is currently gaining starlight. */
    private final boolean isCharging;

    public PktSyncStarlightCharge(float chargeLevel, float maxCharge, boolean isCharging) {
        this.chargeLevel = chargeLevel;
        this.maxCharge = maxCharge;
        this.isCharging = isCharging;
    }

    public static void encode(@Nonnull PktSyncStarlightCharge pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeFloat(pkt.chargeLevel);
        buf.writeFloat(pkt.maxCharge);
        buf.writeBoolean(pkt.isCharging);
    }

    @Nonnull
    public static PktSyncStarlightCharge decode(@Nonnull FriendlyByteBuf buf) {
        return new PktSyncStarlightCharge(buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }

    public static void handle(@Nonnull PktSyncStarlightCharge pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(pkt));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktSyncStarlightCharge pkt) {
        // TODO: Update client-side starlight cache for HUD rendering
        // ClientStarlightCache.setCharge(pkt.chargeLevel, pkt.maxCharge);
        // ClientStarlightCache.setCharging(pkt.isCharging);
    }

    public float getChargeLevel() { return chargeLevel; }
    public float getMaxCharge() { return maxCharge; }
    public boolean isCharging() { return isCharging; }
}
