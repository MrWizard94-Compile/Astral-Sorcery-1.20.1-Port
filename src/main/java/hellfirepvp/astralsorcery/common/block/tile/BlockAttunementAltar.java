package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
 * Attunement Altar — used to attune players and crystals to constellations.
 * Part of a multiblock structure with spectral relay pattern.
 *
 * <p>1.16 -> 1.20 changes: Same as other tile blocks.</p>
 */
public class BlockAttunementAltar extends BlockEntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),   // Base
            Block.box(2, 4, 2, 14, 6, 14)    // Top plate
    );

    public BlockAttunementAltar() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(3.0F, 25.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> 6));
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
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level,
                                 @Nonnull BlockPos pos, @Nonnull Player player,
                                 @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof ItemConstellationPaper) {
            ResourceLocation cstKey = ItemConstellationPaper.getConstellation(held);
            if (cstKey == null) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.translatable("astralsorcery.attunement_altar.no_constellation"), true);
                }
                return InteractionResult.FAIL;
            }
            IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
            if (!(cst instanceof IMajorConstellation)) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.translatable("astralsorcery.attunement_altar.not_major"), true);
                }
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BlockEntityAttunementAltar altar) {
                    altar.setAttunedConstellation(cstKey);
                    altar.setChanged();
                    player.displayClientMessage(
                            Component.translatable("astralsorcery.attunement_altar.set_constellation",
                                    cst.getConstellationName()), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Crystal placement / retrieval
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityAttunementAltar altar) {
                ItemStack inHand = player.getItemInHand(hand);
                ItemStack onAltar = altar.getHeldCrystal();

                if (player.isShiftKeyDown()) {
                    // Sneak-click: eject crystal
                    if (!onAltar.isEmpty()) {
                        altar.setHeldCrystal(ItemStack.EMPTY);
                        if (inHand.isEmpty()) {
                            player.setItemInHand(hand, onAltar);
                        } else if (!player.addItem(onAltar)) {
                            Block.popResource(level, pos, onAltar);
                        }
                        return InteractionResult.CONSUME;
                    }
                } else if (inHand.getItem() instanceof ItemCrystalBase && onAltar.isEmpty()) {
                    // Place crystal on altar
                    altar.setHeldCrystal(inHand.copyWithCount(1));
                    if (!player.isCreative()) inHand.shrink(1);
                    return InteractionResult.CONSUME;
                } else if (inHand.isEmpty() && !onAltar.isEmpty()) {
                    // Retrieve crystal
                    altar.setHeldCrystal(ItemStack.EMPTY);
                    player.setItemInHand(hand, onAltar);
                    return InteractionResult.CONSUME;
                }
            }
        } else if (player.getItemInHand(hand).getItem() instanceof ItemCrystalBase
                || player.isShiftKeyDown()) {
            return InteractionResult.SUCCESS; // optimistic client feedback
        }

        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level,
                         @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityAttunementAltar altar) {
                ItemStack crystal = altar.getHeldCrystal();
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
        return new BlockEntityAttunementAltar(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.ATTUNEMENT_ALTAR.get());
    }
}
