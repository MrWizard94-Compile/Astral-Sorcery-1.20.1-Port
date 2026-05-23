/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Discidia (Major — Combat).
 *
 * <p>When a player wearing Discidia hits an entity, the damage they last received
 * is echoed back as a bonus attack (scaled by {@code damageMultiplier}).
 * A ThreadLocal flag prevents recursive re-entry from the echo attack itself.</p>
 *
 * <p>1.16 → 1.20: DamageSource.causePlayerDamage(player) → damageSources().playerAttack(player),
 * DAMAGE_SOURCE_STELLAR → damageSources().magic() (stellar source not yet ported),
 * EventFlags replaced by ThreadLocal re-entry guard,
 * getEntityLiving() → getEntity(), getTrueSource() → getSource().getEntity().</p>
 */
public class MantleEffectDiscidia extends MantleEffect {

    public static final DiscidiaConfig CONFIG = new DiscidiaConfig();

    private static final ThreadLocal<Boolean> ECHO_IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    public MantleEffectDiscidia() {
        super(ConstellationsAS.DISCIDIA);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(this::onAttack);
        bus.addListener(EventPriority.HIGHEST, this::onHurt);
    }

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    private void onAttack(LivingAttackEvent event) {
        if (ECHO_IN_PROGRESS.get()) return;

        LivingEntity attacked = event.getEntity();
        if (attacked.level().isClientSide()) return;

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player)) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.DISCIDIA) == null) return;

        float added = getLastAttackDamage(player);
        if (added > 0.1F && AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerAttack.get())) {
            ECHO_IN_PROGRESS.set(true);
            try {
                DamageUtil.shotgunAttack(attacked, e -> DamageUtil.attackEntityFrom(e,
                        player.damageSources().magic(), added / 2F));
                DamageUtil.shotgunAttack(attacked, e -> DamageUtil.attackEntityFrom(e,
                        player.damageSources().playerAttack(player), added / 2F));
            } finally {
                ECHO_IN_PROGRESS.set(false);
            }
            AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerAttack.get(), false);
        }
    }

    private void onHurt(LivingHurtEvent event) {
        LivingEntity hurt = event.getEntity();
        if (hurt.level().isClientSide()) return;
        if (ItemMantle.getEffect(hurt, ConstellationsAS.DISCIDIA) != null) {
            writeLastAttackDamage(hurt, event.getAmount());
        }
    }

    public void writeLastAttackDamage(LivingEntity entity, float dmgIn) {
        getData(entity).putFloat("lastAttack", dmgIn);
    }

    public float getLastAttackDamage(LivingEntity entity) {
        return getData(entity).getFloat("lastAttack") * CONFIG.damageMultiplier.get().floatValue();
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class DiscidiaConfig extends Config {

        public ForgeConfigSpec.DoubleValue damageMultiplier;
        public ForgeConfigSpec.IntValue chargeCostPerAttack;

        public DiscidiaConfig() {
            super("discidia");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.discidia");
            damageMultiplier = b
                    .comment("Multiplier applied to the last damage received when echoing as a bonus attack.")
                    .defineInRange("damageMultiplier", 1.5, 0.0, 100.0);
            chargeCostPerAttack = b
                    .comment("Alignment charge consumed per echo attack.")
                    .defineInRange("chargeCostPerAttack", 100, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
