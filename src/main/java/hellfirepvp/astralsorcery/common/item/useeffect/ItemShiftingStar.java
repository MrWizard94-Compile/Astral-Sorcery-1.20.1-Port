package hellfirepvp.astralsorcery.common.item.useeffect;

import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Shifting Star — changes the player's attuned constellation (or clears it).
 * Subclasses override getBaseConstellation() for constellation-specific variants.
 *
 * <p>1.16 → 1.20: PlayerEntity → Player, ServerPlayerEntity → ServerPlayer,
 * ItemUseContext → InteractionHand, World → Level.</p>
 */
public class ItemShiftingStar extends ItemAS {

    public ItemShiftingStar() {
        super(defaultProperties().stacksTo(1));
    }

    @Nullable
    public IMajorConstellation getBaseConstellation() {
        return null;
    }

    @Nonnull
    @Override
    @SuppressWarnings("null")
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PlayerProgress progress = PlayerProgressManager.getProgress(serverPlayer);
            if (progress == null || !progress.wasOnceAttuned()) {
                return InteractionResultHolder.fail(stack);
            }

            IMajorConstellation cst = getBaseConstellation();
            if (cst != null) {
                if (!progress.hasDiscovered(cst.getRegistryName())) {
                    return InteractionResultHolder.fail(stack);
                }
                ResearchManager.attuneTo(serverPlayer, cst.getRegistryName());
            } else {
                // Clear attunement
                progress.setAttunedConstellation(null);
                PlayerProgressManager.syncProgress(serverPlayer);
            }

            level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
