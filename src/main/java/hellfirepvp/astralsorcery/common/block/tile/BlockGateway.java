package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityGateway;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Celestial Gateway — teleportation node for cross-world travel.
 * Part of the gateway network registered via GatewayHelper.
 *
 * <p>1.16 -> 1.20 changes: Same as other tile blocks.</p>
 */
public class BlockGateway extends BlockEntityBlock {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public BlockGateway() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .strength(4.0F, 20.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> 8));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityGateway(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.GATEWAY.get());
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                            @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        DyeColor color = BlockEntityGateway.getItemColor(stack);
        if (color != null && level.getBlockEntity(pos) instanceof BlockEntityGateway gateway) {
            gateway.setColor(color);
        }
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                 @Nonnull Player player, @Nonnull InteractionHand hand,
                                 @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) {
            openScreenClient();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreenClient() {
        net.minecraft.client.Minecraft.getInstance().setScreen(
                new hellfirepvp.astralsorcery.client.screen.ScreenGateway());
    }
}
