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
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Aevitas (Major — Life).
 *
 * <ul>
 *   <li>Server tick: 1-in-N chance per tick to heal {@code healthPerCycle} HP.</li>
 *   <li>Server tick: 1-in-N chance per tick to restore food/saturation when hungry,
 *       consuming alignment charge.</li>
 * </ul>
 *
 * <p>1.16 → 1.20: FoodStats → FoodData, addStats(food, sat) → eat(food, sat),
 * PlayerWalkableAir collision handler deferred (CollisionHelper not yet ported).</p>
 */
public class MantleEffectAevitas extends MantleEffect {

    public static final AevitasConfig CONFIG = new AevitasConfig();

    public MantleEffectAevitas() {
        super(ConstellationsAS.AEVITAS);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {}

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
    }

    @Override
    public Config getConfig() {
        return CONFIG;
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
                    .comment("Charge consumed per tick while floating above air (deferred).")
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
