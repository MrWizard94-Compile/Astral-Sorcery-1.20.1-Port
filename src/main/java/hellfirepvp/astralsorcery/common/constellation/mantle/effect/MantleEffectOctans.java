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
 * Mantle effect for Octans (Weak — Water).
 * Full implementation deferred until event hooks are ported.
 * Effects: infinite water breathing, swimming speed, underwater vision.
 */
public class MantleEffectOctans extends MantleEffect {

    public static final OctansConfig CONFIG = new OctansConfig();

    public MantleEffectOctans() {
        super(ConstellationsAS.OCTANS);
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

    public static class OctansConfig extends Config {

        public ForgeConfigSpec.DoubleValue swimSpeedMultiplier;

        public OctansConfig() {
            super("octans");
        }
    }
}
