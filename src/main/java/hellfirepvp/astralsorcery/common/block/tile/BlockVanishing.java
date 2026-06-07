package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityVanishing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Vanishing block — a barrier-like block placed by the Aevitas perk.
 * Passable by mobs; only blocks players. Managed by {@link BlockEntityVanishing}.
 *
 * <p>1.16 → 1.20: ContainerBlock → BlockEntityBlock, VoxelShapes → Shapes,
 * canEntityDestroy removed (handled by event), canCreatureSpawn replaced by
 * isValidSpawn on block state.</p>
 */
public class BlockVanishing extends BlockEntityBlock {

    public BlockVanishing() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Nonnull
    @Override

    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return Shapes.empty();
    }

    @Nonnull
    @Override

    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                                        @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext eCtx) {
            Entity entity = eCtx.getEntity();
            if (entity instanceof Player) {
                return Shapes.block();
            }
        }
        return Shapes.empty();
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityVanishing(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.VANISHING.get());
    }
}
