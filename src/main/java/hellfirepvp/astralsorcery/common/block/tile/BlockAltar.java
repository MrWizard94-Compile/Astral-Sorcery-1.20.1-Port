package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The Starlight Crafting Altar — the mod's primary crafting device.
 * Has 4 tiers: Discovery, Attunement, Constellation, Radiance.
 * The tier is stored as a block state property.
 *
 * <p>1.16 -> 1.20 changes:
 * Block.Properties.create -> Block.Properties.of,
 * Material.ROCK removed,
 * IStringSerializable -> StringRepresentable,
 * VoxelShapes unchanged,
 * ActionResultType -> InteractionResult,
 * PlayerEntity -> Player,
 * IBlockReader -> BlockGetter,
 * ISelectionContext -> CollisionContext,
 * BlockRayTraceResult -> BlockHitResult</p>
 */
public class BlockAltar extends BlockEntityBlock {

    public static final EnumProperty<AltarType> ALTAR_TYPE = EnumProperty.create("altar_type", AltarType.class);

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public BlockAltar() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(3.0F, 15.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ALTAR_TYPE, AltarType.DISCOVERY));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ALTAR_TYPE);
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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityAltar altar) {
            NetworkHooks.openScreen((ServerPlayer) player, altar, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityAltar(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.ALTAR.get());
    }

    /**
     * The 4 altar tier types. Ordered from lowest to highest progression.
     */
    public enum AltarType implements StringRepresentable {
        DISCOVERY,
        ATTUNEMENT,
        CONSTELLATION,
        RADIANCE;

        @Override
        @Nonnull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        /**
         * Get the tier level (0-based).
         */
        public int getOrdinalTier() {
            return ordinal();
        }

        /**
         * Whether this tier is at least the given tier.
         */
        public boolean isAtLeast(@Nonnull AltarType other) {
            return this.ordinal() >= other.ordinal();
        }
    }
}
