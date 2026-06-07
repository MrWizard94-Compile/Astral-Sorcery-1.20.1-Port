package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityIlluminator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Illumination Powder — places invisible light sources.
 * Right-clicking on a block places an invisible light block above it.
 *
 * <p>1.16 -> 1.20 changes:
 * ItemUseContext -> UseOnContext,
 * ActionResultType -> InteractionResult,
 * onItemUse -> useOn</p>
 */
public class ItemIlluminationPowder extends ItemAS {

    public ItemIlluminationPowder() {
        super(defaultProperties());
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        if (level.isEmptyBlock(targetPos)) {
            level.setBlock(targetPos, BlocksAS.ILLUMINATOR.get().defaultBlockState(), Block.UPDATE_ALL);
            BlockEntity be = level.getBlockEntity(targetPos);
            if (be instanceof BlockEntityIlluminator illuminator) {
                illuminator.setPlayerPlaced(true);
            }
            context.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
