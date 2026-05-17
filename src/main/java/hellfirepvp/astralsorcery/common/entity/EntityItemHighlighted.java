/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * A highlighted item entity that glows with a colored aura.
 * Used for special drops (shooting star fragments, ritual rewards,
 * constellation-attuned crystals) to make them stand out visually.
 *
 * <p>The highlight entity renders like a normal item drop but with
 * an additional glowing particle orbit and colored light emission.
 * The color corresponds to the source constellation or event.</p>
 *
 * <p>Extends ItemEntity to inherit all pickup and physics behavior.</p>
 *
 * <p>1.16 → 1.20: ItemEntity unchanged. SynchedEntityData stable.</p>
 */
public class EntityItemHighlighted extends ItemEntity {

    private static final EntityDataAccessor<Integer> DATA_HIGHLIGHT_COLOR =
            SynchedEntityData.defineId(EntityItemHighlighted.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_HIGHLIGHT_INTENSITY =
            SynchedEntityData.defineId(EntityItemHighlighted.class, EntityDataSerializers.FLOAT);

    public EntityItemHighlighted(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityItemHighlighted(Level level, double x, double y, double z,
                                  @Nonnull ItemStack stack, int highlightColor) {
        super(level, x, y, z, stack);
        this.entityData.set(DATA_HIGHLIGHT_COLOR, highlightColor);
        this.entityData.set(DATA_HIGHLIGHT_INTENSITY, 1.0f);
        this.setNoGravity(false);
        // Longer despawn time for highlighted drops
        this.lifespan = 12000; // 10 minutes instead of default 5
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HIGHLIGHT_COLOR, 0xCCDDFF);
        this.entityData.define(DATA_HIGHLIGHT_INTENSITY, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();

        // Gradually fade the highlight over time
        if (!level().isClientSide()) {
            float intensity = getHighlightIntensity();
            if (intensity > 0.1f) {
                // Fade slowly — about 5% per second
                this.entityData.set(DATA_HIGHLIGHT_INTENSITY, intensity - 0.0025f);
            }
        }
    }

    /**
     * Gets the highlight glow color (RGB packed int).
     */
    public int getHighlightColor() {
        return this.entityData.get(DATA_HIGHLIGHT_COLOR);
    }

    /**
     * Gets the highlight glow intensity [0, 1].
     */
    public float getHighlightIntensity() {
        return this.entityData.get(DATA_HIGHLIGHT_INTENSITY);
    }

    /**
     * Sets the highlight color (RGB packed int).
     */
    public void setHighlightColor(int color) {
        this.entityData.set(DATA_HIGHLIGHT_COLOR, color);
    }

    /**
     * Sets the highlight intensity [0, 1].
     */
    public void setHighlightIntensity(float intensity) {
        this.entityData.set(DATA_HIGHLIGHT_INTENSITY, Math.max(0, Math.min(1, intensity)));
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HighlightColor")) {
            this.entityData.set(DATA_HIGHLIGHT_COLOR, tag.getInt("HighlightColor"));
        }
        if (tag.contains("HighlightIntensity")) {
            this.entityData.set(DATA_HIGHLIGHT_INTENSITY, tag.getFloat("HighlightIntensity"));
        }
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HighlightColor", getHighlightColor());
        tag.putFloat("HighlightIntensity", getHighlightIntensity());
    }

    /**
     * Highlighted items always have the glowing tag set for outline rendering.
     * We set this via setGlowingTag in the constructor rather than overriding.
     */
    @Override
    public boolean isCurrentlyGlowing() {
        return true;
    }
}
