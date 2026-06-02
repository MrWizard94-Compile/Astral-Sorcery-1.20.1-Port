package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Infused Crystal Shovel — enhanced version of the Crystal Shovel.
 * Special ability: breaking a dirt/gravel/sand block while not sneaking
 * mines all connected same-state blocks within 8 blocks (up to 200).
 * 120-tick cooldown.
 */
public class ItemInfusedCrystalShovel extends ItemCrystalShovel {

    private static final int COOLDOWN_TICKS = 120;
    private static final ThreadLocal<Boolean> CHAIN_BREAKING = ThreadLocal.withInitial(() -> false);

    public ItemInfusedCrystalShovel() {
        super();
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (CHAIN_BREAKING.get()) return super.onBlockStartBreak(stack, pos, player);
        if (player.isShiftKeyDown()) return super.onBlockStartBreak(stack, pos, player);
        if (!(player instanceof ServerPlayer serverPlayer)) return super.onBlockStartBreak(stack, pos, player);
        if (serverPlayer.getCooldowns().isOnCooldown(this)) return super.onBlockStartBreak(stack, pos, player);
        if (serverPlayer.level().getBlockState(pos).isAir()) return super.onBlockStartBreak(stack, pos, player);

        List<BlockPos> blocks = BlockDiscoverer.discoverBlocksWithSameStateAround(
                serverPlayer.level(), pos, true, 8, 200, false);

        if (blocks.size() <= 1) return super.onBlockStartBreak(stack, pos, player);

        CHAIN_BREAKING.set(true);
        try {
            for (BlockPos at : blocks) {
                if (at.equals(pos)) continue;
                serverPlayer.gameMode.destroyBlock(at);
            }
        } finally {
            CHAIN_BREAKING.set(false);
        }
        serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return super.onBlockStartBreak(stack, pos, player);
    }
}
