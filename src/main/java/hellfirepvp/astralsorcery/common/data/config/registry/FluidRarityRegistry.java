/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config.registry;

import hellfirepvp.astralsorcery.common.data.config.registry.sets.FluidRarityEntry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Registry of fluid rarities used by the Evershifting Fountain to pick which
 * fluid fills the structure. Lower rarity = rarer selection.
 *
 * <p>Loaded from the default values hardcoded below; replaceable at runtime
 * by calling {@link #setEntries(List)}.</p>
 *
 * <p>1.16 → 1.20: removed ConfigDataAdapter dependency; standalone registry.</p>
 */
public final class FluidRarityRegistry {

    public static final FluidRarityRegistry INSTANCE = new FluidRarityRegistry();

    private List<FluidRarityEntry> entries;

    private FluidRarityRegistry() {
        this.entries = buildDefaults();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /** Returns an unmodifiable view of the current fluid rarity entries. */
    public List<FluidRarityEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Replaces the entry list (e.g. after loading from config). */
    public void setEntries(List<FluidRarityEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    /**
     * Picks a random fluid entry using weighted selection proportional to rarity.
     * Returns {@code null} if no valid entry exists.
     */
    @Nullable
    public FluidRarityEntry getRandomEntry(Random rand) {
        List<FluidRarityEntry> list = entries;
        if (list.isEmpty()) return null;

        int totalWeight = list.stream().mapToInt(FluidRarityEntry::getRarity).sum();
        if (totalWeight <= 0) return list.get(rand.nextInt(list.size()));

        int roll = rand.nextInt(totalWeight);
        int accumulated = 0;
        for (FluidRarityEntry entry : list) {
            accumulated += entry.getRarity();
            if (roll < accumulated) return entry;
        }
        return list.get(list.size() - 1);
    }

    // =========================================================================
    // Defaults
    // =========================================================================

    private static List<FluidRarityEntry> buildDefaults() {
        List<FluidRarityEntry> list = new ArrayList<>();
        list.add(new FluidRarityEntry(mc("water"), 14000, Integer.MAX_VALUE, 0));
        list.add(new FluidRarityEntry(mc("lava"),   7500, 4_000_000, 500_000));
        return list;
    }

    private static ResourceLocation mc(String name) {
        return new ResourceLocation("minecraft", name);
    }
}
