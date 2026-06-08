/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.AstralAdvancementTriggers;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The central perk tree structure. Holds all registered perks and manages
 * player allocation/deallocation with validation.
 *
 * <p>The tree is a graph of {@link AbstractPerk} nodes connected by
 * adjacency edges. Players can allocate perks if:
 * <ul>
 *   <li>They have unspent perk points</li>
 *   <li>The perk is adjacent to an already-allocated perk (reachable from root)</li>
 *   <li>They meet any constellation requirements</li>
 *   <li>They have the required progression tier (ATTUNEMENT minimum)</li>
 * </ul>
 *
 * <p>Deallocation checks that removing the perk would not disconnect any
 * other allocated perks from the root (preserves tree connectivity).</p>
 *
 * <p>Player allocation state is stored in {@link PlayerProgress} as a
 * Set&lt;ResourceLocation&gt; of allocated perk keys.</p>
 */
public final class PerkTree {

    private PerkTree() {}

    /** All registered perks keyed by their ResourceLocation. */
    private static final Map<ResourceLocation, AbstractPerk> PERKS = new LinkedHashMap<>();

    /** Root perks (one per constellation, entry points to the tree). */
    private static final Set<ResourceLocation> ROOT_PERKS = new LinkedHashSet<>();

    /** Tree edge connections. Each entry maps a perk to its adjacent perks. */
    private static final Map<ResourceLocation, Set<ResourceLocation>> CONNECTIONS = new HashMap<>();

    // ========================================================================
    // Registration
    // ========================================================================

    /**
     * Registers a perk in the tree. Must be called during initialization.
     *
     * @param perk the perk to register
     */
    public static void register(@Nonnull AbstractPerk perk) {
        if (PERKS.containsKey(perk.getKey())) {
            AstralSorcery.log.warn("Duplicate perk registration: {}", perk.getKey());
            return;
        }
        PERKS.put(perk.getKey(), perk);

        if (perk.getCategory() == AbstractPerk.PerkCategory.ROOT) {
            ROOT_PERKS.add(perk.getKey());
        }

        // Ensure adjacency entries exist
        CONNECTIONS.computeIfAbsent(perk.getKey(), k -> new LinkedHashSet<>());
    }

    /**
     * Adds a bidirectional connection between two perks.
     * Both perks must be registered first.
     */
    public static void connect(@Nonnull ResourceLocation a, @Nonnull ResourceLocation b) {
        if (!PERKS.containsKey(a) || !PERKS.containsKey(b)) {
            AstralSorcery.log.warn("Cannot connect unregistered perks: {} <-> {}", a, b);
            return;
        }
        CONNECTIONS.computeIfAbsent(a, k -> new LinkedHashSet<>()).add(b);
        CONNECTIONS.computeIfAbsent(b, k -> new LinkedHashSet<>()).add(a);

        PERKS.get(a).addAdjacent(b);
        PERKS.get(b).addAdjacent(a);
    }

    // ========================================================================
    // Lookup
    // ========================================================================

    @Nullable
    public static AbstractPerk getPerk(@Nonnull ResourceLocation key) {
        return PERKS.get(key);
    }

    @Nonnull
    public static Collection<AbstractPerk> getAllPerks() {
        return Collections.unmodifiableCollection(PERKS.values());
    }

    @Nonnull
    public static Set<ResourceLocation> getRootPerkKeys() {
        return Collections.unmodifiableSet(ROOT_PERKS);
    }

    @Nonnull
    public static Set<ResourceLocation> getAdjacentKeys(@Nonnull ResourceLocation perkKey) {
        return Collections.unmodifiableSet(
                CONNECTIONS.getOrDefault(perkKey, Collections.emptySet()));
    }

