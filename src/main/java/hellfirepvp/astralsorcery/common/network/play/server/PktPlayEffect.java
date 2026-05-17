/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server → Client: triggers a specific visual/sound effect at a location.
 * Used for one-shot effects like ritual activation, altar crafting completion,
 * attunement bursts, and transmutation completion.
 *
 * <p>Each effect type is identified by an enum ordinal. The client handler
 * interprets the type and spawns the appropriate particles/sounds.</p>
 *
 * <p>1.16 → 1.20: Packet format unchanged. Client effect dispatch stable.</p>
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
        ctx.get().enqueueWork(() -> {
            // Client-side: dispatch to the effect renderer
            // Handled by ClientEffectHandler (Phase 12)
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Types of one-shot visual effects that can be triggered from the server.
     */
    public enum EffectType {
        /** Altar crafting complete — burst of starlight particles upward. */
        ALTAR_CRAFT_COMPLETE,

        /** Attunement complete — expanding ring of constellation particles. */
        ATTUNEMENT_COMPLETE,

        /** Ritual activation — swirl of constellation-colored particles. */
        RITUAL_ACTIVATE,

        /** Ritual deactivation — particles fade outward. */
        RITUAL_DEACTIVATE,

        /** Block transmutation — sparkle at block position. */
        TRANSMUTATION_COMPLETE,

        /** Shooting star impact — explosion of starlight particles. */
        SHOOTING_STAR_IMPACT,

        /** Gateway teleport — swirl at departure location. */
        GATEWAY_TELEPORT,

        /** Celestial crystal descent — trail of constellation sparks. */
        CELESTIAL_CRYSTAL_DESCEND,

        /** Well filled — fluid surface shimmer. */
        WELL_FILL_BURST,

        /** Infusion complete — liquid drain with item glow. */
        INFUSION_COMPLETE,

        /** Liquid interaction — sparkle between fluid boundaries. */
        LIQUID_INTERACTION,

        /** Perk activation — aura flash around player position. */
        PERK_ACTIVATE,

        /** Crystal formation — slow crystallization particles. */
        CRYSTAL_FORM
    }
}
