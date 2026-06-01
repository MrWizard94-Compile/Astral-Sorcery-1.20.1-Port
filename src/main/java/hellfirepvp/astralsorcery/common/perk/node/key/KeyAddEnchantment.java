/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentType;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Key perk that grants dynamic enchantment bonuses when allocated.
 * Enchantments can be registered by ResourceLocation (resolved lazily at
 * first event fire, when the Forge registry is fully populated) or directly
 * by Enchantment reference.
 *
 * EventHandlerPerkEffects dispatches applyEnchantments() on every
 * DynamicEnchantmentEvent.Add fired while this perk is allocated.
 */
public class KeyAddEnchantment extends KeyPerk {

    // Pending keys to resolve on first use
    private final List<ResourceLocation> pendingKeys = new ArrayList<>();
    private final List<Integer> pendingLevels = new ArrayList<>();
    private final List<DynamicEnchantment> enchantments = new ArrayList<>();
    private boolean resolved = false;

    public KeyAddEnchantment(int x, int y) {
        super(AstralSorcery.key("key_add_enchantment"), x, y);
    }

    public KeyAddEnchantment(@Nonnull ResourceLocation key, int x, int y) {
        super(key, x, y);
    }

    /** Register an enchantment by registry key — resolved lazily on first use. */
    @Nonnull
    public KeyAddEnchantment addEnchantment(@Nonnull ResourceLocation enchantmentKey, int level) {
        pendingKeys.add(enchantmentKey);
        pendingLevels.add(level);
        resolved = false;
        return this;
    }

    @Nonnull
    public KeyAddEnchantment addEnchantment(@Nonnull Enchantment ench, int level) {
        enchantments.add(new DynamicEnchantment(DynamicEnchantmentType.ADD_TO_SPECIFIC, ench, level));
        return this;
    }

    @Nonnull
    public KeyAddEnchantment addAllEnchantmentIncrease(int level) {
        enchantments.add(new DynamicEnchantment(DynamicEnchantmentType.ADD_TO_EXISTING_ALL, level));
        return this;
    }

    private void resolveIfNeeded() {
        if (resolved) return;
        resolved = true;
        for (int i = 0; i < pendingKeys.size(); i++) {
            Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(pendingKeys.get(i));
            if (ench != null) {
                enchantments.add(new DynamicEnchantment(DynamicEnchantmentType.ADD_TO_SPECIFIC, ench, pendingLevels.get(i)));
            }
        }
        pendingKeys.clear();
        pendingLevels.clear();
    }

    public void applyEnchantments(@Nonnull DynamicEnchantmentEvent.Add event) {
        resolveIfNeeded();
        if (enchantments.isEmpty()) return;

        Player player = event.getResolvedPlayer();
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || !progress.hasPerkAllocated(this)) return;

        List<DynamicEnchantment> toApply = event.getEnchantmentsToApply();
        for (DynamicEnchantment ench : enchantments) {
            DynamicEnchantment existing = toApply.stream()
                    .filter(e -> e.getType() == ench.getType() && e.getEnchantment() == ench.getEnchantment())
                    .findFirst().orElse(null);
            if (existing != null) {
                existing.setLevelAddition(existing.getLevelAddition() + ench.getLevelAddition());
            } else {
                toApply.add(ench.copy());
            }
        }
    }
}
