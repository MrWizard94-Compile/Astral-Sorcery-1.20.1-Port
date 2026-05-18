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
 * Mantle effect for Fornax (Weak — Fire).
 * Full implementation deferred until event hooks are ported.
 * Effects: fire immunity, sets attackers on fire, lava swimming.
 */
public class MantleEffectFornax extends MantleEffect {

    public static final FornaxConfig CONFIG = new FornaxConfig();

    public MantleEffectFornax() {
        super(ConstellationsAS.FORNAX);
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

    public static class FornaxConfig extends Config {

        public ForgeConfigSpec.IntValue burnDurationOnAttacker;
        public ForgeConfigSpec.DoubleValue chargeCostPerBurn;

        public FornaxConfig() {
            super("fornax");
        }
    }
}