    /**
     * Returns all edges (connections) in the tree as Connection records.
     * Each edge appears once (not duplicated for bidirectionality).
     */
    @Nonnull
    public static List<Connection> getConnections() {
        Set<String> seen = new HashSet<>();
        List<Connection> result = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : CONNECTIONS.entrySet()) {
            ResourceLocation from = entry.getKey();
            for (ResourceLocation to : entry.getValue()) {
                // Deduplicate bidirectional edges
                String edgeKey = from.compareTo(to) < 0
                        ? from + "|" + to : to + "|" + from;
                if (seen.add(edgeKey)) {
                    AbstractPerk fromPerk = PERKS.get(from);
                    AbstractPerk toPerk = PERKS.get(to);
                    if (fromPerk != null && toPerk != null) {
                        result.add(new Connection(fromPerk, toPerk));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns all perk tree points (for rendering the tree).
     */
    @Nonnull
    public static List<PerkTreePoint> getPoints() {
        List<PerkTreePoint> points = new ArrayList<>();
        for (AbstractPerk perk : PERKS.values()) {
            points.add(perk.getTreePoint());
        }
        return points;
    }

    public static boolean isRegistered(@Nonnull ResourceLocation key) {
        return PERKS.containsKey(key);
    }

    public static int size() {
        return PERKS.size();
    }

    // ========================================================================
    // Connection record (edge between two perks)
    // ========================================================================

    /**
     * Represents an edge between two perk nodes in the tree.
     */
    public record Connection(@Nonnull AbstractPerk from, @Nonnull AbstractPerk to) {}

    // ========================================================================
    // Allocation
    // ========================================================================

    /**
     * Attempts to allocate a perk for a player.
     *
     * @param player   the player (nullable for unit testing only)
     * @param progress the player's progress data
     * @param perkKey  the perk to allocate
     * @return the allocation result
     */
    @Nonnull
    public static AllocationStatus allocate(@Nullable Player player,
                                            @Nonnull PlayerProgress progress,
                                            @Nonnull ResourceLocation perkKey) {
        AbstractPerk perk = PERKS.get(perkKey);
        if (perk == null || !perk.isEnabled()) {
            return AllocationStatus.FAILED;
        }

        Set<ResourceLocation> allocated = progress.getAllocatedPerks();

        // Already allocated?
        if (allocated.contains(perkKey)) {
            return AllocationStatus.ALREADY_ALLOCATED;
        }

        // Minimum tier check
        if (!progress.isAtLeast(ProgressionTier.ATTUNEMENT)) {
            return AllocationStatus.INSUFFICIENT_TIER;
        }

        // Constellation requirement
        if (perk.getRequiredConstellation() != null) {
            ResourceLocation playerConst = progress.getAttunedConstellation();
            if (playerConst == null || !playerConst.equals(perk.getRequiredConstellation())) {
                return AllocationStatus.WRONG_CONSTELLATION;
            }
        }

        // Perk points check
        int level = PerkLevelManager.getLevelFromExp(progress.getPerkExp());
        int totalPoints = PerkLevelManager.getPerkPointsForLevel(level);
        int spent = allocated.size();
        if (spent >= totalPoints) {
            return AllocationStatus.INSUFFICIENT_POINTS;
        }

        // Reachability: must be adjacent to an allocated perk OR be a root with no allocations yet
        if (!isReachable(perkKey, allocated)) {
            return AllocationStatus.NOT_REACHABLE;
        }

        // Allocate
        progress.allocatePerk(perkKey);
        if (player != null) {
            perk.onAllocate(player);
            AstralSorcery.log.debug("Player {} allocated perk {}",
                    player.getName().getString(), perkKey);
            if (player instanceof ServerPlayer sp) {
                AstralAdvancementTriggers.ALLOCATE_PERK.trigger(sp);
            }
        }
        return AllocationStatus.SUCCESS;
    }

    /**
     * Attempts to deallocate a perk for a player.
     *
     * @param player   the player (nullable for unit testing only)
     * @param progress the player's progress data
     * @param perkKey  the perk to deallocate
     * @return the allocation result
     */
    @Nonnull
    public static AllocationStatus deallocate(@Nullable Player player,
                                              @Nonnull PlayerProgress progress,
                                              @Nonnull ResourceLocation perkKey) {
        AbstractPerk perk = PERKS.get(perkKey);
        if (perk == null) {
            return AllocationStatus.FAILED;
        }

        Set<ResourceLocation> allocated = progress.getAllocatedPerks();

        if (!allocated.contains(perkKey)) {
            return AllocationStatus.NOT_ALLOCATED;
        }

        // Check: would removing this perk disconnect others from root?
        if (wouldDisconnect(perkKey, allocated)) {
            return AllocationStatus.WOULD_DISCONNECT;
        }

        // Cannot deallocate root perks if other perks depend on them
        if (perk.getCategory() == AbstractPerk.PerkCategory.ROOT) {
            Set<ResourceLocation> connections = CONNECTIONS.getOrDefault(perkKey, Collections.emptySet());
            for (ResourceLocation adj : connections) {
                if (allocated.contains(adj)) {
                    return AllocationStatus.WOULD_DISCONNECT;
                }
            }
        }

        progress.deallocatePerk(perkKey);
        if (player != null) {
            perk.onDeallocate(player);
            AstralSorcery.log.debug("Player {} deallocated perk {}",
                    player.getName().getString(), perkKey);
        }
        return AllocationStatus.SUCCESS;
    }

    // ========================================================================
    // Reachability and connectivity checks
    // ========================================================================

    /**
     * Checks if a perk is reachable from the player's current allocations.
     * A root perk is reachable if the player has no allocations yet.
     * A non-root perk is reachable if it's adjacent to any allocated perk.
     */
    private static boolean isReachable(@Nonnull ResourceLocation perkKey,
                                       @Nonnull Set<ResourceLocation> allocated) {
        // Root perks are always reachable (they're tree entry points)
        if (ROOT_PERKS.contains(perkKey)) {
            return true;
        }

        // Must be adjacent to at least one allocated perk
        Set<ResourceLocation> connections = CONNECTIONS.getOrDefault(perkKey, Collections.emptySet());
        for (ResourceLocation adj : connections) {
            if (allocated.contains(adj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if removing a perk would disconnect the allocation graph.
     * Uses BFS from roots to verify all remaining allocated perks are reachable.
     */
    private static boolean wouldDisconnect(@Nonnull ResourceLocation removing,
                                           @Nonnull Set<ResourceLocation> allocated) {
        // Build the set of allocations without the perk being removed
        Set<ResourceLocation> remaining = new HashSet<>(allocated);
        remaining.remove(removing);

        if (remaining.isEmpty()) {
            return false; // Nothing left to disconnect
        }

        // Find all root perks that are still allocated
        Set<ResourceLocation> roots = new HashSet<>();
        for (ResourceLocation key : remaining) {
            if (ROOT_PERKS.contains(key)) {
                roots.add(key);
            }
        }

        if (roots.isEmpty()) {
            // No roots remain — all remaining allocations are orphaned
            return true;
        }

        // BFS from roots through allocated connections
        Set<ResourceLocation> reachable = new HashSet<>();
        Deque<ResourceLocation> queue = new ArrayDeque<>(roots);
        reachable.addAll(roots);

        while (!queue.isEmpty()) {
            ResourceLocation current = queue.poll();
            Set<ResourceLocation> connections = CONNECTIONS.getOrDefault(current, Collections.emptySet());
            for (ResourceLocation adj : connections) {
                if (remaining.contains(adj) && reachable.add(adj)) {
                    queue.add(adj);
                }
            }
        }

        // If any remaining allocated perk is not reachable, removal would disconnect
        return !reachable.containsAll(remaining);
    }

    // ========================================================================
    // Ticking (for perks with ongoing effects)
    // ========================================================================

    /**
     * Ticks all allocated perks that have tick effects for a player.
     * Called by {@code PerkEffectHelper} each server tick.
     */
    public static void tickPerks(@Nonnull Player player, @Nonnull Set<ResourceLocation> allocated) {
        if (!hellfirepvp.astralsorcery.common.data.config.CommonConfig.CONFIG.perksWorkInAllDimensions.get()
                && !player.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return;
        }
        for (ResourceLocation key : allocated) {
            AbstractPerk perk = PERKS.get(key);
            if (perk != null && perk.isEnabled() && perk.hasTickEffect()) {
                perk.onPlayerTick(player);
            }
        }
    }

    // ========================================================================
    // Testing / reset
    // ========================================================================

    /**
     * Clears all registered perks and connections. Only for testing.
     */
    public static void clearForTesting() {
        PERKS.clear();
        ROOT_PERKS.clear();
        CONNECTIONS.clear();
    }
}
