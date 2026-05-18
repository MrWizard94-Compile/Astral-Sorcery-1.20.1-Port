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
 * Mantle effect for Pelotrio (Weak — Mob Loot).
 * Full implementation deferred until event hooks are ported.
 * Effects: increased mob loot, random mob effects on kill, XP bonus.
 */
public class MantleEffectPelotrio extends MantleEffect {

    public static final PelotrioConfig CONFIG = new PelotrioConfig();

    public MantleEffectPelotrio() {
        super(ConstellationsAS.PELOTRIO);
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

    public static class PelotrioConfig extends Config {

        public ForgeConfigSpec.DoubleValue lootMultiplier;
        public ForgeConfigSpec.IntValue xpBonus;

        public PelotrioConfig() {
            super("pelotrio");
        }
    }
}
