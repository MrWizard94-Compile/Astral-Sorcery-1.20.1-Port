package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityIlluminator;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Player-placed illuminator device.
 * When placed by a player it activates its tile entity which scans cave layers
 * and places flare-light blocks in unlit air pockets.
 *
 * <p>1.16 → 1.20: ContainerBlock → BlockEntityBlock, IBooleanFunction.OR → BooleanOp.OR,
 * Block.makeCuboidShape → Block.box, VoxelUtils.combineAll → Shapes.joinUnoptimized,
 * onBlockPlacedBy(World) → setPlacedBy(Level).</p>
 */
public class BlockIlluminator extends BlockEntityBlock {

    private static final VoxelShape SHAPE = buildShape();

    public BlockIlluminator() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(1.5F, 6.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 10)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    private static VoxelShape buildShape() {
        List<VoxelShape> boxes = new ArrayList<>();
        for (int xx = 0; xx < 3; xx++) {
            for (int yy = 0; yy < 3; yy++) {
                for (int zz = 0; zz < 3; zz++) {
                    boxes.add(Block.box(
                            1 + xx * 5, 1 + yy * 5, 1 + zz * 5,
                            5 + xx * 5, 5 + yy * 5, 5 + zz * 5));
                }
            }
        }
        VoxelShape result = Shapes.empty();
        for (VoxelShape box : boxes) {
            result = Shapes.joinUnoptimized(result, box, BooleanOp.OR);
        }
        return result.optimize();
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                            @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player) {
            BlockEntityIlluminator illuminator = MiscUtils.getTileAt(level, pos,
                    BlockEntityIlluminator.class, true);
            if (illuminator != null) {
                illuminator.setPlayerPlaced(true);
            }
        }
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityIlluminator(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.ILLUMINATOR.get());
    }
}
