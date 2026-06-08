package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.auxiliary.link.IItemLinkingTool;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;

/**
 * Linking Tool — a dedicated item for creating links between starlight network nodes.
 * Separate from the wand, this tool focuses exclusively on the linking interaction.
 * Implements {@link IItemLinkingTool} to participate in the linking system.
 *
 * <p>1.16 -> 1.20 changes:
 * ActionResultType -> InteractionResult,
 * PlayerEntity -> Player,
 * World -> Level,
 * Hand -> InteractionHand</p>
 */
public class ItemLinkingTool extends ItemAS implements IItemLinkingTool {

    public ItemLinkingTool() {
        super(defaultProperties().stacksTo(1));
    }

    /**
     * Client-side: return sidedSuccess to prevent block GUIs (chests, machines, etc.)
     * from opening while holding the linking tool.
     * Server-side: delegates directly to {@link LinkHandler} so linking works
     * even if EventHandlerInteract's class-based registration has issues.
     */
    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        BlockHitResult hit = new BlockHitResult(
                ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), false);
        return LinkHandler.handleInteraction(
                level, player, ctx.getHand(), ctx.getItemInHand(), hit);
    }

    @Override
    @Nonnull
    public InteractionResult doInteract(@Nonnull Level level,
                                        @Nonnull Player player,
                                        @Nonnull InteractionHand hand,
                                        @Nonnull ItemStack stack,
                                        @Nonnull BlockHitResult hit) {
        return LinkHandler.handleInteraction(level, player, hand, stack, hit);
    }
}
