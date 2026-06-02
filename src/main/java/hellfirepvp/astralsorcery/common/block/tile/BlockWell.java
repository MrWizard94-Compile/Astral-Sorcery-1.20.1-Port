package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.block.base.LiquidStarlightOwned;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityWell;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Lightwell — converts rock crystals into liquid starlight over time.
 * Uses a PrecisionSingleFluidTank for sub-mB drip mechanics.
 *
 * <p>1.16 -> 1.20 changes:
 * Material removed, VoxelShapes.or -> Shapes.or,
 * ActionResultType -> InteractionResult</p>
 */
public class BlockWell extends BlockEntityBlock implements LiquidStarlightOwned {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),   // Base
            Block.box(0, 0, 0, 2, 14, 2),    // Corner pillar NW
            Block.box(14, 0, 0, 16, 14, 2),  // Corner pillar NE
            Block.box(0, 0, 14, 2, 14, 16),  // Corner pillar SW
            Block.box(14, 0, 14, 16, 14, 16),// Corner pillar SE
            Block.box(0, 1, 0, 16, 3, 16)    // Basin
    );

    public BlockWell() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(3.0F, 15.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    public int getMaxLiquidStarlight() {
        return 2000; // 2 buckets
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
        if (!(be instanceof BlockEntityWell well)) {
            return InteractionResult.PASS;
        }
        // Shift-click: retrieve catalyst
        if (player.isShiftKeyDown() && well.hasCatalyst()) {
            ItemStack catalyst = well.getCatalystStack().copy();
            well.setCatalystStack(ItemStack.EMPTY);
            if (!player.addItem(catalyst)) Block.popResource(level, pos, catalyst);
            return InteractionResult.CONSUME;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }

        // Try inserting as a liquefaction catalyst (only if slot is empty)
        if (!well.hasCatalyst()) {
            List<WellLiquefaction> recipes = level.getRecipeManager()
                    .getAllRecipesFor(RecipeTypesAS.WELL_LIQUEFACTION.get());
            for (WellLiquefaction recipe : recipes) {
                if (recipe.matches(heldItem)) {
                    well.setCatalystStack(heldItem.copyWithCount(1));
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS, 0.2F,
                            ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    return InteractionResult.CONSUME;
                }
            }
        }

        // Try filling a fluid container (bucket, etc.) from the well's tank
        IFluidHandler fluidHandler = well.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .resolve().orElse(null);
        if (fluidHandler != null) {
            FluidActionResult far = FluidUtil.tryFillContainerAndStow(
                    heldItem, fluidHandler, new InvWrapper(player.getInventory()),
                    FluidType.BUCKET_VOLUME, player, true);
            if (far.isSuccess()) {
                player.setItemInHand(hand, far.getResult());
                level.playSound(null, pos, SoundEvents.BUCKET_FILL,
                        SoundSource.PLAYERS, 1F, 1F);
                well.markForUpdate();
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level,
                         @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityWell well) {
                ItemStack catalyst = well.getCatalystStack();
                if (!catalyst.isEmpty()) {
                    Block.popResource(level, pos, catalyst);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityWell(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.WELL.get());
    }
}
