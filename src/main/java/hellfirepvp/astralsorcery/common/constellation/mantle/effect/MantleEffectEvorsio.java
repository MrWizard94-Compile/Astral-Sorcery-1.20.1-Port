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
 * Mantle effect for Evorsio (Major — Mining).
 * Full implementation deferred until event hooks are ported.
 * Effects: mining speed bonus, vein mining, tool durability conservation.
 */
public class MantleEffectEvorsio extends MantleEffect {

    public static final EvorsioConfig CONFIG = new EvorsioConfig();

    public MantleEffectEvorsio() {
        super(ConstellationsAS.EVORSIO);
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

    public static class EvorsioConfig extends Config {

        public ForgeConfigSpec.DoubleValue miningSpeedBonus;
        public ForgeConfigSpec.IntValue veinMiningRadius;

        public EvorsioConfig() {
            super("evorsio");
        }
    }
}
