package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface for custom collision providers (e.g., Aevitas walkable-air effect).
 *
 * <p>1.16 -> 1.20 changes:
 * Entity -> net.minecraft.world.entity.Entity,
 * AxisAlignedBB -> AABB</p>
 */
public interface CustomCollisionHandler {

    boolean shouldAddCollisionFor(@Nullable Entity entity);

    void addCollision(@Nullable Entity entity, @Nonnull AABB testBox,
                      @Nonnull List<AABB> additionalCollision);
}
