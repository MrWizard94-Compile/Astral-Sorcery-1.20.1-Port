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
 * Mantle effect for Vicio (Major — Movement).
 * Full implementation deferred until AlignmentChargeHandler is ported.
 * Effects: flight, increased movement speed, reduced fall damage.
 */
public class MantleEffectVicio extends MantleEffect {

    public static final VicioConfig CONFIG = new VicioConfig();

    public MantleEffectVicio() {
        super(ConstellationsAS.VICIO);
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

    public static class VicioConfig extends Config {

        public ForgeConfigSpec.DoubleValue speedMultiplier;
        public ForgeConfigSpec.DoubleValue chargeCostPerFlightTick;

        public VicioConfig() {
            super("vicio");
        }
    }
}
