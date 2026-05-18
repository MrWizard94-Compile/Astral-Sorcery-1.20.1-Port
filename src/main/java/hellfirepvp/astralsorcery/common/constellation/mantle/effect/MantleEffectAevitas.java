/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Aevitas (Major — Life/Growth).
 * Full implementation deferred until AlignmentChargeHandler and ResearchHelper are ported.
 * Effects: passive healing, hunger replenishment, floating on air.
 */
public class MantleEffectAevitas extends MantleEffect {

    public static final AevitasConfig CONFIG = new AevitasConfig();

    public MantleEffectAevitas() {
        super(ConstellationsAS.AEVITAS);
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    protected void tickServer(@Nonnull Player player) {}

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
        public ForgeConfigSpec.IntValue chargeCostPerFood;
        public ForgeConfigSpec.IntValue chargeCostPerHeal;

        public AevitasConfig() {
            super("aevitas");
        }
    }
}
