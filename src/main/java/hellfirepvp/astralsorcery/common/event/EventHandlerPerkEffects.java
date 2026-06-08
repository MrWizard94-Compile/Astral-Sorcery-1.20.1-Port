/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.AstralAdvancementTriggers;
import hellfirepvp.astralsorcery.common.auxiliary.BlockBreakHelper;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.util.block.TreeDiscoverer;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyAddEnchantment;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyAreaOfEffect;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyBleed;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyCheatDeath;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyCullingAttack;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDamageEffects;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyDisarm;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyLastBreath;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyLightningArc;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyNoArmor;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyProjectileDistance;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyProjectileProximity;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyRampage;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyStoneEnrichment;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyTreeConnector;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyVoidTrash;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Centralized Forge event handler for all event-based key perk effects.
 *
 * <p>Each perk's gameplay effect is implemented here rather than in the perk
 * class itself, following the 1.20 pattern of centralized event dispatch
 * (vs the 1.16 pattern of per-perk {@code attachListeners}).</p>
 *
 * <p>The helper {@link #hasPerk(Player, ResourceLocation)} checks whether the
 * player has a given perk allocated via the capability system.</p>
 */
public class EventHandlerPerkEffects {

    /** Re-entry guard: prevents recursive tree/chain-break events when we break extra blocks. */
    public static final ThreadLocal<Boolean> IS_CHAIN_BREAKING = ThreadLocal.withInitial(() -> false);

    // -----------------------------------------------------------------------
    // Cooldown tracking (in-memory; clears on restart, acceptable for now)
    // -----------------------------------------------------------------------

    /** UUID → tick timestamp of when a cooldown was last set. */
    private static final Map<UUID, Long> CHEAT_DEATH_COOLDOWN = new WeakHashMap<>();

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    private static boolean hasPerk(@Nonnull Player player, @Nonnull ResourceLocation key) {
        AbstractPerk perk = PerkTree.getPerk(key);
        if (perk == null) return false;
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        return progress != null && progress.hasPerkAllocated(perk);
    }

    // -----------------------------------------------------------------------
    // KeyNoKnockback — cancel knockback on players
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onKnockback(@Nonnull LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (hasPerk(player, AstralSorcery.key("key_no_knockback"))) {
            event.setCanceled(true);
        }
    }

    // -----------------------------------------------------------------------
    // KeyNoArmor — 30% damage reduction when wearing < 2 armor pieces
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onNoArmorDamage(@Nonnull LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_no_armor"))) return;

        int equippedPieces = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!player.getItemBySlot(slot).isEmpty()) equippedPieces++;
        }
        if (equippedPieces < 2) {
            event.setAmount(event.getAmount() * KeyNoArmor.DAMAGE_MULTIPLIER);
        }
    }

    // -----------------------------------------------------------------------
    // KeyLastBreath — scale attack damage and dig speed by missing health
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLastBreathAttack(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_last_breath"))) return;

        float missingFraction = 1.0f - (player.getHealth() / player.getMaxHealth());
        float bonus = 1.0f + missingFraction * (KeyLastBreath.DAMAGE_MULTIPLIER - 1.0f);
        event.setAmount(event.getAmount() * bonus);
    }

    @SubscribeEvent
    public void onLastBreathDigSpeed(@Nonnull PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!hasPerk(player, AstralSorcery.key("key_last_breath"))) return;

        float missingFraction = 1.0f - (player.getHealth() / player.getMaxHealth());
        float bonus = 1.0f + missingFraction * (KeyLastBreath.DIG_MULTIPLIER - 1.0f);
        event.setNewSpeed(event.getNewSpeed() * bonus);
    }

    // -----------------------------------------------------------------------
    // KeyRampage — Speed II + Haste II + Strength I on kill
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRampageKill(@Nonnull LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!hasPerk(player, AstralSorcery.key("key_rampage"))) return;

        int dur = KeyRampage.RAMPAGE_TICKS;
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.MOVEMENT_SPEED),  dur, 1, false, false));
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DIG_SPEED),       dur, 1, false, false));
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DAMAGE_BOOST),    dur, 0, false, false));
    }

    // -----------------------------------------------------------------------
    // KeyBleed — 25% chance to apply Weakness I for 2 s on melee hit
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onBleedAttack(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_bleed"))) return;

        if (player.getRandom().nextFloat() < KeyBleed.BLEED_CHANCE) {
            event.getEntity().addEffect(new MobEffectInstance(
                    hellfirepvp.astralsorcery.common.lib.EffectsAS.EFFECT_BLEED.get(),
                    KeyBleed.BLEED_DURATION, 0, false, true));
        }
    }

    // -----------------------------------------------------------------------
    // KeyMagnetDrops — pull all mob drops into the player's inventory on kill
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onMagnetDrops(@Nonnull LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_magnet_drops"))) return;

        List<ItemEntity> unabsorbed = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack remaining = drop.getItem().copy();
            remaining = player.getInventory().add(remaining) ? ItemStack.EMPTY : remaining;
            if (!remaining.isEmpty()) {
                ItemEntity leftover = new ItemEntity(player.level(),
                        drop.getX(), drop.getY(), drop.getZ(), remaining);
                unabsorbed.add(leftover);
            }
        }
        event.getDrops().clear();
        event.getDrops().addAll(unabsorbed);
    }

    // -----------------------------------------------------------------------
    // KeyVoidTrash — remove trash items from mob loot
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onVoidTrash(@Nonnull LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_void_trash"))) return;

        event.getDrops().removeIf(drop -> KeyVoidTrash.TRASH_ITEMS.contains(drop.getItem().getItem()));
    }

    // -----------------------------------------------------------------------
    // KeyCullingAttack — instantly kill mobs below 15% max health
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCullingAttack(@Nonnull LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!hasPerk(player, AstralSorcery.key("key_culling_attack"))) return;

        LivingEntity target = event.getEntity();
        if (target instanceof Player) return; // never cull players
        float lifePercent = target.getHealth() / target.getMaxHealth();
        if (lifePercent < KeyCullingAttack.CULL_THRESHOLD) {
            target.kill();
            event.setCanceled(true);
        }
    }

    // -----------------------------------------------------------------------
    // KeyDamageArmor — damage one random armor piece on the target per hit
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onDamageArmor(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_damage_armor"))) return;

        LivingEntity target = event.getEntity();
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET};
        List<EquipmentSlot> worn = new ArrayList<>();
        for (EquipmentSlot slot : armorSlots) {
            if (!target.getItemBySlot(slot).isEmpty()) worn.add(slot);
        }
        if (!worn.isEmpty()) {
            EquipmentSlot chosen = worn.get(player.getRandom().nextInt(worn.size()));
            ItemStack armor = target.getItemBySlot(chosen);
            if (armor.isDamageableItem()) {
                armor.setDamageValue(armor.getDamageValue() + 1);
                if (armor.getDamageValue() >= armor.getMaxDamage()) {
                    target.setItemSlot(chosen, ItemStack.EMPTY);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // KeyDisarm — 20% chance to knock a weapon from the mob's main hand
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onDisarm(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_disarm"))) return;
        if (player.getRandom().nextFloat() >= KeyDisarm.DISARM_CHANCE) return;

        LivingEntity target = event.getEntity();
        ItemStack mainHand = target.getMainHandItem();
        if (!mainHand.isEmpty()) {
            target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            ItemEntity dropped = new ItemEntity(target.level(),
                    target.getX(), target.getY() + 0.5, target.getZ(), mainHand.copy());
            dropped.setPickUpDelay(40);
            target.level().addFreshEntity(dropped);
        }
    }

    // -----------------------------------------------------------------------
    // KeyCheatDeath — cancel player death once per cooldown, restore 4 hearts
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCheatDeath(@Nonnull LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_cheat_death"))) return;

        UUID id = player.getUUID();
        long now = player.level().getGameTime();
        Long lastUsed = CHEAT_DEATH_COOLDOWN.get(id);
        if (lastUsed != null && (now - lastUsed) < KeyCheatDeath.COOLDOWN_TICKS) return;

        CHEAT_DEATH_COOLDOWN.put(id, now);
        event.setCanceled(true);
        player.setHealth(8.0f); // 4 hearts
        player.addEffect(new MobEffectInstance(
                Objects.requireNonNull(MobEffects.REGENERATION),
                KeyCheatDeath.REGEN_DURATION, 1, false, true));
    }

    // -----------------------------------------------------------------------
    // KeyLightningArc — chain lightning through 3 enemies on hit (60% chance)
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLightningArc(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_lightning_arc"))) return;
        if (player.getRandom().nextFloat() >= KeyLightningArc.ARC_CHANCE) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        float arcDamage = event.getAmount() * KeyLightningArc.ARC_DAMAGE;
        LivingEntity primary = event.getEntity();
        Set<LivingEntity> hit = new HashSet<>();
        hit.add(primary);
        hit.add(player);

        LivingEntity current = primary;
        for (int chain = 0; chain < KeyLightningArc.ARC_CHAINS; chain++) {
            AABB search = current.getBoundingBox().inflate(KeyLightningArc.ARC_RANGE);
            @Nullable LivingEntity next = null;
            double bestDist = Double.MAX_VALUE;
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, search)) {
                if (hit.contains(nearby) || !nearby.isAlive()) continue;
                double d = nearby.distanceToSqr(current);
                if (d < bestDist) { bestDist = d; next = nearby; }
            }
            if (next == null) break;
            hit.add(next);
            next.hurt(level.damageSources().lightningBolt(), arcDamage);
            current = next;
        }
    }

    // -----------------------------------------------------------------------
    // KeyAreaOfEffect — sweep 50% damage to entities within 2.5 blocks
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAreaOfEffect(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_area_of_effect"))) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        LivingEntity primary = event.getEntity();
        float sweepDamage = event.getAmount() * KeyAreaOfEffect.SWEEP_PERCENT;
        AABB search = primary.getBoundingBox().inflate(KeyAreaOfEffect.SWEEP_RANGE);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, search)) {
            if (nearby == primary || nearby == player || !nearby.isAlive()) continue;
            nearby.hurt(level.damageSources().playerAttack(player), sweepDamage);
        }
    }

    // -----------------------------------------------------------------------
    // KeyDamageEffects — apply Poison I for 3 s on hit
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onDamageEffects(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_damage_effects"))) return;

        event.getEntity().addEffect(new MobEffectInstance(
                Objects.requireNonNull(MobEffects.POISON),
                KeyDamageEffects.POISON_DURATION, 0, false, true));
    }

    // -----------------------------------------------------------------------
    // KeyCleanseBadPotions — remove a harmful effect on heal
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCleanseBadPotions(@Nonnull LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!hasPerk(player, AstralSorcery.key("key_cleanse_bad_potions"))) return;

        List<MobEffectInstance> harmful = player.getActiveEffects().stream()
                .filter(e -> !e.getEffect().isBeneficial())
                .toList();
        if (harmful.isEmpty()) return;

        // Chance scales with heal amount: more healing → higher cleanse chance
        float healAmount = event.getAmount();
        float chance = Math.min(1.0f, healAmount * 0.1f);
        if (player.getRandom().nextFloat() < chance) {
            MobEffectInstance toRemove = harmful.get(player.getRandom().nextInt(harmful.size()));
            player.removeEffect(toRemove.getEffect());
        }
    }

    // -----------------------------------------------------------------------
    // KeyProjectileDistance — bonus damage scaled by travel distance
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onProjectileDistance(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof Projectile)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_projectile_distance"))) return;

        // Approximate travel distance as distance from player to target
        double dist = player.distanceTo(event.getEntity());
        if (dist <= KeyProjectileDistance.BASE_DISTANCE) return;

        float bonus = (float) ((dist - KeyProjectileDistance.BASE_DISTANCE) / 10.0)
                * KeyProjectileDistance.BONUS_PER_10_BLOCKS;
        bonus = Math.min(bonus, KeyProjectileDistance.MAX_BONUS);
        event.setAmount(event.getAmount() * (1.0f + bonus));
    }

    // -----------------------------------------------------------------------
    // KeyProjectileProximity — bonus for close-range projectile hits
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onProjectileProximity(@Nonnull LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof Projectile)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_projectile_proximity"))) return;

        double dist = player.distanceTo(event.getEntity());
        if (dist < KeyProjectileProximity.PROXIMITY_DISTANCE) {
            event.setAmount(event.getAmount() * (1.0f + KeyProjectileProximity.PROXIMITY_BONUS));
        }
    }

    // -----------------------------------------------------------------------
    // KeyStoneEnrichment — 20% chance to double drops from stone-type blocks
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onStoneEnrichment(@Nonnull BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_stone_enrichment"))) return;
        if (player.getRandom().nextFloat() >= KeyStoneEnrichment.DROP_BONUS_CHANCE) return;

        BlockState state = event.getState();
        if (!isStoneType(state)) return;

        BlockPos pos = event.getPos();
        if (!(player.level() instanceof ServerLevel level)) return;

        // Drop an extra copy of the block's loot
        state.getBlock().playerDestroy(level, player, pos, state,
                level.getBlockEntity(pos), player.getMainHandItem());
    }

    private static boolean isStoneType(@Nonnull BlockState state) {
        String id = state.getBlock().getDescriptionId();
        return id.contains("stone") || id.contains("cobblestone") || id.contains("andesite")
                || id.contains("diorite") || id.contains("granite") || id.contains("deepslate")
                || id.contains("gravel");
    }

    // -----------------------------------------------------------------------
    // KeyTreeConnector — fell entire tree when any log is broken with an axe
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onTreeConnector(@Nonnull BlockEvent.BreakEvent event) {
        if (IS_CHAIN_BREAKING.get()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!hasPerk(player, AstralSorcery.key("key_tree_connector"))) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        ItemStack tool = player.getMainHandItem();
        if (!tool.canPerformAction(net.minecraftforge.common.ToolActions.AXE_DIG)) return;
        if (!TreeDiscoverer.isLog(event.getState())) return;

        // Tag-based flood-fill; respects all log variants including modded wood
        java.util.List<BlockPos> logs = TreeDiscoverer.findConnectedLogs(level, event.getPos());
        if (logs.size() <= 1) return;

        IS_CHAIN_BREAKING.set(true);
        try {
            int broken = 0;
            for (BlockPos logPos : logs) {
                if (logPos.equals(event.getPos())) continue;
                if (broken >= KeyTreeConnector.MAX_LOGS) break;
                // BlockBreakHelper fires break event and applies Fortune/Silk Touch
                BlockBreakHelper.breakBlock(level, logPos, true);
                broken++;
            }
        } finally {
            IS_CHAIN_BREAKING.set(false);
        }
    }

    // -----------------------------------------------------------------------
    // KeyDigTypes — pickaxe mines shovel/axe blocks at full speed
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onDigTypesBreakSpeed(@Nonnull PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!hasPerk(player, AstralSorcery.key("key_dig_types"))) return;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) return;
        // If held item is a pickaxe and block prefers shovel/axe, apply dig speed
        if (!heldItem.canPerformAction(net.minecraftforge.common.ToolActions.PICKAXE_DIG)) return;

        BlockState state = event.getState();
        if (state.canBeReplaced()) return;
        // Let pickaxe harvest any block it can't normally harvest at tool speed
        if (!state.requiresCorrectToolForDrops()) return;
        if (!heldItem.isCorrectToolForDrops(state)) {
            // Apply standard pickaxe speed instead of the slow hand-break speed
            float pickaxeSpeed = heldItem.getDestroySpeed(state);
            if (pickaxeSpeed > 1.0f) {
                event.setNewSpeed(Math.max(event.getNewSpeed(), pickaxeSpeed));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Perk experience gain — convert vanilla XP pickup to perk exp
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onXpPickup(@Nonnull PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null) return;
        if (progress.getAttunedConstellation() == null) return;

        double multiplier = CommonConfig.CONFIG.perkExpMultiplier.get();
        long perkExpGain = (long) (event.getOrb().getValue() * 10L * multiplier);
        progress.setPerkExp(progress.getPerkExp() + perkExpGain);
        AstralAdvancementTriggers.PERK_LEVEL.trigger(serverPlayer);
        PlayerProgressManager.syncProgress(serverPlayer);
    }

    @SubscribeEvent
    public void onDynamicEnchantmentAdd(@Nonnull DynamicEnchantmentEvent.Add event) {
        for (AbstractPerk perk : PerkTree.getAllPerks()) {
            if (perk instanceof KeyAddEnchantment keyEnch) {
                keyEnch.applyEnchantments(event);
            }
        }
    }
}
