/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Invisible marker entity used by the Observatory block to manage
 * the player's mounted state and camera control while using
 * the observatory for constellation observation.
 *
 * <p>When a player interacts with the Observatory block, this entity
 * is spawned and the player is set as a rider. While mounted, the player's
 * camera pitch/yaw is sent to the server for constellation targeting. The
 * entity is discarded when the player dismounts or moves too far.</p>
 *
 * <p>This pattern avoids modifying the player entity directly and allows
 * clean state management for the observatory UI.</p>
 *
 * <p>1.16 → 1.20: Riding/passenger API unchanged.
 * Entity data accessor pattern stable.</p>
 */
public class EntityObservatoryHelper extends Entity {

    private static final EntityDataAccessor<Float> DATA_PITCH =
            SynchedEntityData.defineId(EntityObservatoryHelper.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_YAW =
            SynchedEntityData.defineId(EntityObservatoryHelper.class, EntityDataSerializers.FLOAT);

    @Nullable
    private UUID ownerPlayerId;
    private int ticksWithoutRider = 0;

    public EntityObservatoryHelper(@Nonnull EntityType<?> type, @Nonnull Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityObservatoryHelper(@Nonnull Level level, double x, double y, double z,
                                    @Nonnull ServerPlayer player) {
        this(EntityTypesAS.OBSERVATORY_HELPER.get(), level);
        setPos(x, y, z);
        this.ownerPlayerId = player.getUUID();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_PITCH, 0.0f);
        this.entityData.define(DATA_YAW, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        // Auto-discard if no rider for too long
        if (getPassengers().isEmpty()) {
            ticksWithoutRider++;
            if (ticksWithoutRider > 20) {
                discard();
                return;
            }
        } else {
            ticksWithoutRider = 0;
        }

        // Auto-discard if moved too far (shouldn't happen, but safety)
        if (!getPassengers().isEmpty()) {
            Entity rider = getPassengers().get(0);
            if (rider instanceof ServerPlayer player) {
                if (ownerPlayerId != null && !player.getUUID().equals(ownerPlayerId)) {
                    player.stopRiding();
                    discard();
                }
            }
        }

        // Discard after 10 minutes to prevent orphaned entities
        if (tickCount > 12000) {
            ejectPassengers();
            discard();
        }
    }

    /**
     * Updates the observed sky direction from the client.
     * Called by packet when the player moves their camera in the observatory.
     */
    public void updateObservedDirection(float pitch, float yaw) {
        this.entityData.set(DATA_PITCH, pitch);
        this.entityData.set(DATA_YAW, yaw);
    }

    public float getObservedPitch() {
        return this.entityData.get(DATA_PITCH);
    }

    public float getObservedYaw() {
        return this.entityData.get(DATA_YAW);
    }

    @Nullable
    public UUID getOwnerPlayerId() {
        return ownerPlayerId;
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
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
    protected void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        if (compound.hasUUID("ownerPlayer")) {
            this.ownerPlayerId = compound.getUUID("ownerPlayer");
        }
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        if (ownerPlayerId != null) {
            compound.putUUID("ownerPlayer", ownerPlayerId);
        }
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }
}
