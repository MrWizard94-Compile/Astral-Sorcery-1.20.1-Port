package hellfirepvp.astralsorcery.common.crafting.nojson.fountain;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.tile.BlockEntityFountain;
import hellfirepvp.astralsorcery.common.util.block.BlockGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Fountain effect that excavates a downward cone below the fountain.
 * During RUNNING phase, breaks one block every 32 ticks from a pre-calculated
 * vertical cone (radius 5, depth 3 below the fountain center).
 */
public class FountainEffectLiquid extends FountainEffect<LiquidContext> {

    public FountainEffectLiquid() {
        super(AstralSorcery.key("effect_liquid"));
    }

    @Nonnull
    @Override
    public LiquidContext createContext(@Nonnull BlockEntityFountain fountain) {
        LiquidContext ctx = new LiquidContext();
        Level level = fountain.getLevel();
        if (level != null) {
            ctx.digPositions.addAll(BlockGeometry.getVerticalCone(fountain.getBlockPos().below(3), 5));
        }
        return ctx;
    }

    @Override
    public void tick(@Nonnull BlockEntityFountain fountain, @Nonnull LiquidContext context,
                      int operationTick, @Nonnull OperationSegment segment) {
        if (segment != OperationSegment.RUNNING) return;
        Level level = fountain.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (++context.ticksSinceLastDig >= 32) {
            context.ticksSinceLastDig = 0;
            digNextBlock(serverLevel, context);
        }
    }

    private void digNextBlock(@Nonnull ServerLevel level, @Nonnull LiquidContext context) {
        Iterator<BlockPos> it = context.digPositions.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.hasBlockEntity() || state.getBlock().defaultDestroyTime() < 0) {
                it.remove();
                continue;
            }
            level.destroyBlock(pos, false);
            it.remove();
            return;
        }
    }

    @Override
    public void transition(@Nonnull BlockEntityFountain fountain, @Nonnull LiquidContext context,
                            @Nonnull OperationSegment prev, @Nonnull OperationSegment next) {
        if (next == OperationSegment.RUNNING) {
            Level level = fountain.getLevel();
            if (level != null) {
                context.digPositions.clear();
                context.digPositions.addAll(BlockGeometry.getVerticalCone(fountain.getBlockPos().below(3), 5));
            }
        }
    }

    @Override
    public void onReplace(@Nonnull BlockEntityFountain fountain, @Nonnull LiquidContext context,
                           @Nullable FountainEffect<?> newEffect) {
        context.digPositions.clear();
    }
}
