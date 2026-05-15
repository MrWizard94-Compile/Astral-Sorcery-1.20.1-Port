/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server -> Client packet that triggers a particle effect at a specific
 * location. Used as a generic particle dispatch mechanism for various
 * Astral Sorcery visual effects (altar crafting, crystal formation,
 * attunement, ritual effects, etc.).
 *
 * <p>The {@code eventType} field selects the particle routine to run,
 * and {@code data1}/{@code data2}/{@code data3} provide effect-specific
 * parameters (e.g., color, intensity, radius).</p>
 *
 * <p>1.16 -> 1.20 changes:
 * PacketBuffer -> FriendlyByteBuf,
 * particle system dispatch unchanged</p>
 */
public class PktParticleEvent {

    private final int eventType;
    @Nonnull
    private final BlockPos pos;
    private final double data1;
    private final double data2;
    private final double data3;

    /**
     * @param eventType particle event type ID (see constants in this class)
     * @param pos       the block position to spawn particles at
     * @param data1     first effect-specific parameter
     * @param data2     second effect-specific parameter
     * @param data3     third effect-specific parameter
     */
    public PktParticleEvent(int eventType, @Nonnull BlockPos pos,
                            double data1, double data2, double data3) {
        this.eventType = eventType;
        this.pos = pos;
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
    }

    /**
     * Convenience constructor with no extra data parameters.
     */
    public PktParticleEvent(int eventType, @Nonnull BlockPos pos) {
        this(eventType, pos, 0, 0, 0);
    }

    public int getEventType() {
        return eventType;
    }

    @Nonnull
    public BlockPos getPos() {
        return pos;
    }

    public double getData1() {
        return data1;
    }

    public double getData2() {
        return data2;
    }

    public double getData3() {
        return data3;
    }

    // ---- Particle event type constants ----

    /** Altar crafting sparkle burst */
    public static final int ALTAR_CRAFT = 0;
    /** Crystal formation shimmer */
    public static final int CRYSTAL_FORM = 1;
    /** Attunement beam activation */
    public static final int ATTUNEMENT_BEAM = 2;
    /** Ritual pedestal activation pulse */
    public static final int RITUAL_ACTIVATE = 3;
    /** Starlight well collection sparkle */
    public static final int WELL_COLLECT = 4;
    /** Infuser crafting effect */
    public static final int INFUSER_CRAFT = 5;
    /** Celestial gateway activation */
    public static final int GATEWAY_ACTIVATE = 6;
    /** Generic starlight burst */
    public static final int STARLIGHT_BURST = 7;
    /** Constellation paper discovery flash */
    public static final int CONSTELLATION_DISCOVER = 8;
    /** Fountain prime activation */
    public static final int FOUNTAIN_PRIME = 9;

    public static void encode(@Nonnull PktParticleEvent msg, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(msg.eventType);
        buf.writeBlockPos(msg.pos);
        buf.writeDouble(msg.data1);
        buf.writeDouble(msg.data2);
        buf.writeDouble(msg.data3);
    }

    @Nonnull
    public static PktParticleEvent decode(@Nonnull FriendlyByteBuf buf) {
        int eventType = buf.readVarInt();
        BlockPos pos = buf.readBlockPos();
        double data1 = buf.readDouble();
        double data2 = buf.readDouble();
        double data3 = buf.readDouble();
        return new PktParticleEvent(eventType, pos, data1, data2, data3);
    }

    public static void handle(@Nonnull PktParticleEvent msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg))
        );
        ctx.setPacketHandled(true);
    }

    private static void handleClient(@Nonnull PktParticleEvent msg) {
        // TODO: Dispatch to particle effect registry based on eventType.
        // Each event type maps to a client-side particle routine that spawns
        // the appropriate particles at msg.pos with msg.data1/data2/data3.
        AstralSorcery.log.trace("Particle event type={} at {} data=[{},{},{}]",
                msg.eventType, msg.pos, msg.data1, msg.data2, msg.data3);
    }
}
