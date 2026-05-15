package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockDynamicColor;
import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collector Crystal — a starlight source that collects from the sky.
 * Tints dynamically based on the attuned constellation (stored in BE data).
 *
 * <p>1.16 -> 1.20 changes:
 * Material removed, IBlockReader -> BlockGetter,
 * ISelectionContext -> CollisionContext,
 * ILightReader -> BlockAndTintGetter,
 * Block light emission via properties.lightLevel(state -> val)</p>
 */
public class BlockCollectorCrystal extends BlockEntityBlock implements BlockDynamicColor {

    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 16, 13);

    private final boolean celestial;

    public BlockCollectorCrystal(boolean celestial) {
        super(Properties.of()
                .mapColor(celestial ? MapColor.COLOR_CYAN : MapColor.QUARTZ)
                .strength(4.0F, 20.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> celestial ? 11 : 7));
        this.celestial = celestial;
    }

    /**
     * Whether this is a celestial collector crystal (higher tier).
     */
    public boolean isCelestial() {
        return celestial;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public int getBlockColor(@Nonnull BlockState state, @Nullable BlockAndTintGetter level,
                             @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0 || level == null || pos == null) {
            return 0xFFFFFF;
        }
        // TODO: Read constellation color from BlockEntity once implemented
        return celestial ? 0x4488DD : 0xAAAAFF;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityCollectorCrystal(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.COLLECTOR_CRYSTAL.get());
    }
}
