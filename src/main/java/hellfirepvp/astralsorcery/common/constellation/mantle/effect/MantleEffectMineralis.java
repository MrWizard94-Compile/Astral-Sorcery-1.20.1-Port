/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Mineralis (Weak — Ore).
 * On each block break, drains up to {@code chargeCostPerBreak} alignment charge.
 * Client-side ore highlighting is Phase 12 VFX work.
 *
 * <p>1.16 → 1.20: BlockEvent package moved to net.minecraftforge.event.level.</p>
 */
public class MantleEffectMineralis extends MantleEffect {

    public static final MineralisConfig CONFIG = new MineralisConfig();

    public MantleEffectMineralis() {
        super(ConstellationsAS.MINERALIS);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(EventPriority.LOWEST, this::onBreak);
    }

    private void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.MINERALIS) == null) return;

        float available = AlignmentChargeHandler.INSTANCE.getCurrentCharge(player, LogicalSide.SERVER);
        float drain = Math.min(available, CONFIG.chargeCostPerBreak.get().floatValue());
        AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, drain, false);
    }

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class MineralisConfig extends Config {

        public ForgeConfigSpec.IntValue highlightRange;
        public ForgeConfigSpec.IntValue chargeCostPerBreak;

        public MineralisConfig() {
            super("mineralis");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.mineralis");
            highlightRange = b
                    .comment("Radius in blocks for client-side ore highlighting (Phase 12 VFX).")
                    .defineInRange("highlightRange", 10, 0, 32);
            chargeCostPerBreak = b
                    .comment("Alignment charge consumed per block broken while wearing the Mineralis mantle.")
                    .defineInRange("chargeCostPerBreak", 2, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
