package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.util.collision.CollisionHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Hooks into {@link Entity#collideBoundingBox} to apply custom
 * collision logic from {@link CollisionHelper} (e.g. Horologium time-stop zones).
 *
 * <p>1.16 → 1.20: Entity.collideBoundingBoxHeuristically → Entity.collideBoundingBox
 * (now a static method). VoxelShapeSpliterator mixin dropped; custom shapes are
 * injected here by modifying the post-collision return value.</p>
 */
@Mixin(Entity.class)
public class MixinEntity {

    @Inject(
            method = "collideBoundingBox",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void addCustomCollision(
            @Nullable Entity entity,
            Vec3 vec,
            AABB collisionBox,
            Level world,
            List<VoxelShape> potentialHits,
            CallbackInfoReturnable<Vec3> cir) {
        if (entity == null) {
            return;
        }
        Vec3 allowedMovement = CollisionHelper.onEntityCollision(cir.getReturnValue(), entity);
        if (allowedMovement != null) {
            cir.setReturnValue(allowedMovement);
        }
    }
}
