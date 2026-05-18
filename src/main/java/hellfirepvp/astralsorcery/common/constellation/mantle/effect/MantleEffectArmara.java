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
 * Mantle effect for Armara (Major — Protection).
 * Full implementation deferred until AlignmentChargeHandler and event hooks are ported.
 * Effects: damage reduction, shield absorption on high-damage hits.
 */
public class MantleEffectArmara extends MantleEffect {

    public static final ArmaraConfig CONFIG = new ArmaraConfig();

    public MantleEffectArmara() {
        super(ConstellationsAS.ARMARA);
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

    public static class ArmaraConfig extends Config {

        public ForgeConfigSpec.DoubleValue chargeCostPerAbsorption;
        public ForgeConfigSpec.DoubleValue maxAbsorptionPercent;

        public ArmaraConfig() {
            super("armara");
        }
    }
}
