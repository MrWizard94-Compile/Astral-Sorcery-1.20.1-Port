package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTreeBeaconComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tree beacon component — the internal structural block that the tree beacon multiblock
 * places inside the tree canopy. Not obtainable in survival.
 */
public class BlockTreeBeaconComponent extends BlockEntityBlock {

    public BlockTreeBeaconComponent() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.GRASS)
                .lightLevel(state -> 12)
                .noOcclusion());
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityTreeBeaconComponent(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.TREE_BEACON_COMPONENT.get());
    }
}
