package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.AstralAdvancementTriggers;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
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
     * Also grants any {@link ResearchProgression} entries that become eligible at the new tier.
     * Does NOT sync — callers must call {@link PlayerProgressManager#syncProgress} when done.
     *
     * @return true if the tier was actually advanced
     */
    public static boolean grantTier(@Nonnull ServerPlayer player, @Nonnull ProgressionTier tier) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return false;
        if (progress.isAtLeast(tier)) {
            grantEligibleProgressions(progress);
            return false;
        }
        progress.setTierReached(tier);
        AstralAdvancementTriggers.REACH_TIER.trigger(player);
        grantEligibleProgressions(progress);
        return true;
    }

    /**
     * Grants all {@link ResearchProgression} entries the player is now eligible for
     * based on their current tier and already-unlocked progressions.
     * Called automatically by {@link #grantTier} and on player login.
     */
    public static void grantEligibleProgressions(@Nonnull PlayerProgress progress) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ResearchProgression prog : ResearchProgression.values()) {
                if (progress.hasResearch(prog)) continue;
                if (!progress.isAtLeast(prog.getRequiredProgress())) continue;
                boolean preMet = prog.getPreConditions().stream()
                        .allMatch(progress::hasResearch);
                if (preMet) {
                    progress.forceGainResearch(prog);
                    changed = true;
                }
            }
        }
    }

    /**
     * Called on player login to ensure they have DISCOVERY research and any
     * progressions they have already earned through their tier.
     */
    public static void onPlayerLogin(@Nonnull ServerPlayer player) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return;
        // Always grant DISCOVERY — every player starts here
        progress.forceGainResearch(ResearchProgression.DISCOVERY);
        grantEligibleProgressions(progress);
        sync(player);
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
            IConstellation cst = ConstellationRegistry.getConstellation(constellationKey);
            if (cst != null) {
                AstralAdvancementTriggers.DISCOVER_CONSTELLATION.trigger(player, cst);
            }
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
        IConstellation cst = ConstellationRegistry.getConstellation(constellationKey);
        if (cst instanceof IMajorConstellation major) {
            AstralAdvancementTriggers.ATTUNE_SELF.trigger(player, major);
        }
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

    public static boolean memorizeConstellation(@Nonnull ServerPlayer player,
                                                @Nonnull ResourceLocation constellationKey) {
        return discoverConstellation(player, constellationKey);
    }

    public static boolean setExp(@Nonnull ServerPlayer player, long exp) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return false;
        progress.setPerkExp(Math.max(0, exp));
        sync(player);
        return true;
    }

    public static void wipeProgress(@Nonnull ServerPlayer player) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return;
        hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper.removeAllVanillaModifiers(player);
        progress.setTierReached(ProgressionTier.DISCOVERY);
        progress.setAttunedConstellation(null);
        progress.setOnceAttuned(false);
        progress.clearDiscoveredConstellations();
        progress.clearAllocatedPerks();
        progress.setPerkExp(0);
        progress.setPerkPoints(0);
        progress.setUnlockedResearch(java.util.Collections.emptyList());
        progress.forceGainResearch(ResearchProgression.DISCOVERY);
        sync(player);
    }

    public static boolean forceMaximizeAll(@Nonnull ServerPlayer player) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return false;
        progress.setTierReached(ProgressionTier.TRAIT_CRAFT);
        for (IConstellation cst : ConstellationRegistry.getAllConstellations()) {
            progress.discoverConstellation(cst.getRegistryName());
        }
        grantEligibleProgressions(progress);
        sync(player);
        return true;
    }

    private static void sync(@Nonnull ServerPlayer player) {
        PlayerProgressManager.syncProgress(player);
    }
}
