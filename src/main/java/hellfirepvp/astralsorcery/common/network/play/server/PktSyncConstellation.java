/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server -> Client packet that notifies the client about a constellation
 * discovery or removal. Triggers client-side VFX and updates local
 * progress data.
 *
 * <p>1.16 -> 1.20 changes:
 * PacketBuffer -> FriendlyByteBuf,
 * ResourceLocation read/write unchanged</p>
 */
public class PktSyncConstellation {

    @Nonnull
    private final ResourceLocation constellation;
    private final boolean discovered;

    /**
     * @param constellation the constellation registry name
     * @param discovered    true if newly discovered, false if removed
     */
    public PktSyncConstellation(@Nonnull ResourceLocation constellation, boolean discovered) {
        this.constellation = constellation;
        this.discovered = discovered;
    }

    @Nonnull
    public ResourceLocation getConstellation() {
        return constellation;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public static void encode(@Nonnull PktSyncConstellation msg, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.constellation);
        buf.writeBoolean(msg.discovered);
    }

    @Nonnull
    public static PktSyncConstellation decode(@Nonnull FriendlyByteBuf buf) {
        ResourceLocation constellation = buf.readResourceLocation();
        boolean discovered = buf.readBoolean();
        return new PktSyncConstellation(constellation, discovered);
    }

    public static void handle(@Nonnull PktSyncConstellation msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg))
        );
        ctx.setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktSyncConstellation msg) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        player.getCapability(PlayerCapabilityProvider.CAPABILITY).ifPresent(progress -> {
            if (msg.discovered) {
                progress.discoverConstellation(msg.constellation);
                AstralSorcery.log.debug("Constellation discovered: {}", msg.constellation);
                spawnDiscoveryBurst(mc, player);
            } else {
                progress.undiscoverConstellation(msg.constellation);
                AstralSorcery.log.debug("Constellation removed: {}", msg.constellation);
            }
        });
    }

    private static void spawnDiscoveryBurst(@Nonnull Minecraft mc, @Nonnull Player player) {
        ClientLevel level = mc.level;
        if (level == null) return;
        Vec3 pos = player.position().add(0, player.getEyeHeight(), 0);
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < 40; i++) {
            double ox = (rng.nextDouble() - 0.5) * 2.0;
            double oy = (rng.nextDouble() - 0.5) * 2.0;
            double oz = (rng.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.END_ROD,
                    pos.x + ox * 0.5, pos.y + oy * 0.5, pos.z + oz * 0.5,
                    ox * 0.1, oy * 0.1 + 0.05, oz * 0.1);
        }
        for (int i = 0; i < 20; i++) {
            double ox = (rng.nextDouble() - 0.5) * 1.5;
            double oy = rng.nextDouble() * 1.5;
            double oz = (rng.nextDouble() - 0.5) * 1.5;
            level.addParticle(ParticleTypes.FLASH,
                    pos.x + ox, pos.y + oy, pos.z + oz,
                    0, 0, 0);
        }
    }
}
