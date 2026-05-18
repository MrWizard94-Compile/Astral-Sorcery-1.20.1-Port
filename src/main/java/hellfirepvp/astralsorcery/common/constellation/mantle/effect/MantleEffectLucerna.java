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
 * Mantle effect for Lucerna (Weak — Light).
 * Full implementation deferred until event hooks are ported.
 * Effects: night vision, mob spawning prevention in a radius.
 */
public class MantleEffectLucerna extends MantleEffect {

    public static final LucernaConfig CONFIG = new LucernaConfig();

    public MantleEffectLucerna() {
        super(ConstellationsAS.LUCERNA);
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

    public static class LucernaConfig extends Config {

        public ForgeConfigSpec.IntValue spawnDenialRadius;

        public LucernaConfig() {
            super("lucerna");
        }
    }
}
