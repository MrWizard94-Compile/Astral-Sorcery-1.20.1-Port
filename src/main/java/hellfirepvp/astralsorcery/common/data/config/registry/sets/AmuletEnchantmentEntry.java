/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Whitelist entry mapping an enchantment to a selection weight for the enchantment amulet.
 *
 * <p>1.16 → 1.20: getRegistryName() → ForgeRegistries.ENCHANTMENTS.getKey().</p>
 */
public class AmuletEnchantmentEntry implements Comparable<AmuletEnchantmentEntry> {

    private final Enchantment enchantment;
    private final int weight;

    public AmuletEnchantmentEntry(@Nonnull Enchantment enchantment, int weight) {
        this.enchantment = enchantment;
        this.weight = weight;
    }

    @Nonnull
    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int compareTo(AmuletEnchantmentEntry o) {
        return Integer.compare(this.weight, o.weight);
    }

    @Nonnull
    public String serialize() {
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return (key != null ? key.toString() : "unknown") + ";" + weight;
    }

    @Nullable
    public static AmuletEnchantmentEntry deserialize(@Nonnull String str) {
        String[] parts = str.split(";");
        if (parts.length < 2) return null;
        ResourceLocation key = new ResourceLocation(parts[0]);
        // Auto-ignore known problematic enchantments
        if (key.toString().equalsIgnoreCase("cofhcore:holding")) return null;
        Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(key);
        if (ench == null) {
            AstralSorcery.log.info("Ignoring amulet enchantment '{}' — not found in registry.", str);
            return null;
        }
        try {
            int w = Integer.parseInt(parts[1]);
            return new AmuletEnchantmentEntry(ench, w);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
