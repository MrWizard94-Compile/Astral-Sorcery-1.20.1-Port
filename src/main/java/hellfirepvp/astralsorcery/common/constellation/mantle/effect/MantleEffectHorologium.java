/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.auxiliary.TimeStopController;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Mantle effect for Horologium (Weak — Time).
 *
 * <p>When the wearer takes non-fire damage and has alignment charge, applies
 * a heavy {@link MobEffects#MOVEMENT_SLOWDOWN Slowness} to all nearby entities
 * within {@code effectRange} blocks for {@code effectDuration} ticks.
 * A cooldown prevents re-triggering during that window.</p>
 *
 *
 * <p>1.16 → 1.20: player.getCooldownTracker() → player.getCooldowns(),
 * hasCooldown/setCooldown → isOnCooldown/addCooldown,
 * DamageSource.isFireDamage() → source.is(DamageTypeTags.IS_FIRE),
 * getEntitiesWithinAABB → getEntitiesOfClass,
 * getPosition() → blockPosition().</p>
 */
public class MantleEffectHorologium extends MantleEffect {

    public static final HorologiumConfig CONFIG = new HorologiumConfig();

    public MantleEffectHorologium() {
        super(ConstellationsAS.HOROLOGIUM);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(this::onHurt);
    }

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    private void onHurt(LivingHurtEvent event) {
        LivingEntity hurt = event.getEntity();
        if (hurt.level().isClientSide()) return;
        if (!(hurt instanceof Player player)) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.HOROLOGIUM) == null) return;
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) return;

        ItemStack mantleStack = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (player.getCooldowns().isOnCooldown(mantleStack.getItem())) return;

        if (!AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerFreeze.get())) return;

        Level level = player.level();
        BlockPos center = player.blockPosition();
        float range = CONFIG.effectRange.get().floatValue();
        int duration = CONFIG.effectDuration.get();

        AABB box = new AABB(center).inflate(range);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive());
        TimeStopController.INSTANCE.freezeEntities(nearby, duration);

        player.getCooldowns().addCooldown(mantleStack.getItem(), CONFIG.cooldown.get());
        AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerFreeze.get(), false);
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class HorologiumConfig extends Config {

        public ForgeConfigSpec.DoubleValue effectRange;
        public ForgeConfigSpec.IntValue effectDuration;
        public ForgeConfigSpec.IntValue cooldown;
        public ForgeConfigSpec.IntValue chargeCostPerFreeze;

        public HorologiumConfig() {
            super("horologium");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.horologium");
            effectRange = b
                    .comment("Radius of the slowness/time-stop bubble in blocks.")
                    .defineInRange("effectRange", 20.0, 4.0, 64.0);
            effectDuration = b
                    .comment("Duration of the slowness effect in ticks.")
                    .defineInRange("effectDuration", 180, 40, 1000);
            cooldown = b
                    .comment("Cooldown in ticks before the effect can trigger again.")
                    .defineInRange("cooldown", 1000, 40, 20_000);
            chargeCostPerFreeze = b
                    .comment("Alignment charge consumed per trigger.")
                    .defineInRange("chargeCostPerFreeze", 400, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
