package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.object.ObjectReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Raytrace utility for starlight transmission — steps through blocks
 * along a ray and checks for obstructions.
 *
 * <p>1.16 → 1.20 changes:
 * World → Level, AxisAlignedBB → AABB,
 * Material.GLASS removed → Tags.Blocks.GLASS / GLASS_PANES tag check,
 * getEntitiesWithinAABB → getEntitiesOfClass,
 * getEntityByID → getEntity,
 * entity.getEntityId → entity.getId,
 * isAirBlock → isEmptyBlock</p>
 */
public class RaytraceAssist {

    private static final List<BlockPredicate> passable = new ArrayList<>();
    private static final double STEP_WIDTH = 0.1;
    private static final Vector3 CENTRALIZE = new Vector3(0.5, 0.5, 0.5);

    private final Vector3 start;
    private final Vector3 target;
    private final BlockPos startPos;
    private final BlockPos targetPos;

    private boolean collectEntities = false;
    private final Set<Integer> collected = new HashSet<>();
    @Nullable
    private AABB collectBox = null;
    private boolean includeEnd = false;
    private boolean hitBlocks = true;
    private boolean hitFluids = true;
    private double stepWidth = STEP_WIDTH;

    @Nullable
    private BlockPos posHit = null;

    public RaytraceAssist(@Nonnull BlockPos start, @Nonnull BlockPos target) {
        this(new Vector3(start).add(CENTRALIZE), new Vector3(target).add(CENTRALIZE));
    }

    public RaytraceAssist(@Nonnull Vector3 start, @Nonnull Vector3 target) {
        this.start = start.clone();
        this.target = target.clone();
        this.startPos = this.start.toBlockPos();
        this.targetPos = this.target.toBlockPos();
    }

    @Nonnull
    public RaytraceAssist includeEndPoint() {
        this.includeEnd = true;
        return this;
    }

    @Nonnull
    public RaytraceAssist hitBlock(boolean hitBlocks) {
        this.hitBlocks = hitBlocks;
        return this;
    }

    @Nonnull
    public RaytraceAssist hitFluidsBeforeBlocks(boolean hitFluids) {
        this.hitFluids = hitFluids;
        return this;
    }

    @Nonnull
    public RaytraceAssist stepWith(double stepWidth) {
        this.stepWidth = stepWidth;
        return this;
    }

    public void setCollectEntities(double radius) {
        this.collectEntities = true;
        this.collectBox = new AABB(0, 0, 0, 0, 0, 0);
        this.collectBox = this.collectBox.inflate(radius).move(radius, 0, radius);
    }

    public boolean isClear(@Nonnull Level level) {
        return this.forEachBlockPos(at -> {
            if (collectEntities && collectBox != null) {
                List<Entity> entities = level.getEntitiesOfClass(Entity.class,
                        collectBox.move(at.getX(), at.getY(), at.getZ()));
                for (Entity b : entities) {
                    collected.add(b.getId());
                }
            }

            Boolean chunkResult = MiscUtils.executeWithChunk(level, at, () -> {
                if (!isStartEnd(at) && !level.isEmptyBlock(at)) {
                    if (this.hitFluids && !level.getFluidState(at).isEmpty()) {
                        posHit = at;
                        return false;
                    }
                    if ((this.hitBlocks || this.hitFluids)
                            && !isAllowed(level, at, level.getBlockState(at))) {
                        posHit = at;
                        return false;
                    }
                }
                return true;
            }, false);
            return Boolean.TRUE.equals(chunkResult);
        });
    }

    public boolean forEachStep(@Nonnull Predicate<Vector3> raystepFn) {
        Vector3 aim = start.vectorFromHereTo(target);
        Vector3 stepAim = aim.clone().normalize().multiply(this.stepWidth);
        double distance = aim.length();
        Vector3 prevVec = start.clone();

        for (double distancePart = this.stepWidth; distancePart <= distance;
             distancePart += this.stepWidth) {
            Vector3 stepVec = prevVec.clone().add(stepAim);
            if (!raystepFn.test(stepVec.clone())) {
                return false;
            }
            prevVec = stepVec;
        }
        return true;
    }

    public boolean forEachBlockPos(@Nonnull Predicate<BlockPos> raystepFn) {
        ObjectReference<BlockPos> lastPos = new ObjectReference<>();
        return this.forEachStep(vec -> {
            BlockPos at = vec.toBlockPos();
            if (lastPos.get() == null || !lastPos.get().equals(at)) {
                lastPos.set(at);
                return raystepFn.test(at);
            }
            return true;
        });
    }

    @Nullable
    public BlockPos positionHit() {
        return posHit;
    }

    @Nonnull
    public List<Entity> collectedEntities(@Nonnull Level level) {
        List<Entity> entities = new LinkedList<>();
        for (Integer id : collected) {
            Entity e = level.getEntity(id);
            if (e != null && e.isAlive()) {
                entities.add(e);
            }
        }
        return entities;
    }

    private boolean isAllowed(@Nonnull Level level, @Nonnull BlockPos at,
                              @Nonnull BlockState state) {
        return MiscUtils.contains(passable, predicate -> predicate.test(level, at, state));
    }

    private boolean isStartEnd(@Nonnull BlockPos hit) {
        return hit.equals(startPos) || (!includeEnd && hit.equals(targetPos));
    }

    public static void addPassable(@Nonnull BlockPredicate predicate) {
        passable.add(predicate);
    }

    static {
        // In 1.20, Material is removed. Use Forge glass tags instead of Material.GLASS.
        addPassable((level, pos, state) -> state.getFluidState().isEmpty()
                && (state.is(Tags.Blocks.GLASS) || state.is(Tags.Blocks.GLASS_PANES)));
    }
}
