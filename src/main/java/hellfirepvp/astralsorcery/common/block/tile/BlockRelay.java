package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.item.ItemGlassLens;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRelay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Spectral Relay — collects additional starlight for nearby altars.
 * Part of altar multiblock structures. Placed on relay pedestals.
 *
 * <p>1.16 -> 1.20 changes: Same as other tile blocks.</p>
 */
public class BlockRelay extends BlockEntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 2, 13),  // Base
            Block.box(5, 2, 5, 11, 8, 11)   // Crystal holder
    );

    public BlockRelay() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(2.0F, 10.0F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .lightLevel(state -> 5));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityRelay(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.RELAY.get());
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                 @Nonnull Player player, @Nonnull InteractionHand hand,
                                 @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockEntityRelay relay)) return InteractionResult.PASS;

        ItemStack inHand = player.getItemInHand(hand);
        ItemStack inSlot = relay.getHeldItem();

        if (player.isShiftKeyDown()) {
            if (!inSlot.isEmpty()) {
                relay.setHeldItem(ItemStack.EMPTY);
                if (!player.addItem(inSlot)) Block.popResource(level, pos, inSlot);
                return InteractionResult.CONSUME;
            }
        } else if (inHand.getItem() instanceof ItemGlassLens && inSlot.isEmpty()) {
            relay.setHeldItem(inHand.copyWithCount(1));
            if (!player.isCreative()) inHand.shrink(1);
            return InteractionResult.CONSUME;
        } else if (inHand.isEmpty() && !inSlot.isEmpty()) {
            relay.setHeldItem(ItemStack.EMPTY);
            player.setItemInHand(hand, inSlot);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                         @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityRelay relay) {
                ItemStack held = relay.getHeldItem();
                if (!held.isEmpty()) Block.popResource(level, pos, held);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
