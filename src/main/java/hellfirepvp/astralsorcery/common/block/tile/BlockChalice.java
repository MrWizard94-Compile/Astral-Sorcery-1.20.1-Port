package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.block.base.LiquidStarlightOwned;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityChalice;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Chalice — stores and auto-distributes liquid starlight.
 * Can be linked to other chalices to share fluid.
 *
 * <p>1.16 -> 1.20 changes: Same as other tile blocks.</p>
 */
public class BlockChalice extends BlockEntityBlock implements LiquidStarlightOwned {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 2, 13),   // Base
            Block.box(6, 2, 6, 10, 6, 10),   // Stem
            Block.box(2, 6, 2, 14, 14, 14)   // Cup
    );

    public BlockChalice() {
        super(Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(2.5F, 10.0F)
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int getMaxLiquidStarlight() {
        return 24000; // 24 buckets
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level,
                                 @Nonnull BlockPos pos, @Nonnull Player player,
                                 @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockEntityChalice chalice)) {
            return InteractionResult.PASS;
        }
        ItemStack heldItem = player.getItemInHand(hand);
        if (!FluidUtil.getFluidHandler(heldItem).isPresent()) {
            return InteractionResult.PASS;
        }
        IFluidHandler chaliceHandler = chalice.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .resolve().orElse(null);
        if (chaliceHandler == null) {
            return InteractionResult.PASS;
        }

        FluidStack contained = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
        if (contained.isEmpty()) {
            // Fill container from chalice
            FluidActionResult far = FluidUtil.tryFillContainerAndStow(heldItem, chaliceHandler,
                    new InvWrapper(player.getInventory()), FluidType.BUCKET_VOLUME, player, true);
            if (far.isSuccess()) {
                player.setItemInHand(hand, far.getResult());
                chalice.markForUpdate();
                return InteractionResult.CONSUME;
            }
        } else {
            // Drain container into chalice
            FluidActionResult far = FluidUtil.tryEmptyContainerAndStow(heldItem, chaliceHandler,
                    new InvWrapper(player.getInventory()), FluidType.BUCKET_VOLUME, player, true);
            if (far.isSuccess()) {
                player.setItemInHand(hand, far.getResult());
                chalice.markForUpdate();
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityChalice(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.CHALICE.get());
    }
}
