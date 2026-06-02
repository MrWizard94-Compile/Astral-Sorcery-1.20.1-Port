package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRitualPedestal;
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
 * Ritual Pedestal — holds an attuned crystal to produce constellation effects.
 * The active effect depends on the crystal's attuned constellation.
 *
 * <p>1.16 -> 1.20 changes: Same as other tile blocks.</p>
 */
public class BlockRitualPedestal extends BlockEntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 3, 14),   // Base
            Block.box(5, 3, 5, 11, 10, 11),  // Pillar
            Block.box(3, 10, 3, 13, 12, 13)  // Top
    );

    public BlockRitualPedestal() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(3.0F, 15.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Nonnull
    @Override
    @SuppressWarnings("null")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level,
                                 @Nonnull BlockPos pos, @Nonnull Player player,
                                 @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockEntityRitualPedestal pedestal)) {
            return InteractionResult.PASS;
        }
        ItemStack heldItem = player.getItemInHand(hand);
        ItemStack inPedestal = pedestal.getHeldCrystal();

        if (player.isShiftKeyDown()) {
            // Sneak-click: always eject held crystal to player
            if (!inPedestal.isEmpty()) {
                pedestal.setHeldCrystal(ItemStack.EMPTY);
                if (heldItem.isEmpty()) {
                    player.setItemInHand(hand, inPedestal);
                } else if (!player.addItem(inPedestal)) {
                    Block.popResource(level, pos, inPedestal);
                }
            }
        } else {
            // Normal click: place held item, return old crystal
            if (!heldItem.isEmpty() && inPedestal.isEmpty()) {
                pedestal.setHeldCrystal(heldItem.copyWithCount(1));
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
            } else if (heldItem.isEmpty() && !inPedestal.isEmpty()) {
                pedestal.setHeldCrystal(ItemStack.EMPTY);
                player.setItemInHand(hand, inPedestal);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level,
                         @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityRitualPedestal pedestal) {
                ItemStack crystal = pedestal.getHeldCrystal();
                if (!crystal.isEmpty()) {
                    Block.popResource(level, pos, crystal);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityRitualPedestal(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.RITUAL_PEDESTAL.get());
    }
}
