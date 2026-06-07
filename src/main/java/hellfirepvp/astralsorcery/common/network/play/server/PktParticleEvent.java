/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.Random;
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
    /** Liquid starlight field fountain (Resonator FLUID_FIELDS visualization) */
    public static final int LIQUID_FOUNTAIN = 10;

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
                DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg))
        );
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktParticleEvent msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Vec3 center = Vec3.atCenterOf(msg.pos);
        Random rand = new Random();

        switch (msg.eventType) {
            case ALTAR_CRAFT -> {
                burst(level, center, ParticleTypes.END_ROD, 30, rand, 0.8);
                burst(level, center, ParticleTypes.ENCHANT, 20, rand, 0.5);
            }
            case CRYSTAL_FORM -> burst(level, center, ParticleTypes.ELECTRIC_SPARK, 20, rand, 0.5);
            case ATTUNEMENT_BEAM -> {
                burst(level, center, ParticleTypes.END_ROD, 20, rand, 0.6);
                burst(level, center, ParticleTypes.WITCH, 12, rand, 0.4);
            }
            case RITUAL_ACTIVATE -> {
                level.addParticle(ParticleTypes.FLASH, center.x, center.y, center.z, 0, 0, 0);
                burst(level, center, ParticleTypes.END_ROD, 16, rand, 0.6);
            }
            case WELL_COLLECT -> burst(level, center, ParticleTypes.ELECTRIC_SPARK, 15, rand, 0.4);
            case INFUSER_CRAFT -> {
                burst(level, center, ParticleTypes.END_ROD, 24, rand, 0.6);
                burst(level, center, ParticleTypes.ELECTRIC_SPARK, 12, rand, 0.4);
            }
            case GATEWAY_ACTIVATE -> {
                burst(level, center, ParticleTypes.PORTAL, 40, rand, 0.6);
                level.addParticle(ParticleTypes.FLASH, center.x, center.y, center.z, 0, 0, 0);
            }
            case STARLIGHT_BURST -> burst(level, center, ParticleTypes.ELECTRIC_SPARK, 24, rand, 0.6);
            case CONSTELLATION_DISCOVER -> {
                level.addParticle(ParticleTypes.FLASH, center.x, center.y, center.z, 0, 0, 0);
                burst(level, center, ParticleTypes.END_ROD, 16, rand, 0.8);
            }
            case FOUNTAIN_PRIME -> burst(level, center, ParticleTypes.DOLPHIN, 20, rand, 0.6);
            case LIQUID_FOUNTAIN -> {
                for (int i = 0; i < 12; i++) {
                    double angle = rand.nextDouble() * Math.PI * 2;
                    double spread = rand.nextDouble() * 0.3;
                    double vx = Math.cos(angle) * spread * 0.08;
                    double vz = Math.sin(angle) * spread * 0.08;
                    double vy = 0.10 + rand.nextDouble() * 0.12;
                    double ox = (rand.nextDouble() - 0.5) * 0.6;
                    double oz = (rand.nextDouble() - 0.5) * 0.6;
                    level.addParticle(ParticleTypes.DOLPHIN,
                            center.x + ox, center.y, center.z + oz, vx, vy, vz);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void burst(@Nonnull ClientLevel level, @Nonnull Vec3 center,
                               @Nonnull ParticleOptions type, int count,
                               @Nonnull Random rand, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double pitch = (rand.nextDouble() - 0.5) * Math.PI;
            double vx = Math.cos(pitch) * Math.cos(angle) * 0.12;
            double vy = Math.sin(pitch) * 0.12;
            double vz = Math.cos(pitch) * Math.sin(angle) * 0.12;
            double ox = (rand.nextDouble() - 0.5) * radius * 0.4;
            double oz = (rand.nextDouble() - 0.5) * radius * 0.4;
            level.addParticle(type, center.x + ox, center.y, center.z + oz, vx, vy, vz);
        }
    }
}
