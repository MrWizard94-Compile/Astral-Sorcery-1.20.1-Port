package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom collision handlers for entities (e.g., Aevitas walkable air).
 * In 1.16, this also cached VoxelShapeSpliterator results; that caching
 * is removed because VoxelShapeSpliterator was restructured in 1.20.
 * The mixin that hooks into the collision system will need to be rewritten
 * for 1.20's BlockCollisions / CollisionGetter API (Phase 13).
 *
 * <p>1.16 -> 1.20 changes:
 * Entity -> net.minecraft.world.entity.Entity,
 * AxisAlignedBB -> AABB,
 * BlockPos.ZERO -> BlockPos.ZERO (unchanged),
 * VoxelShapeSpliterator caching removed (internal class restructured)</p>
 */
public class CollisionManager {

    private static final List<CustomCollisionHandler> customHandlers = new ArrayList<>();

    public static void init() {
        // In 1.16: register(new MantleEffectAevitas.PlayerWalkableAir());
        // Deferred until MantleEffectAevitas is ported (Phase 9)
    }

    public static void register(@Nonnull CustomCollisionHandler handler) {
        customHandlers.add(handler);
    }

    public static boolean needsCustomCollision(@Nullable Entity entity) {
        for (CustomCollisionHandler handler : customHandlers) {
            if (handler.shouldAddCollisionFor(entity)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public static List<AABB> getAdditionalBoundingBoxes(@Nullable Entity entity) {
        List<AABB> additionalCollision = new ArrayList<>();
        AABB entityBox = entity != null
                ? entity.getBoundingBox()
                : new AABB(0, 0, 0, 1, 1, 1);
        customHandlers.stream()
                .filter(handler -> handler.shouldAddCollisionFor(entity))
                .forEach(handler -> handler.addCollision(entity, entityBox, additionalCollision));
        return additionalCollision;
    }
}
