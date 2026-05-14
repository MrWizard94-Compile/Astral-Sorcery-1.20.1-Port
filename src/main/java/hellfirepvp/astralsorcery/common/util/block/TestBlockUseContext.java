package hellfirepvp.astralsorcery.common.util.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A fake BlockPlaceContext for testing block replaceability without a real player.
 *
 * <p>1.16 → 1.20 changes:
 * BlockItemUseContext → BlockPlaceContext,
 * Hand → InteractionHand,
 * BlockRayTraceResult → BlockHitResult,
 * Vector3d.copyCentered → Vec3.atCenterOf,
 * rotationYaw → getYRot(),
 * Direction.fromAngle → Direction.fromYRot,
 * Direction.getFacingDirections → Direction.orderedByNearest,
 * getPlacementHorizontalFacing → getHorizontalDirection,
 * hasSecondaryUseForPlayer → isSecondaryUseActive,
 * getPlacementYaw → getRotation,
 * getFace → getClickedFace</p>
 */
public class TestBlockUseContext extends BlockPlaceContext {

    @Nullable
    private final Entity entity;

    private TestBlockUseContext(@Nonnull Level level, @Nullable Entity usingEntity,
                               @Nonnull InteractionHand hand, @Nonnull ItemStack stack,
                               @Nonnull BlockPos at, @Nonnull Direction side) {
        super(level, null, hand, stack,
                new BlockHitResult(Vec3.atCenterOf(at), side, at, false));
        this.entity = usingEntity;
    }

    @Nonnull
    public static BlockPlaceContext getHandContext(@Nonnull Level level,
                                                  @Nullable Entity usingEntity,
                                                  @Nonnull InteractionHand usedHand,
                                                  @Nonnull BlockPos at,
                                                  @Nonnull Direction side) {
        return getHandContextWithItem(level, usingEntity, usedHand, ItemStack.EMPTY, at, side);
    }

    @Nonnull
    public static BlockPlaceContext getHandContextWithItem(@Nonnull Level level,
                                                          @Nullable Entity usingEntity,
                                                          @Nonnull InteractionHand usedHand,
                                                          @Nonnull ItemStack stack,
                                                          @Nonnull BlockPos at,
                                                          @Nonnull Direction side) {
        return new TestBlockUseContext(level, usingEntity, usedHand, stack, at, side);
    }

    @Override
    @Nonnull
    public Direction getHorizontalDirection() {
        return this.entity == null ? Direction.NORTH : Direction.fromYRot(this.entity.getYRot());
    }

    @Override
    @Nonnull
    public Direction getNearestLookingDirection() {
        if (this.entity == null) {
            return Direction.DOWN;
        }
        return Direction.orderedByNearest(this.entity)[0];
    }

    @Override
    @Nonnull
    public Direction[] getNearestLookingDirections() {
        if (this.entity == null) {
            return Direction.values();
        }
        Direction[] adirection = Direction.orderedByNearest(this.entity);
        if (this.replaceClicked) {
            return adirection;
        } else {
            Direction direction = this.getClickedFace();

            int i = 0;
            while (i < adirection.length && adirection[i] != direction.getOpposite()) {
                ++i;
            }

            if (i > 0) {
                System.arraycopy(adirection, 0, adirection, 1, i);
                adirection[0] = direction.getOpposite();
            }
            return adirection;
        }
    }

    @Override
    public boolean isSecondaryUseActive() {
        return false;
    }

    @Override
    public float getRotation() {
        return 0F;
    }
}
