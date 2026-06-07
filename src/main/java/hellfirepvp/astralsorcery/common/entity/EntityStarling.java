/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A starling is a small, glowing companion entity that orbits around
 * a player or a ritual pedestal. Created by the Pelotrio constellation's
 * ritual effect, starlings provide minor passive buffs and serve as
 * visual indicators of active rituals.
 *
 * <p>Behavior:
 * <ul>
 *   <li>Orbits around their owner at a fixed radius</li>
 *   <li>Emits ambient light particles (client-side)</li>
 *   <li>Has a limited lifespan (despawns after duration expires)</li>
 *   <li>Cannot be targeted or damaged by players</li>
 *   <li>Does not collide with blocks or entities</li>
 * </ul>
 * </p>
 *
 * <p>1.16 → 1.20: Entity physics/data sync unchanged.
 * Uses NetworkHooks for spawn packet.</p>
 */
public class EntityStarling extends Entity {

    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(EntityStarling.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ORBIT_RADIUS =
            SynchedEntityData.defineId(EntityStarling.class, EntityDataSerializers.FLOAT);

    /** Default orbit radius in blocks. */
    private static final float DEFAULT_ORBIT_RADIUS = 1.5f;

    /** Orbital speed in radians per tick. */
    private static final float ORBIT_SPEED = 0.05f;

    /** Maximum lifespan in ticks (10 minutes). */
    private static final int MAX_LIFESPAN = 12000;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private BlockPos anchorPos;

    private float orbitAngle = 0f;
    private int ticksAlive = 0;

    public EntityStarling(@Nonnull EntityType<?> type, @Nonnull Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityStarling(@Nonnull Level level, @Nonnull Player owner) {
        this(EntityTypesAS.STARLING.get(), level);
        this.ownerUUID = owner.getUUID();
        setPos(owner.getX(), owner.getY() + 1.5, owner.getZ());
        this.orbitAngle = level.getRandom().nextFloat() * (float) Math.PI * 2;
    }

    public EntityStarling(@Nonnull Level level, @Nonnull BlockPos anchor) {
        this(EntityTypesAS.STARLING.get(), level);
        this.anchorPos = anchor.immutable();
        setPos(anchor.getX() + 0.5, anchor.getY() + 1.5, anchor.getZ() + 0.5);
        this.orbitAngle = level.getRandom().nextFloat() * (float) Math.PI * 2;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_COLOR, 0xCCDDFF); // Default: starlight blue-white
        this.entityData.define(DATA_ORBIT_RADIUS, DEFAULT_ORBIT_RADIUS);
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (level().isClientSide()) {
            // Client: particles handled by renderer
            return;
        }

        // Lifespan check
        if (ticksAlive > MAX_LIFESPAN) {
            discard();
            return;
        }

        // Update orbit position
        orbitAngle += ORBIT_SPEED;
        if (orbitAngle > Math.PI * 2) {
            orbitAngle -= (float) (Math.PI * 2);
        }

        Vec3 center = getOrbitCenter();
        if (center == null) {
            discard();
            return;
        }

        float radius = getOrbitRadius();
        double targetX = center.x + Mth.cos(orbitAngle) * radius;
        double targetY = center.y + Mth.sin(orbitAngle * 0.3f) * 0.3; // gentle vertical bob
        double targetZ = center.z + Mth.sin(orbitAngle) * radius;

        setPos(targetX, targetY, targetZ);
    }

    /**
     * Gets the center point for the starling's orbit.
     * Either around the owner player or around the anchor block.
     */
    @Nullable
    private Vec3 getOrbitCenter() {
        UUID oid = ownerUUID;
        if (oid != null) {
            if (level() instanceof ServerLevel serverLevel) {
                Entity owner = serverLevel.getEntity(oid);
                if (owner instanceof Player player) {
                    if (player.isAlive() && distanceToSqr(player) < 256) {
                        return new Vec3(player.getX(), player.getY() + 1.2, player.getZ());
                    }
                }
            }
            // Owner not found or level not server — despawn
            return null;
        }

        BlockPos ap = anchorPos;
        if (ap != null) {
            return new Vec3(ap.getX() + 0.5, ap.getY() + 1.5, ap.getZ() + 0.5);
        }

        return null;
    }

    public int getStarlingColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setStarlingColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }

    public float getOrbitRadius() {
        return this.entityData.get(DATA_ORBIT_RADIUS);
    }

    public void setOrbitRadius(float radius) {
        this.entityData.set(DATA_ORBIT_RADIUS, Math.max(0.5f, radius));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public int getTicksAlive() {
        return ticksAlive;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        this.ticksAlive = compound.getInt("ticksAlive");
        this.orbitAngle = compound.getFloat("orbitAngle");
        if (compound.hasUUID("ownerUUID")) {
            this.ownerUUID = compound.getUUID("ownerUUID");
        }
        if (compound.contains("anchorX")) {
            this.anchorPos = new BlockPos(
                    compound.getInt("anchorX"),
                    compound.getInt("anchorY"),
                    compound.getInt("anchorZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        compound.putInt("ticksAlive", ticksAlive);
        compound.putFloat("orbitAngle", orbitAngle);
        if (ownerUUID != null) {
            compound.putUUID("ownerUUID", ownerUUID);
        }
        BlockPos ap2 = anchorPos;
        if (ap2 != null) {
            compound.putInt("anchorX", ap2.getX());
            compound.putInt("anchorY", ap2.getY());
            compound.putInt("anchorZ", ap2.getZ());
        }
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
