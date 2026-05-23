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
 * Mantle effect for Evorsio (Major — Mining).
 * On each block break, drains up to {@code chargeCostPerBreak} alignment charge.
 *
 * <p>1.16 → 1.20: BlockEvent package moved to net.minecraftforge.event.level,
 * player.getEntityWorld().isRemote() → !player.level().isClientSide().</p>
 */
public class MantleEffectEvorsio extends MantleEffect {

    public static final EvorsioConfig CONFIG = new EvorsioConfig();

    public MantleEffectEvorsio() {
        super(ConstellationsAS.EVORSIO);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(EventPriority.LOWEST, this::onBreak);
    }

    private void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        if (ItemMantle.getEffect(player, ConstellationsAS.EVORSIO) == null) return;

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

    public static class EvorsioConfig extends Config {

        public ForgeConfigSpec.IntValue chargeCostPerBreak;

        public EvorsioConfig() {
            super("evorsio");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.evorsio");
            chargeCostPerBreak = b
                    .comment("Alignment charge consumed per block broken while wearing the Evorsio mantle.")
                    .defineInRange("chargeCostPerBreak", 2, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
