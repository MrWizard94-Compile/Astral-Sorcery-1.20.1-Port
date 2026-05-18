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
 * Mantle effect for Bootes (Weak — Animals).
 * Full implementation deferred until event hooks are ported.
 * Effects: animals are passive, animal loot increased, taming bonuses.
 */
public class MantleEffectBootes extends MantleEffect {

    public static final BootesConfig CONFIG = new BootesConfig();

    public MantleEffectBootes() {
        super(ConstellationsAS.BOOTES);
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

    public static class BootesConfig extends Config {

        public ForgeConfigSpec.DoubleValue lootMultiplier;

        public BootesConfig() {
            super("bootes");
        }
    }
}
