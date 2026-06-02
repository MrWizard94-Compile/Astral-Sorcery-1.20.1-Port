package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.block.base.LiquidStarlightOwned;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityInfuser;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Starlight Infuser — used for liquid infusion recipes.
 * Requires a multiblock structure and liquid starlight to operate.
 *
 * <p>1.16 -> 1.20 changes: Same as other tile blocks.</p>
 */
public class BlockInfuser extends BlockEntityBlock implements LiquidStarlightOwned {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),  // Base platform
            Block.box(3, 4, 3, 13, 6, 13)   // Basin
    );

    public BlockInfuser() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(3.5F, 18.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    public int getMaxLiquidStarlight() {
        return 4000; // 4 buckets
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
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
        if (!(be instanceof BlockEntityInfuser infuser)) {
            return InteractionResult.PASS;
        }
        TileInventory inventory = infuser.getInventory();
        ItemStack stored = inventory.getStackInSlot(0);
        ItemStack heldItem = player.getItemInHand(hand);

        if (!heldItem.isEmpty()) {
            // Eject current input first if occupied
            if (!stored.isEmpty()) {
                if (!player.addItem(stored)) {
                    Block.popResource(level, pos, stored);
                }
                inventory.setStackInSlot(0, ItemStack.EMPTY);
            }
            // Place held item only if sky above is clear
            if (!level.isEmptyBlock(pos.above())) {
                return InteractionResult.PASS;
            }
            inventory.setStackInSlot(0, heldItem.copyWithCount(1));
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                    0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            if (!player.isCreative()) {
                heldItem.shrink(1);
            }
            infuser.markForUpdate();
        } else if (!stored.isEmpty()) {
            // Empty-handed: eject stored input
            if (!player.addItem(stored)) {
                Block.popResource(level, pos, stored);
            }
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            infuser.markForUpdate();
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level,
                         @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityInfuser infuser) {
                for (int i = 0; i < infuser.getInventory().getSlots(); i++) {
                    ItemStack stack = infuser.getInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) Block.popResource(level, pos, stack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityInfuser(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.INFUSER.get());
    }
}
