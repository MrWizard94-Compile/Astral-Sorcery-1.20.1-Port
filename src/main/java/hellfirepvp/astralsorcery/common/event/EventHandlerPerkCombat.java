/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyPelotrio;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVorux;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Handles perk effects related to combat: damage dealt, damage taken,
 * healing, life steal, critical hits, and elemental resistance.
 *
 * <p>This handler connects the perk attribute system to actual game events.
 * Perk modifiers applied via the attribute helper modify the effective
 * damage/healing values through event manipulation.</p>
 *
 * <p>Event priority: HIGH for outgoing damage (apply bonuses early),
 * LOW for incoming damage (apply reduction after other mods).</p>
 *
 * <p>1.16 → 1.20 changes:
 * LivingHurtEvent, LivingDamageEvent API unchanged.
 * DamageSource access patterns stable.</p>
 */
public class EventHandlerPerkCombat {

    /**
     * Applies perk attack damage bonuses when the player attacks.
     * Handles: Discidia attack damage, crit chance/multiplier.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerDealDamage(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || progress.getAllocatedPerks().isEmpty()) {
            return;
        }

        float baseDamage = event.getAmount();

        Set<ResourceLocation> allocated = progress.getAllocatedPerks();

        // Apply flat attack damage bonus from perks
        List<PerkAttributeModifier> modifiers = PerkAttributeHelper.collectModifiers(
                player, allocated,
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE.getKey());
        if (!modifiers.isEmpty()) {
            // The vanilla attribute system handles the base attack damage modifier,
            // but ADDED_MULTIPLY and STACKING_MULTIPLY layers may apply here
            double modified = PerkAttributeHelper.applyModifiers(baseDamage, modifiers);
            baseDamage = (float) modified;
        }

        // KeyDiscidia: full-health bonus damage (25% extra at full HP)
        ResourceLocation discidiaKey = AstralSorcery.key("key_discidia");
        if (allocated.contains(discidiaKey)) {
            if (player.getHealth() >= player.getMaxHealth() - 0.1f) {
                baseDamage *= 1.25f;
            }
        }

        // KeyVorux: low-health berserker bonus (up to 50% extra at low HP)
        ResourceLocation voruxKey = AstralSorcery.key("key_vorux");
        if (allocated.contains(voruxKey)) {
            baseDamage += baseDamage * KeyVorux.getLowHealthBonus(player);
        }

        // Critical hit chance (separate from vanilla crit)
        float critChance = getCritChance(player, progress);
        if (critChance > 0 && player.getRandom().nextFloat() < critChance) {
            float critMult = getCritMultiplier(player, progress);
            baseDamage *= critMult;
        }

        event.setAmount(baseDamage);
    }

    /**
     * Applies perk damage reduction when the player takes damage.
     * Handles: Armara armor bonuses, elemental resistance, Horologium time freeze.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTakeDamage(@Nonnull LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || progress.getAllocatedPerks().isEmpty()) {
            return;
        }

        float damage = event.getAmount();

        // Pelotrio revive: prevent death if the perk is allocated and off cooldown
        if (player.getHealth() - damage <= 0) {
            ResourceLocation pelotrioKey = AstralSorcery.key("key_pelotrio");
            if (progress.getAllocatedPerks().contains(pelotrioKey)) {
                AbstractPerk perk = PerkTree.getPerk(pelotrioKey);
                if (perk instanceof KeyPelotrio pelotrio && pelotrio.tryRevive(player)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // Elemental resistance (fire, magic, etc.)
        // In 1.20, damage type checks use tags instead of isFire()/isMagic()
        if (event.getSource().is(DamageTypeTags.IS_FIRE)
                || event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            List<PerkAttributeModifier> resistMods = PerkAttributeHelper.collectModifiers(
                    player, progress.getAllocatedPerks(),
                    PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST.getKey());
            if (!resistMods.isEmpty()) {
                double resistance = PerkAttributeHelper.applyModifiers(0.0, resistMods);
                // Resistance is a flat reduction percentage, capped at 80%
                float reduction = (float) Math.min(resistance, 0.80);
                damage *= (1.0f - reduction);
            }
        }

        // Horologium time-freeze chance: 10% chance to nullify damage completely
        if (hasHorologiumPerk(progress)) {
            float freezeChance = 0.10f;
            if (player.getRandom().nextFloat() < freezeChance) {
                event.setCanceled(true);
                net.minecraft.core.BlockPos freezePos = player.blockPosition();
                PacketChannel.sendToAllTracking(
                        new PktParticleEvent(PktParticleEvent.RITUAL_ACTIVATE, freezePos),
                        (net.minecraft.server.level.ServerLevel) player.level(), freezePos);
                return;
            }
        }

        event.setAmount(Math.max(0, damage));
    }

    /**
     * Applies life steal effect when the player deals damage.
     * Triggers after the hurt event is processed (so we know actual damage dealt).
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamageDealtLifeSteal(@Nonnull LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || progress.getAllocatedPerks().isEmpty()) {
            return;
        }

        // Life steal: heal for a % of damage dealt
        List<PerkAttributeModifier> stealMods = PerkAttributeHelper.collectModifiers(
                player, progress.getAllocatedPerks(),
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL.getKey());
        if (!stealMods.isEmpty()) {
            double stealPercent = PerkAttributeHelper.applyModifiers(0.0, stealMods);
            if (stealPercent > 0) {
                float healAmount = (float) (event.getAmount() * Math.min(stealPercent, 0.5));
                if (healAmount > 0.1f) {
                    player.heal(healAmount);
                }
            }
        }
    }

    /**
     * Modifies player healing by perk effects.
     * Currently no perk reduces healing, but the hook is in place for future use.
     */
    @SubscribeEvent
    public void onPlayerHeal(@Nonnull LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || progress.getAllocatedPerks().isEmpty()) {
            return;
        }

        // Perk effect amplifier could boost healing here
        // For now, leave as a hook for future constellation effects
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private float getCritChance(@Nonnull Player player, @Nonnull PlayerProgress progress) {
        List<PerkAttributeModifier> mods = PerkAttributeHelper.collectModifiers(
                player, progress.getAllocatedPerks(),
                PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE.getKey());
        if (mods.isEmpty()) return 0;
        return (float) Math.min(PerkAttributeHelper.applyModifiers(0.0, mods), 1.0);
    }

    private float getCritMultiplier(@Nonnull Player player, @Nonnull PlayerProgress progress) {
        List<PerkAttributeModifier> mods = PerkAttributeHelper.collectModifiers(
                player, progress.getAllocatedPerks(),
                PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER.getKey());
        if (mods.isEmpty()) return 1.5f; // Default crit multiplier
        return (float) Math.max(PerkAttributeHelper.applyModifiers(1.5, mods), 1.0);
    }

    private boolean hasHorologiumPerk(@Nonnull PlayerProgress progress) {
        return progress.getAllocatedPerks().stream()
                .anyMatch(key -> key.getPath().contains("key_horologium"));
    }
}
