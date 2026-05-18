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
 * Mantle effect for Mineralis (Weak — Ore).
 * Full implementation deferred until event hooks are ported.
 * Effects: ore highlighting, fortune bonus, drop multiplier on ores.
 */
public class MantleEffectMineralis extends MantleEffect {

    public static final MineralisConfig CONFIG = new MineralisConfig();

    public MantleEffectMineralis() {
        super(ConstellationsAS.MINERALIS);
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

    public static class MineralisConfig extends Config {

        public ForgeConfigSpec.IntValue fortuneLevel;
        public ForgeConfigSpec.DoubleValue dropMultiplier;

        public MineralisConfig() {
            super("mineralis");
        }
    }
}
