package hellfirepvp.astralsorcery.common.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Teleporter implementation that places the entity at a specific position
 * without portal creation or placement logic.
 *
 * <p>1.16 -> 1.20 changes:
 * Teleporter (class) -> ITeleporter (interface),
 * ServerWorld -> ServerLevel,
 * setPositionAndUpdate -> teleportTo</p>
 */
public class NoOpTeleporter implements ITeleporter {

    private final double targetX;
    private final double targetY;
    private final double targetZ;

    public NoOpTeleporter(@Nonnull ServerLevel worldIn, double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    public NoOpTeleporter(@Nonnull ServerLevel worldIn, @Nonnull net.minecraft.core.BlockPos targetPos) {
        this(worldIn, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
    }

    @Override
    @Nonnull
    public Entity placeEntity(@Nonnull Entity entity, @Nonnull ServerLevel currentWorld,
                              @Nonnull ServerLevel destWorld, float yaw,
                              @Nonnull Function<Boolean, Entity> repositionEntity) {
        Entity created = repositionEntity.apply(false);
        created.teleportTo(targetX, targetY, targetZ);
        return created;
    }
}
