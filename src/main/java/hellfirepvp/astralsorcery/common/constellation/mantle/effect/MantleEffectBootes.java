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
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.PlayerUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Bootes (Weak — Animals/Flares).
 *
 * <p>When the wearer is hurt by a living entity, a constellation flare is launched
 * at the attacker. When the wearer attacks a living entity, a flare is launched at
 * the target. Each launch costs alignment charge.</p>
 *
 * <p>1.16 → 1.20 adaptation: The 1.16 EntityFlare had AI follower behavior
 * (setFollowingTarget / setAttackTarget). The port's EntityFlare is a ThrowableProjectile
 * with no AI. The "idle follower" tick behavior and entity-ID tracking are removed.
 * Flares are now launched directly at the target on the triggering event.
 * maxFlareCount is retained in config for when AI following is re-implemented.</p>
 *
 * <p>1.16 → 1.20 API changes:
 * event.getEntityLiving() → event.getEntity(),
 * src.getTrueSource() → event.getSource().getEntity(),
 * world.addEntity() → level.addFreshEntity(),
 * player.getEntityWorld() → player.level().</p>
 */
public class MantleEffectBootes extends MantleEffect {

    public static final BootesConfig CONFIG = new BootesConfig();

    public MantleEffectBootes() {
        super(ConstellationsAS.BOOTES);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(EventPriority.LOW, this::onHurt);
        bus.addListener(EventPriority.LOW, this::onAttacked);
    }

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    private void onHurt(LivingHurtEvent event) {
        LivingEntity hurt = event.getEntity();
        if (hurt.level().isClientSide()) return;
        if (!(hurt instanceof Player player)) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.BOOTES) == null) return;
        Entity src = event.getSource().getEntity();
        if (src instanceof LivingEntity attacker && attacker.isAlive()) {
            launchFlareAt(player, attacker);
        }
    }

    private void onAttacked(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntity();
        if (attacked.level().isClientSide()) return;
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player)) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.BOOTES) == null) return;
        if (!attacked.isAlive()) return;
        if (attacked instanceof Player playerTarget && !PlayerUtils.canPlayerAttackServer(player, playerTarget)) return;
        launchFlareAt(player, attacked);
    }

    private void launchFlareAt(Player shooter, LivingEntity target) {
        if (!AlignmentChargeHandler.INSTANCE.hasCharge(shooter, LogicalSide.SERVER, CONFIG.chargeCostPerFlare.get())) return;
        Level level = shooter.level();
        EntityFlare flare = new EntityFlare(EntityTypesAS.FLARE.get(), level, shooter,
                ConstellationsAS.BOOTES.getRegistryName(), 4.0f);
        double dx = target.getX() - shooter.getX();
        double dy = target.getEyeY() - shooter.getEyeY();
        double dz = target.getZ() - shooter.getZ();
        flare.shoot(dx, dy, dz, 1.5f, 1.0f);
        if (level.addFreshEntity(flare)) {
            AlignmentChargeHandler.INSTANCE.drainCharge(shooter, LogicalSide.SERVER, CONFIG.chargeCostPerFlare.get(), false);
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class BootesConfig extends Config {

        public ForgeConfigSpec.IntValue maxFlareCount;
        public ForgeConfigSpec.IntValue chargeCostPerFlare;

        public BootesConfig() {
            super("bootes");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.bootes");
            maxFlareCount = b
                    .comment("Maximum simultaneous flares (reserved for when AI following is re-implemented).")
                    .defineInRange("maxFlareCount", 3, 0, 6);
            chargeCostPerFlare = b
                    .comment("Alignment charge consumed per launched flare.")
                    .defineInRange("chargeCostPerFlare", 400, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
