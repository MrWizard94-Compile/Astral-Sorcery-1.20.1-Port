package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Flare light — an air-like decorative light block with a DyeColor state.
 * Placed by rituals and effects; not craftable.
 */
public class BlockFlareLight extends BlockAS {

    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    private static final VoxelShape SHAPE = Block.box(6, 3, 6, 10, 7, 10);

    public BlockFlareLight() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F)
                .sound(SoundType.GLASS)
                .noCollission()
                .lightLevel(state -> 15)
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(COLOR, DyeColor.YELLOW));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
                               @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(@Nonnull BlockState state, @Nonnull Level level,
                            @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        if (random.nextInt(4) != 0) return;
        DyeColor dye = state.getValue(COLOR);
        float[] rgb = dye.getTextureDiffuseColors();
        Color color = new Color(rgb[0], rgb[1], rgb[2]);
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        hellfirepvp.astralsorcery.client.effect.EffectHelper.flare(center, color, 0.15f);
    }
}
