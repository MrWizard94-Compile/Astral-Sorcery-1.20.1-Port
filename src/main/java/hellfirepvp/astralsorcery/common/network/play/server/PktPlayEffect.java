/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.function.Supplier;

/**
 * Server â†’ Client: triggers a specific visual/sound effect at a location.
 * Used for one-shot effects like ritual activation, altar crafting completion,
 * attunement bursts, and transmutation completion.
 *
 * <p>Each effect type is identified by an enum ordinal. The client handler
 * interprets the type and spawns the appropriate particles/sounds.</p>
 *
 * <p>1.16 â†’ 1.20: Packet format unchanged. Client effect dispatch stable.</p>
 */
public class PktPlayEffect {

    @Nonnull
    private final EffectType effectType;
    @Nonnull
    private final BlockPos position;
    private final int auxData;

    public PktPlayEffect(@Nonnull EffectType effectType, @Nonnull BlockPos position) {
        this(effectType, position, 0);
    }

    public PktPlayEffect(@Nonnull EffectType effectType, @Nonnull BlockPos position, int auxData) {
        this.effectType = effectType;
        this.position = position;
        this.auxData = auxData;
    }

    @Nonnull
    public EffectType getEffectType() {
        return effectType;
    }

    @Nonnull
    public BlockPos getPosition() {
        return position;
    }

    public int getAuxData() {
        return auxData;
    }

    public static void encode(@Nonnull PktPlayEffect pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.effectType.ordinal());
        buf.writeBlockPos(pkt.position);
        buf.writeVarInt(pkt.auxData);
    }

    @Nonnull
    public static PktPlayEffect decode(@Nonnull FriendlyByteBuf buf) {
        int typeOrd = buf.readVarInt();
        BlockPos pos = buf.readBlockPos();
        int aux = buf.readVarInt();
        EffectType type = EffectType.values()[Math.min(typeOrd, EffectType.values().length - 1)];
        return new PktPlayEffect(type, pos, aux);
    }

    public static void handle(@Nonnull PktPlayEffect pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> handleClient(pkt));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktPlayEffect pkt) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Vec3 center = Vec3.atCenterOf(pkt.position);

        switch (pkt.effectType) {
            case ALTAR_CRAFT_COMPLETE -> {
                EffectHelper.flareStarlight(center);
                EffectHelper.burstStarlight(center);
                EffectHelper.orbitalStarlight(center, 1.2f);
            }
            case ATTUNEMENT_COMPLETE -> {
                EffectHelper.flareStarlight(center);
                EffectHelper.orbitalStarlight(center, 1.5f);
                EffectHelper.orbitalStarlight(center, 2.5f);
            }
            case RITUAL_ACTIVATE -> {
                EffectHelper.flareStarlight(center);
                EffectHelper.vortexStarlight(center);
                EffectHelper.burstStarlight(center);
            }
            case RITUAL_DEACTIVATE ->
                EffectHelper.dustCloud(center, new Color(130, 170, 240), 16, 1.0f);
            case TRANSMUTATION_COMPLETE -> {
                EffectHelper.crystalGrowthCluster(center, new Color(160, 200, 255), 12, 0.5f);
                EffectHelper.sparkleCloud(center, 0.4f, EffectHelper.randomStarlightColor(), 10, 0.12f, 30);
            }
            case SHOOTING_STAR_IMPACT -> {
                EffectHelper.flareStarlight(center);
                EffectHelper.burstStarlight(center);
                EffectHelper.burstStarlight(center);
            }
            case GATEWAY_TELEPORT -> {
                EffectHelper.vortexStarlight(center);
                EffectHelper.orbitalStarlight(center, 1.0f);
            }
            case CELESTIAL_CRYSTAL_DESCEND -> {
                EffectHelper.crystalGrowthCluster(center, new Color(180, 220, 255), 8, 0.3f);
                EffectHelper.sparkleCloud(center, 0.5f, EffectHelper.randomStarlightColor(), 8, 0.1f, 40);
            }
            case WELL_FILL_BURST ->
                EffectHelper.sparkleCloud(center, 0.6f, new Color(100, 160, 240), 12, 0.1f, 25);
            case INFUSION_COMPLETE -> {
                EffectHelper.flareStarlight(center);
                EffectHelper.burstStarlight(center);
                EffectHelper.orbitalStarlight(center, 0.8f);
            }
            case LIQUID_INTERACTION ->
                EffectHelper.sparkleCloud(center, 0.3f, new Color(80, 140, 220), 6, 0.08f, 20);
            case PERK_ACTIVATE -> {
                EffectHelper.flareStarlight(center);
                EffectHelper.orbitalStarlight(center, 1.0f);
            }
            case CRYSTAL_FORM ->
                EffectHelper.crystalGrowthCluster(center, new Color(200, 230, 255), 10, 0.4f);
            default -> {}
        }
    }

    /**
     * Types of one-shot visual effects that can be triggered from the server.
     */
    public enum EffectType {
        /** Altar crafting complete â€” burst of starlight particles upward. */
        ALTAR_CRAFT_COMPLETE,

        /** Attunement complete â€” expanding ring of constellation particles. */
        ATTUNEMENT_COMPLETE,

        /** Ritual activation â€” swirl of constellation-colored particles. */
        RITUAL_ACTIVATE,

        /** Ritual deactivation â€” particles fade outward. */
        RITUAL_DEACTIVATE,

        /** Block transmutation â€” sparkle at block position. */
        TRANSMUTATION_COMPLETE,

        /** Shooting star impact â€” explosion of starlight particles. */
        SHOOTING_STAR_IMPACT,

        /** Gateway teleport â€” swirl at departure location. */
        GATEWAY_TELEPORT,

        /** Celestial crystal descent â€” trail of constellation sparks. */
        CELESTIAL_CRYSTAL_DESCEND,

        /** Well filled â€” fluid surface shimmer. */
        WELL_FILL_BURST,

        /** Infusion complete â€” liquid drain with item glow. */
        INFUSION_COMPLETE,

        /** Liquid interaction â€” sparkle between fluid boundaries. */
        LIQUID_INTERACTION,

        /** Perk activation â€” aura flash around player position. */
        PERK_ACTIVATE,

        /** Crystal formation â€” slow crystallization particles. */
        CRYSTAL_FORM
    }
}
