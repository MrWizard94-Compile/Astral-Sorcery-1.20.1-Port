package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Collision helper for applying custom collision boxes to entities.
 * In 1.16, onCollision() hooked into VoxelShapeSpliterator via mixin;
 * in 1.20, the collision iteration API (BlockCollisions / CollisionGetter)
 * was restructured. The mixin hook will need rewriting in Phase 13.
 *
 * <p>1.16 -> 1.20 changes:
 * AxisAlignedBB -> AABB,
 * VoxelShapes -> Shapes,
 * IBooleanFunction -> BooleanOp,
 * Vector3d -> Vec3,
 * VoxelShapeSpliterator removed (must use BlockCollisions or similar in 1.20),
 * Entity.getBoundingBox() unchanged,
 * VoxelShape.getAllowedOffset -> Shapes.collide</p>
 */
public class CollisionHelper {

    /**
     * Adjusts entity movement based on custom collision boxes.
     * Called from entity collision processing.
     *
     * @param allowedMovement the movement vector the entity wants to perform
     * @param entity the entity being moved
     * @return adjusted movement vector, or null if no custom collision applies
     */
    @Nullable
    public static Vec3 onEntityCollision(@Nonnull Vec3 allowedMovement, @Nonnull Entity entity) {
        if (!CollisionManager.needsCustomCollision(entity)) {
            return null;
        }
        List<AABB> additionalBoxes = CollisionManager.getAdditionalBoundingBoxes(entity);
        AABB entityBox = entity.getBoundingBox().inflate(1.0E-7D);

        double newY = allowedMovement.y;
        for (AABB box : additionalBoxes) {
            VoxelShape shape = Shapes.create(box);
            newY = shape.collide(net.minecraft.core.Direction.Axis.Y, entityBox, newY);
        }

        return new Vec3(allowedMovement.x, newY, allowedMovement.z);
    }
}
