package hellfirepvp.astralsorcery.common.crafting.nojson.fountain;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.tile.BlockEntityFountain;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Fountain effect that pulls living entities (non-players) into a capture zone
 * 4 blocks below the fountain, holding them suspended there.
 *
 * <p>Capture zone: 5×5×5 centered at fountain.pos.below(4).
 * Pull zone: capture zone expanded by 14 blocks in all directions.
 * Entities in capture zone are frozen. Entities in pull zone are drawn inward.</p>
 */
public class FountainEffectVortex extends FountainEffect<VortexContext> {

    private static final int CAPTURE_RADIUS = 2;
    private static final int PULL_EXTRA = 14;

    public FountainEffectVortex() {
        super(AstralSorcery.key("effect_vortex"));
    }

    @Nonnull
    @Override
    public VortexContext createContext(@Nonnull BlockEntityFountain fountain) {
        return new VortexContext();
    }

    @Override
    public void tick(@Nonnull BlockEntityFountain fountain, @Nonnull VortexContext context,
                      int operationTick, @Nonnull OperationSegment segment) {
        if (segment != OperationSegment.RUNNING) return;
        Level level = fountain.getLevel();
        if (!(level instanceof ServerLevel)) return;

        BlockPos pos = fountain.getBlockPos();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() - 4.0;
        double cz = pos.getZ() + 0.5;

        AABB captureBox = new AABB(cx - CAPTURE_RADIUS, cy - CAPTURE_RADIUS, cz - CAPTURE_RADIUS,
                cx + CAPTURE_RADIUS, cy + CAPTURE_RADIUS, cz + CAPTURE_RADIUS);
        AABB pullBox = captureBox.inflate(PULL_EXTRA);
        Vector3 target = new Vector3(cx, cy, cz);

        List<LivingEntity> inCapture = level.getEntitiesOfClass(LivingEntity.class, captureBox,
                e -> e.isAlive() && !(e instanceof Player));
        for (LivingEntity entity : inCapture) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hurtMarked = true;
        }

        List<LivingEntity> inPull = level.getEntitiesOfClass(LivingEntity.class, pullBox,
                e -> e.isAlive() && !(e instanceof Player) && !inCapture.contains(e));
        for (LivingEntity entity : inPull) {
            EntityUtils.applyVortexMotion(
                    () -> new Vector3(entity.getX(), entity.getY(), entity.getZ()),
                    v -> entity.setDeltaMovement(entity.getDeltaMovement().add(v.getX(), v.getY(), v.getZ())),
                    target, CAPTURE_RADIUS + PULL_EXTRA, 1.0);
            entity.hurtMarked = true;
        }
    }

    @Override
    public void transition(@Nonnull BlockEntityFountain fountain, @Nonnull VortexContext context,
                            @Nonnull OperationSegment prev, @Nonnull OperationSegment next) {}

    @Override
    public void onReplace(@Nonnull BlockEntityFountain fountain, @Nonnull VortexContext context,
                           @Nullable FountainEffect<?> newEffect) {}
}
