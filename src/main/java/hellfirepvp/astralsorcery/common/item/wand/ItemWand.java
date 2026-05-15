package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.auxiliary.link.IItemLinkingTool;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;

/**
 * Resonating Wand / Illumination Wand — the mod's primary interaction tool.
 * Used for linking tile entities, activating structures, and various interactions.
 * Implements IItemLinkingTool for the starlight linking system.
 *
 * <p>1.16 -> 1.20 changes:
 * ActionResultType -> InteractionResult,
 * PlayerEntity -> Player,
 * World -> Level,
 * Hand -> InteractionHand</p>
 */
public class ItemWand extends ItemAS implements IItemLinkingTool {

    public ItemWand() {
        super(defaultProperties().stacksTo(1));
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
