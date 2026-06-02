package hellfirepvp.astralsorcery.common.item.tool;

import hellfirepvp.astralsorcery.common.lib.TagsAS;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Infused Crystal Pickaxe — enhanced version of the Crystal Pickaxe.
 * Special ability: right-click to scan for ores within 16 blocks.
 * Found ores are highlighted with END_ROD particles visible for ~3s.
 * 120-tick cooldown between scans.
 */
public class ItemInfusedCrystalPickaxe extends ItemCrystalPickaxe {

    private static final int SCAN_RADIUS = 16;
    private static final int COOLDOWN_TICKS = 120;

    public ItemInfusedCrystalPickaxe() {
        super();
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && !serverPlayer.getCooldowns().isOnCooldown(this)) {

            List<BlockPos> ores = BlockDiscoverer.searchForBlocksAround(
                    level, player.blockPosition(), SCAN_RADIUS,
                    BlockPredicates.isInTag(TagsAS.Blocks.ORES));

            if (!ores.isEmpty()) {
                ServerLevel serverLevel = (ServerLevel) level;
                for (BlockPos ore : ores) {
                    serverLevel.sendParticles(serverPlayer,
                            ParticleTypes.END_ROD,
                            true,
                            ore.getX() + 0.5, ore.getY() + 0.5, ore.getZ() + 0.5,
                            8, 0.3, 0.3, 0.3, 0.02);
                }
                serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }
}
