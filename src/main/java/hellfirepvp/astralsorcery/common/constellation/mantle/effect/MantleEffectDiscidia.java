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
 * Mantle effect for Discidia (Major — Combat).
 * Full implementation deferred until AlignmentChargeHandler and event hooks are ported.
 * Effects: damage boost, attacker retaliation, charge drain on hits taken.
 */
public class MantleEffectDiscidia extends MantleEffect {

    public static final DiscidiaConfig CONFIG = new DiscidiaConfig();

    public MantleEffectDiscidia() {
        super(ConstellationsAS.DISCIDIA);
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

    public static class DiscidiaConfig extends Config {

        public ForgeConfigSpec.DoubleValue damageBoostPercent;
        public ForgeConfigSpec.DoubleValue chargeCostPerHit;
        public ForgeConfigSpec.DoubleValue retaliationDamage;

        public DiscidiaConfig() {
            super("discidia");
        }
    }
}
