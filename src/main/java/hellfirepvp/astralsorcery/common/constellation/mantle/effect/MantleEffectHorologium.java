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
 * Mantle effect for Horologium (Weak — Time).
 * Full implementation deferred until AlignmentChargeHandler is ported.
 * Effects: slowness aura on nearby mobs, time freeze burst on high damage taken.
 */
public class MantleEffectHorologium extends MantleEffect {

    public static final HorologiumConfig CONFIG = new HorologiumConfig();

    public MantleEffectHorologium() {
        super(ConstellationsAS.HOROLOGIUM);
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

    public static class HorologiumConfig extends Config {

        public ForgeConfigSpec.DoubleValue slownessDuration;
        public ForgeConfigSpec.DoubleValue chargeCostPerFreeze;

        public HorologiumConfig() {
            super("horologium");
        }
    }
}
