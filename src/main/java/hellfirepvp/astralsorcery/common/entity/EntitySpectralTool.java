/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

/**
 * A spectral (ghost) tool entity summoned by the Vicio constellation's
 * perk effects or certain ritual configurations. Floats near the player
 * and periodically performs actions (mining, attacking) like a spectral helper.
 *
 * <p>The entity is purely visual on the client side — actual block-breaking
 * or entity-damaging logic is handled server-side via capability data.</p>
 *
 * <p>1.16 → 1.20 changes:
 * DataParameter → EntityDataAccessor,
 * DataSerializers → EntityDataSerializers,
 * IPacket → Packet (but entity spawn still uses Forge).</p>
 */
public class EntitySpectralTool extends Entity {

    private static final EntityDataAccessor<ItemStack> DATA_TOOL =
            SynchedEntityData.defineId(EntitySpectralTool.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(EntitySpectralTool.class, EntityDataSerializers.INT);

    /** Lifetime countdown. */
    private int remainingTicks = 200; // 10 seconds default

    /** Rotation for visual spinning effect. */
    private float rotationDeg = 0.0f;
    private float rotationSpeed = 5.0f;

    public EntitySpectralTool(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // Ghost — no collision
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TOOL, ItemStack.EMPTY);
        this.entityData.define(DATA_OWNER_ID, -1);
    }

    // ========================================================================
    // Data accessors
    // ========================================================================

    public void setToolItem(@Nonnull ItemStack tool) {
        this.entityData.set(DATA_TOOL, tool);
    }

    @Nonnull
    public ItemStack getToolItem() {
        return this.entityData.get(DATA_TOOL);
    }

    public void setOwnerId(int entityId) {
        this.entityData.set(DATA_OWNER_ID, entityId);
    }

    public int getOwnerId() {
        return this.entityData.get(DATA_OWNER_ID);
    }

    public float getRotation() {
        return rotationDeg;
    }

    // ========================================================================
    // Tick
    // ========================================================================

    @Override
    public void tick() {
        super.tick();

        remainingTicks--;
        if (remainingTicks <= 0) {
            discard();
            return;
        }

        // Visual rotation
        rotationDeg += rotationSpeed;
        if (rotationDeg >= 360.0f) {
            rotationDeg -= 360.0f;
        }

        // Gentle floating motion
        Vec3 motion = getDeltaMovement();
        double bobY = Math.sin(tickCount * 0.1) * 0.005;
        setDeltaMovement(motion.x * 0.95, bobY, motion.z * 0.95);
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
    }

    // ========================================================================
    // Serialization
    // ========================================================================

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        this.remainingTicks = tag.getInt("RemainingTicks");
        this.rotationDeg = tag.getFloat("Rotation");
        if (tag.contains("ToolItem")) {
            setToolItem(ItemStack.of(tag.getCompound("ToolItem")));
        }
        setOwnerId(tag.getInt("OwnerId"));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putInt("RemainingTicks", remainingTicks);
        tag.putFloat("Rotation", rotationDeg);
        ItemStack tool = getToolItem();
        if (!tool.isEmpty()) {
            tag.put("ToolItem", tool.save(new CompoundTag()));
        }
        tag.putInt("OwnerId", getOwnerId());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
