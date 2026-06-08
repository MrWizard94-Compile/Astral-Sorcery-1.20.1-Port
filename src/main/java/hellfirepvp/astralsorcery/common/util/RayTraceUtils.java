package hellfirepvp.astralsorcery.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Ray tracing utilities: block and generic hit results from an entity's look direction.
 */
public class RayTraceUtils {

    @Nullable
    public static BlockHitResult rayTraceLookBlock(@Nonnull Player player) {
        return rayTraceLookBlock(player, player.getBlockReach());
    }

    @Nonnull
    public static HitResult rayTraceLook(@Nonnull Player player) {
        return rayTraceLook(player, player.getBlockReach());
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(
            @Nonnull Player player,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode) {
        return rayTraceLookBlock(player, blockMode, fluidMode, player.getBlockReach());
    }

    @Nonnull
    public static HitResult rayTraceLook(
            @Nonnull Player player,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode) {
        return rayTraceLook(player, blockMode, fluidMode, player.getBlockReach());
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(@Nonnull Player player, double reachDst) {
        return rayTraceLookBlock(player,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, reachDst);
    }

    @Nonnull
    public static HitResult rayTraceLook(@Nonnull Player player, double reachDst) {
        return rayTraceLook(player,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, reachDst);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(
            @Nonnull Entity entity,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode,
            double reachDst) {
        HitResult rtr = rayTraceLook(entity, blockMode, fluidMode, reachDst);
        if (rtr.getType() == HitResult.Type.BLOCK && rtr instanceof BlockHitResult blockHit) {
            return blockHit;
        }
        return null;
    }

    @Nonnull
    public static HitResult rayTraceLook(
            @Nonnull Entity entity,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode,
            double reachDst) {
        Vec3 pos = new Vec3(entity.getX(),
                entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 lookVec = entity.getLookAngle();
        Vec3 end = pos.add(
                lookVec.x * reachDst, lookVec.y * reachDst, lookVec.z * reachDst);
        ClipContext ctx = new ClipContext(pos, end, blockMode, fluidMode, entity);
        return entity.level().clip(ctx);
    }
}
