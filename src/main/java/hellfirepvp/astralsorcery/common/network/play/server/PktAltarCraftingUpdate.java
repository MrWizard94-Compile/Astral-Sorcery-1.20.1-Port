/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server → Client: Updates the client about altar crafting progress.
 * Sent when an altar starts crafting, during progress updates (for VFX),
 * and on craft completion or failure.
 */
public class PktAltarCraftingUpdate {

    public enum CraftState {
        /** Altar has started a new craft. */
        STARTED,
        /** Craft is in progress — update progress bar. */
        PROGRESS,
        /** Craft completed successfully. */
        COMPLETED,
        /** Craft failed (insufficient starlight, broken structure, etc.). */
        FAILED
    }

    @Nonnull
    private final BlockPos altarPos;
    @Nonnull
    private final CraftState state;
    /** Recipe being crafted (for VFX). Null for FAILED state. */
    @Nonnull
    private final ResourceLocation recipeId;
    /** Progress [0, 1] for PROGRESS state, craft tier for STARTED. */
    private final float progress;

    public PktAltarCraftingUpdate(@Nonnull BlockPos altarPos, @Nonnull CraftState state,
                                   @Nonnull ResourceLocation recipeId, float progress) {
        this.altarPos = altarPos;
        this.state = state;
        this.recipeId = recipeId;
        this.progress = progress;
    }

    public static void encode(@Nonnull PktAltarCraftingUpdate pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.altarPos);
        buf.writeEnum(pkt.state);
        buf.writeResourceLocation(pkt.recipeId);
        buf.writeFloat(pkt.progress);
    }

    @Nonnull
    public static PktAltarCraftingUpdate decode(@Nonnull FriendlyByteBuf buf) {
        return new PktAltarCraftingUpdate(
                buf.readBlockPos(),
                buf.readEnum(CraftState.class),
                buf.readResourceLocation(),
                buf.readFloat());
    }

    public static void handle(@Nonnull PktAltarCraftingUpdate pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(pkt));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktAltarCraftingUpdate pkt) {
        // Phase 12: dispatch VFX by state
        // - STARTED: begin crafting animation, particles
        // - PROGRESS: particle stream update
        // - COMPLETED: burst effect, item pop animation
        // - FAILED: fizzle particle effect
        // GUI progress bar reads isCrafting/recipeTick directly from BlockEntityAltar via normal BE sync.
    }

    @Nonnull
    public BlockPos getAltarPos() { return altarPos; }
    @Nonnull
    public CraftState getState() { return state; }
    @Nonnull
    public ResourceLocation getRecipeId() { return recipeId; }
    public float getProgress() { return progress; }
}
