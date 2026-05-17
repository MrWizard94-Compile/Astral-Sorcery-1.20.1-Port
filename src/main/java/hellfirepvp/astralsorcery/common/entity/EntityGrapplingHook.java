/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Grappling hook projectile entity for the Evorsio constellation perk effect.
 * When it hits a block, it pulls the player toward the impact point, providing
 * mobility for miners working in deep caverns.
 *
 * <p>The hook entity attaches to solid blocks and creates a "tether" to the
 * owner player. The player is pulled at a configurable speed until they reach
 * the block or the tether breaks (exceed max range).</p>
 *
 * <p>1.16 → 1.20: ThrowableProjectile unchanged. Velocity handling stable.</p>
 */
public class EntityGrapplingHook extends ThrowableProjectile {

    private static final EntityDataAccessor<Boolean> DATA_ATTACHED =
            SynchedEntityData.defineId(EntityGrapplingHook.class, EntityDataSerializers.BOOLEAN);

    private static final double MAX_RANGE = 64.0;
    private static final double PULL_SPEED = 0.8;
    private static final int MAX_ATTACH_TIME = 100; // 5 seconds max attached

    private int attachedTicks = 0;

    public EntityGrapplingHook(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityGrapplingHook(EntityType<? extends ThrowableProjectile> type,
                               Level level, @Nonnull LivingEntity owner) {
        super(type, owner, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ATTACHED, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (isAttached()) {
                attachedTicks++;
                if (attachedTicks > MAX_ATTACH_TIME) {
                    this.discard();
                    return;
                }
                pullOwner();
            }

            // Check range
            Player owner = getOwnerPlayer();
            if (owner != null && distanceTo(owner) > MAX_RANGE) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide()) {
            this.entityData.set(DATA_ATTACHED, true);
            // Stop motion — anchor to block
            setDeltaMovement(Vec3.ZERO);
        }
    }

    private void pullOwner() {
        Player owner = getOwnerPlayer();
        if (owner == null || owner.isRemoved()) {
            this.discard();
            return;
        }

        Vec3 direction = position().subtract(owner.position()).normalize();
        double distance = distanceTo(owner);

        if (distance < 1.5) {
            // Close enough — detach
            this.discard();
            return;
        }

        Vec3 pullVec = direction.scale(PULL_SPEED);
        owner.setDeltaMovement(pullVec);
        owner.hurtMarked = true; // Force client to apply velocity
        owner.fallDistance = 0; // Prevent fall damage during pull
    }

    @Nullable
    private Player getOwnerPlayer() {
        if (getOwner() instanceof Player player) {
            return player;
        }
        return null;
    }

    public boolean isAttached() {
        return this.entityData.get(DATA_ATTACHED);
    }

    @Override
    protected float getGravity() {
        return isAttached() ? 0.0f : 0.03f;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_ATTACHED, tag.getBoolean("Attached"));
        this.attachedTicks = tag.getInt("AttachedTicks");
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Attached", isAttached());
        tag.putInt("AttachedTicks", attachedTicks);
    }
}
