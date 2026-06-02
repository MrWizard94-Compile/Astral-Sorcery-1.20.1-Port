package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.util.block.TreeDiscoverer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Infused Crystal Axe — enhanced version of the Crystal Axe.
 * Special ability: breaking a log while not sneaking fells the entire
 * connected tree (up to 128 logs) at once. 120-tick cooldown.
 */
public class ItemInfusedCrystalAxe extends ItemCrystalAxe {

    private static final int COOLDOWN_TICKS = 120;
    private static final ThreadLocal<Boolean> CHAIN_BREAKING = ThreadLocal.withInitial(() -> false);

    public ItemInfusedCrystalAxe() {
        super();
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (CHAIN_BREAKING.get()) return super.onBlockStartBreak(stack, pos, player);
        if (player.isShiftKeyDown()) return super.onBlockStartBreak(stack, pos, player);
        if (!(player instanceof ServerPlayer serverPlayer)) return super.onBlockStartBreak(stack, pos, player);
        if (serverPlayer.getCooldowns().isOnCooldown(this)) return super.onBlockStartBreak(stack, pos, player);

        List<BlockPos> logs = TreeDiscoverer.findConnectedLogs(serverPlayer.level(), pos);
        if (logs.size() <= 1) return super.onBlockStartBreak(stack, pos, player);

        CHAIN_BREAKING.set(true);
        try {
            for (BlockPos logPos : logs) {
                if (logPos.equals(pos)) continue;
                serverPlayer.gameMode.destroyBlock(logPos);
            }
        } finally {
            CHAIN_BREAKING.set(false);
        }
        serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return super.onBlockStartBreak(stack, pos, player);
    }
}
