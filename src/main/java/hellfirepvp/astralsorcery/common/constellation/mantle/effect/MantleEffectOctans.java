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
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Octans (Weak — Water).
 *
 * <ul>
 *   <li>Server tick: heals {@code healPerTick} health and restores air while eyes are in water.</li>
 *   <li>Cancels knockback while the wearer's eyes are in water.</li>
 *   <li>Negates the underwater mining speed penalty when wearing Octans and the player has
 *       enough charge (multiplies break speed by 5, matching Aqua Affinity behaviour).</li>
 * </ul>
 *
 * <p>1.16 → 1.20: areEyesInFluid(FluidTags.WATER) → isEyeInFluidType(ForgeMod.WATER_TYPE.get()),
 * player.setAir/getMaxAir unchanged, EnchantmentHelper.hasAquaAffinity → inline check,
 * inventory slot swap approach simplified to direct speed multiply,
 * BlockEvent package → net.minecraftforge.event.level.</p>
 */
public class MantleEffectOctans extends MantleEffect {

    public static final OctansConfig CONFIG = new OctansConfig();

    public MantleEffectOctans() {
        super(ConstellationsAS.OCTANS);
    }

    /**
     * Called by {@code MixinLivingEntity} to check if the water movement slowdown
     * should be negated for this entity (wearing an Octans mantle).
     */
    public static boolean shouldPreventWaterSlowdown(@Nonnull LivingEntity entity) {
        return ItemMantle.getEffect(entity, ConstellationsAS.OCTANS) != null;
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(this::handleUnderwaterBreakSpeed);
        bus.addListener(this::handleUnderwaterUnwavering);
    }

    @Override
    protected void tickServer(@Nonnull Player player) {
        if (player.isEyeInFluidType(ForgeMod.WATER_TYPE.get())) {
            if (player.getAirSupply() < (player.getMaxAirSupply() - 20)) {
                player.setAirSupply(player.getMaxAirSupply());
            }
            player.heal(CONFIG.healPerTick.get().floatValue());
        }
    }

    private void handleUnderwaterBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.isEyeInFluidType(ForgeMod.WATER_TYPE.get())) return;
        if (EnchantmentHelper.hasAquaAffinity(player)) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.OCTANS) == null) return;

        LogicalSide side = player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
        float cost = CONFIG.chargeCostPerBreakSpeed.get().floatValue();
        if (AlignmentChargeHandler.INSTANCE.hasCharge(player, side, cost)) {
            // Aqua Affinity negates the 1/5 underwater speed penalty — replicate by multiplying by 5
            event.setNewSpeed(event.getNewSpeed() * 5.0f);
            AlignmentChargeHandler.INSTANCE.drainCharge(player, side, cost, false);
        }
    }

    private void handleUnderwaterUnwavering(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isEyeInFluidType(ForgeMod.WATER_TYPE.get())) return;
        if (ItemMantle.getEffect(entity, ConstellationsAS.OCTANS) != null) {
            event.setCanceled(true);
        }
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class OctansConfig extends Config {

        public ForgeConfigSpec.DoubleValue healPerTick;
        public ForgeConfigSpec.IntValue chargeCostPerBreakSpeed;

        public OctansConfig() {
            super("octans");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.octans");
            healPerTick = b
                    .comment("Health healed per tick while the wearer's eyes are in water.")
                    .defineInRange("healPerTick", 0.01, 0.0, 5.0);
            chargeCostPerBreakSpeed = b
                    .comment("Alignment charge consumed per underwater block break speed boost.")
                    .defineInRange("chargeCostPerBreakSpeed", 30, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
