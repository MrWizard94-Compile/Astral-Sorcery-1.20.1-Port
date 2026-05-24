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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Aevitas (Major — Life).
 *
 * <ul>
 *   <li>Server tick: heal and feed player at configurable rates.</li>
 *   <li>Walkable air: prevent fall when player has the effect and sufficient charge.
 *       Implemented via {@code LivingUpdateEvent} (replaces the 1.16 VoxelShapeSpliterator
 *       mixin — no mixin required in 1.20 Forge).</li>
 * </ul>
 *
 * <p>1.16 → 1.20: FoodStats → FoodData, addStats → eat,
 * VoxelShapeSpliterator mixin replaced by LivingUpdateEvent handler,
 * PlayerWalkableAir now uses {@code @SubscribeEvent} on the Forge bus.</p>
 */
public class MantleEffectAevitas extends MantleEffect {

    public static final AevitasConfig CONFIG = new AevitasConfig();

    public MantleEffectAevitas() {
        super(ConstellationsAS.AEVITAS);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.register(new PlayerWalkableAir());
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    protected void tickServer(@Nonnull Player player) {
        int healChance = CONFIG.healChance.get();
        int foodChance = CONFIG.feedChance.get();

        if (healChance > 0 && rand.nextInt(healChance) == 0) {
            player.heal(CONFIG.healthPerCycle.get().floatValue());
        }
        if (foodChance > 0 && rand.nextInt(foodChance) == 0) {
            FoodData food = player.getFoodData();
            if (food.getFoodLevel() < 20 || food.getSaturationLevel() < 5F) {
                if (AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerFood.get())) {
                    food.eat(CONFIG.foodPerCycle.get().intValue(), 0.5F);
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerFood.get(), false);
                }
            }
        }

        if (isFloatingOnAir(player)) {
            AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER,
                    CONFIG.chargeCostPerTravelTick.get().floatValue(), false);
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    /**
     * Returns true if the player is wearing an Aevitas mantle and has enough charge to float.
     */
    public static boolean canSupportEffect(@Nonnull Player player) {
        LogicalSide side = player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
        return ItemMantle.getEffect(player, ConstellationsAS.AEVITAS) != null
                && AlignmentChargeHandler.INSTANCE.hasCharge(player, side,
                        (float) (double) CONFIG.chargeCostPerTravelTick.get());
    }

    /**
     * Returns true if the player is currently being held up by the walkable-air effect:
     * on ground but no solid block beneath their feet.
     */
    public static boolean isFloatingOnAir(@Nonnull Player player) {
        if (!player.isOnGround()) return false;
        BlockPos below = player.blockPosition().below();
        return player.level().getBlockState(below).isAir();
    }

    /**
     * Prevents the player from falling through air by zeroing downward velocity
     * and marking them as on-ground each tick. Fires on the Forge event bus.
     *
     * <p>In 1.16 this was done by injecting a phantom collision box via a mixin
     * into VoxelShapeSpliterator. That class no longer exists in 1.20; intercepting
     * LivingUpdateEvent before physics runs achieves the same result without any mixin.</p>
     */
    public static class PlayerWalkableAir {

        @SubscribeEvent
        public void onLivingUpdate(@Nonnull LivingUpdateEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide()) return;
            if (player.getAbilities().flying) return;
            if (!canSupportEffect(player)) return;

            Vec3 motion = player.getDeltaMovement();
            if (motion.y >= 0) return;

            // Allow crouching to descend one block (mirrors 1.16 yOffset=2 when crouching)
            if (player.getPose() == Pose.CROUCHING && isFloatingOnAir(player)) return;

            BlockPos below = player.blockPosition().below();
            if (!player.level().getBlockState(below).isAir()) return;

            player.setDeltaMovement(motion.x, 0.0, motion.z);
            player.setOnGround(true);
        }
    }

    public static class AevitasConfig extends Config {

        public ForgeConfigSpec.IntValue healChance;
        public ForgeConfigSpec.IntValue feedChance;
        public ForgeConfigSpec.DoubleValue healthPerCycle;
        public ForgeConfigSpec.DoubleValue foodPerCycle;
        public ForgeConfigSpec.DoubleValue chargeCostPerTravelTick;
        public ForgeConfigSpec.IntValue chargeCostPerHeal;
        public ForgeConfigSpec.IntValue chargeCostPerFood;

        public AevitasConfig() {
            super("aevitas");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.aevitas");
            healChance = b
                    .comment("'1 in N' chance per tick to heal. 0 = disabled.")
                    .defineInRange("healChance", 80, 0, Integer.MAX_VALUE);
            feedChance = b
                    .comment("'1 in N' chance per tick to feed. 0 = disabled.")
                    .defineInRange("feedChance", 80, 0, Integer.MAX_VALUE);
            healthPerCycle = b
                    .comment("Health restored per heal cycle.")
                    .defineInRange("healthPerCycle", 0.5, 0.0, 100.0);
            foodPerCycle = b
                    .comment("Food levels restored per feed cycle.")
                    .defineInRange("foodPerCycle", 1.0, 0.0, 100.0);
            chargeCostPerTravelTick = b
                    .comment("Charge consumed per tick while floating above air.")
                    .defineInRange("chargeCostPerTravelTick", 2.5, 0.0, 100.0);
            chargeCostPerHeal = b
                    .comment("Charge consumed per heal cycle.")
                    .defineInRange("chargeCostPerHeal", 100, 0, 1000);
            chargeCostPerFood = b
                    .comment("Charge consumed per feed cycle.")
                    .defineInRange("chargeCostPerFood", 100, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
