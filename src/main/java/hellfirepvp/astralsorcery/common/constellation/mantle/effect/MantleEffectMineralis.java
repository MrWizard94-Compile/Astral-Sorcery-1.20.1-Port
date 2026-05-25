/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Mantle effect for Mineralis (Weak — Ore).
 * On each block break, drains up to {@code chargeCostPerBreak} alignment charge.
 * Client tick highlights the block type held in hand with orange Mineralis sparkles.
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
    @OnlyIn(Dist.CLIENT)
    protected boolean usesClientTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(@Nonnull Player player) {
        if (rand.nextBoolean()) {
            playBlockHighlight(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playBlockHighlight(@Nonnull Player player) {
        BlockState state = null;
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off  = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!main.isEmpty()) state = ItemUtils.createBlockState(main);
        if (!off.isEmpty())  state = ItemUtils.createBlockState(off);
        if (state == null || state.getBlock() instanceof AirBlock) return;

        BlockState fState = state;
        List<BlockPos> positions = BlockDiscoverer.searchForBlocksAround(
                player.level(), player.blockPosition(), CONFIG.highlightRange.get(),
                (level, pos, s) -> s == fState);
        if (positions.isEmpty()) return;

        int index = positions.size() > 10 ? rand.nextInt(positions.size()) : rand.nextInt(10);
        if (index >= positions.size()) return;

        BlockPos at = positions.get(index);
        Vec3 center = new Vec3(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5);
        EffectHelper.sparkleFloating(center, ColorsAS.CONSTELLATION_MINERALIS,
                0.3F + rand.nextFloat() * 0.3F, 20 + rand.nextInt(15));
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
                    .comment("Radius in blocks for ore/block highlighting via client VFX.")
                    .defineInRange("highlightRange", 10, 0, 32);
            chargeCostPerBreak = b
                    .comment("Alignment charge consumed per block broken while wearing the Mineralis mantle.")
                    .defineInRange("chargeCostPerBreak", 2, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
