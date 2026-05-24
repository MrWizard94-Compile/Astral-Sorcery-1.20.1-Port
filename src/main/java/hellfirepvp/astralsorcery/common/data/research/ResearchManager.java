package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.AstralAdvancementTriggers;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.infusion.ActiveLiquidInfusionRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityInfuser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Static utility for mutating and syncing player progression state.
 * All methods are server-side only.
 *
 * <p>1.16 → 1.20: ResearchHelper/ResearchSyncHelper collapsed into
 * PlayerProgressManager; ResearchProgression enum dropped (progress is now
 * tracked as unlocked ResourceLocation keys via PlayerProgress); tier-only
 * grants are sufficient for the gated content in the port.</p>
 */
public final class ResearchManager {

    private ResearchManager() {}

    /**
     * Advances the player's tier if {@code tier} is higher than their current tier.
     * Does NOT sync — callers must call {@link PlayerProgressManager#syncProgress} when done.
     *
     * @return true if the tier was actually advanced
     */
    public static boolean grantTier(@Nonnull ServerPlayer player, @Nonnull ProgressionTier tier) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return false;
        if (progress.isAtLeast(tier)) return false;
        progress.setTierReached(tier);
        AstralAdvancementTriggers.REACH_TIER.trigger(player);
        return true;
    }

    /**
     * Marks a constellation as discovered for the player and syncs to the client.
     *
     * @return true if it was newly discovered (not already in the set)
     */
    public static boolean discoverConstellation(@Nonnull ServerPlayer player,
                                                @Nonnull ResourceLocation constellationKey) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return false;
        boolean added = progress.discoverConstellation(constellationKey);
        if (added) {
            AstralAdvancementTriggers.DISCOVER_CONSTELLATION.trigger(player);
            sync(player);
        }
        return added;
    }

    /**
     * Sets the player's attuned constellation and marks them as once-attuned.
     * Also ensures the constellation is discovered and grants the ATTUNEMENT tier.
     * Syncs the updated progress to the client in one packet.
     */
    public static void attuneTo(@Nonnull ServerPlayer player,
                                @Nonnull ResourceLocation constellationKey) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return;
        progress.setAttunedConstellation(constellationKey);
        progress.discoverConstellation(constellationKey);
        grantTier(player, ProgressionTier.ATTUNEMENT);
        AstralAdvancementTriggers.ATTUNE_PLAYER.trigger(player);
        sync(player);
    }

    /**
     * Called when the infuser finishes a recipe. Grants BASIC_CRAFT tier to the
     * player that initiated the infusion and syncs in one packet.
     */
    public static void informCraftedInfuser(@Nonnull BlockEntityInfuser infuser,
                                            @Nonnull ActiveLiquidInfusionRecipe recipe,
                                            @Nonnull ItemStack crafted) {
        Player player = recipe.tryGetCraftingPlayerServer();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            AstralSorcery.log.warn("Infusion finished but initiating player not found. Tile: {} in {}",
                    infuser.getBlockPos(), infuser.getLevel() != null
                            ? infuser.getLevel().dimension().location() : "unknown");
            return;
        }
        grantTier(serverPlayer, ProgressionTier.BASIC_CRAFT);
        sync(serverPlayer);
    }

    /**
     * Called when an altar upgrade recipe completes. Grants the progression tier
     * matching the new altar tier (and all lower tiers via fallthrough) in one sync.
     *
     * @param player    the nearest server player (may be null — no-op if so)
     * @param altarType the tier that was just unlocked
     */
    @SuppressWarnings("fallthrough")
    public static void informCraftedAltar(@Nullable ServerPlayer player,
                                          @Nonnull BlockAltar.AltarType altarType) {
        if (player == null) return;
        switch (altarType) {
            case RADIANCE:
                grantTier(player, ProgressionTier.TRAIT_CRAFT);
                // fall through
            case CONSTELLATION:
                grantTier(player, ProgressionTier.CONSTELLATION_CRAFT);
                // fall through
            case ATTUNEMENT:
                grantTier(player, ProgressionTier.ATTUNEMENT);
                // fall through
            default:
                grantTier(player, ProgressionTier.BASIC_CRAFT);
        }
        sync(player);
    }

    private static void sync(@Nonnull ServerPlayer player) {
        PlayerProgressManager.syncProgress(player);
    }
}
