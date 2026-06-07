/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config.registry;

import hellfirepvp.astralsorcery.common.data.config.registry.sets.AmuletEnchantmentEntry;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Whitelist of enchantments that the enchantment amulet can roll and buff.
 * Defaults to all non-curse enchantments, weighted by their rarity.
 *
 * <p>1.16 → 1.20: removed ConfigDataAdapter dependency; standalone registry.</p>
 */
public final class AmuletEnchantmentRegistry {

    public static final AmuletEnchantmentRegistry INSTANCE = new AmuletEnchantmentRegistry();

    private static final Random RAND = new Random();

    private List<AmuletEnchantmentEntry> entries = null;

    private AmuletEnchantmentRegistry() {}

    // =========================================================================
    // Public API
    // =========================================================================

    /** Lazily-built list — safe to call after registry population is complete. */
    public List<AmuletEnchantmentEntry> getEntries() {
        if (entries == null) {
            entries = buildDefaults();
        }
        return Collections.unmodifiableList(entries);
    }

    public void setEntries(List<AmuletEnchantmentEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    /** Returns a random enchantment using weighted selection, or {@code null} if list is empty. */
    @Nullable
    public static Enchantment getRandomEnchant() {
        List<AmuletEnchantmentEntry> list = INSTANCE.getEntries();
        if (list.isEmpty()) return null;

        int total = list.stream().mapToInt(AmuletEnchantmentEntry::getWeight).sum();
        if (total <= 0) return list.get(RAND.nextInt(list.size())).getEnchantment();

        int roll = RAND.nextInt(total);
        int acc = 0;
        for (AmuletEnchantmentEntry e : list) {
            acc += e.getWeight();
            if (roll < acc) return e.getEnchantment();
        }
        return list.get(list.size() - 1).getEnchantment();
    }

    /** Returns {@code true} if the given enchantment is on the whitelist. */
    public static boolean canBeInfluenced(Enchantment ench) {
        return INSTANCE.getEntries().stream().anyMatch(e -> e.getEnchantment().equals(ench));
    }

    // =========================================================================
    // Defaults — all non-curse enchantments weighted by rarity
    // =========================================================================

    private static List<AmuletEnchantmentEntry> buildDefaults() {
        List<AmuletEnchantmentEntry> list = new ArrayList<>();
        for (Enchantment e : ForgeRegistries.ENCHANTMENTS.getValues()) {
            if (!e.isCurse()) {
                list.add(new AmuletEnchantmentEntry(e, e.getRarity().getWeight()));
            }
        }
        return list;
    }
}
