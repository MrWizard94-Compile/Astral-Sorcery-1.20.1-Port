/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Lucerna (Weak — Light).
 *
 * <p>All gameplay effects (entity highlighting, spawner/chest detection) are
 * client-side VFX and are deferred to Phase 12. There is no server-side logic.</p>
 *
 * <p>Config values are retained so they can be referenced by client-side rendering
 * when that is ported.</p>
 */
public class MantleEffectLucerna extends MantleEffect {

    public static final LucernaConfig CONFIG = new LucernaConfig();

    public MantleEffectLucerna() {
        super(ConstellationsAS.LUCERNA);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {}

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class LucernaConfig extends Config {

        public ForgeConfigSpec.IntValue range;
        public ForgeConfigSpec.BooleanValue findSpawners;
        public ForgeConfigSpec.BooleanValue findChests;

        public LucernaConfig() {
            super("lucerna");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.lucerna");
            range = b
                    .comment("Maximum range in blocks for entity/block highlighting (client VFX, Phase 12).")
                    .defineInRange("range", 48, 0, 512);
            findSpawners = b
                    .comment("Whether spawners in range should be highlighted.")
                    .define("findSpawners", true);
            findChests = b
                    .comment("Whether containers in range should be highlighted.")
                    .define("findChests", true);
            b.pop();
            b.build();
        }
    }
}
