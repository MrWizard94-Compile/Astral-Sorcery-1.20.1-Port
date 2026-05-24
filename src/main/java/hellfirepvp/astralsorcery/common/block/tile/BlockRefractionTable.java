package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.client.screen.ClientScreenHandler;
import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRefractionTable;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Refraction table — applies constellation-based engravings to items via infused glass.
 * Player interaction inserts/removes glass and input items; right-click opens the GUI.
 *
 * <p>1.16 → 1.20: ContainerBlock → BlockEntityBlock,
 * onBlockActivated → use, ActionResultType → InteractionResult,
 * onReplaced → onRemove, player.setHeldItem → player.setItemInHand.</p>
 */
public class BlockRefractionTable extends BlockEntityBlock {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BlockRefractionTable() {
        super(Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 6.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .requiresCorrectToolForDrops());
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
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level,
                                 @Nonnull BlockPos pos, @Nonnull Player player,
                                 @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            BlockEntityRefractionTable te = MiscUtils.getTileAt(level, pos,
                    BlockEntityRefractionTable.class, true);
            if (te != null) {
                if (player.isShiftKeyDown()) {
                    if (!te.getInputStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player,
                                te.setInputStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, player.getX(), player.getY(),
                                    player.getZ(), remaining);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    if (!te.getGlassStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player,
                                te.setGlassStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, player.getX(), player.getY(),
                                    player.getZ(), remaining);
                        }
                        return InteractionResult.SUCCESS;
                    }
                } else if (!held.isEmpty()) {
                    if (BlockEntityRefractionTable.isValidGlassStack(held)
                            && te.getGlassStack().isEmpty()) {
                        ItemStack previous = te.setGlassStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!previous.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, pos.getX() + 0.5,
                                    pos.getY() + 1.8, pos.getZ() + 0.5, previous);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            player.setItemInHand(hand, held);
                        }
                        return InteractionResult.PASS;
                    } else if (te.getInputStack().isEmpty()) {
                        ItemStack previous = te.setInputStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!previous.isEmpty()) {
                            ItemUtils.dropItemNaturally(level, pos.getX() + 0.5,
                                    pos.getY() + 1.8, pos.getZ() + 0.5, previous);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            player.setItemInHand(hand, held);
                        }
                    }
                }
            }
        } else if (!player.isShiftKeyDown() && held.isEmpty()) {
            // Client-side: open engraving screen when right-clicking with empty hand
            BlockEntityRefractionTable te = MiscUtils.getTileAt(level, pos,
                    BlockEntityRefractionTable.class, true);
            if (te != null) {
                openRefractionScreen(te);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private static void openRefractionScreen(@Nonnull BlockEntityRefractionTable te) {
        ClientScreenHandler.openRefractionTableScreen(te);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level,
                         @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntityRefractionTable te = MiscUtils.getTileAt(level, pos,
                    BlockEntityRefractionTable.class, true);
            if (te != null && !level.isClientSide()) {
                te.dropContents();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityRefractionTable(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.REFRACTION_TABLE.get());
    }
}
