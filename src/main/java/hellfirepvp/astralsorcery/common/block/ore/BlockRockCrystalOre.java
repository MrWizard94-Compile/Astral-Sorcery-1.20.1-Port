package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;

/**
 * Rock Crystal Ore — spawns deep underground, requires special conditions to find.
 * Only visible/mineable under starlight conditions (constellation focus or nighttime).
 *
 * <p>1.16 -> 1.20 changes:
 * Material.ROCK removed, xp dropping via getExpDrop event,
 * RandomSource replaces java.util.Random in block methods</p>
 */
public class BlockRockCrystalOre extends BlockAS {

    public BlockRockCrystalOre() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .strength(4.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 4));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(@Nonnull BlockState state, @Nonnull ServerLevel level,
                           @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        // TODO: Crystal growth logic — chance to grow crystal size
        // when exposed to starlight from above
    }
}
