package hellfirepvp.astralsorcery.common.data.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-chunk data tracking fluid availability for the lightwell system.
 * Each chunk can have a "remaining fluid" budget that depletes as
 * lightwells extract fluid and regenerates over time.
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag</p>
 */
public class ChunkFluidEntry {

    private static final String TAG_ENTRIES = "fluidEntries";

    /**
     * Map from item registry name -> remaining fluid budget for that item type.
     * When a lightwell processes an item, it draws from this per-chunk budget.
     */
    @Nonnull
    private final Map<ResourceLocation, Float> remainingFluid = new HashMap<>();

    /**
     * Get remaining fluid budget for a given catalyst item in this chunk.
     *
     * @param catalystItem the item being liquified
     * @return the remaining fluid amount, or 0 if none
     */
    public float getRemaining(@Nonnull ResourceLocation catalystItem) {
        return remainingFluid.getOrDefault(catalystItem, 0.0F);
    }

    /**
     * Set the remaining fluid budget for a catalyst item.
     */
    public void setRemaining(@Nonnull ResourceLocation catalystItem, float amount) {
        if (amount <= 0) {
            remainingFluid.remove(catalystItem);
        } else {
            remainingFluid.put(catalystItem, amount);
        }
    }

    /**
     * Drain fluid from the budget for a given catalyst.
     *
     * @return the actual amount drained (may be less than requested)
     */
    public float drain(@Nonnull ResourceLocation catalystItem, float amount) {
        float current = getRemaining(catalystItem);
        float drained = Math.min(current, amount);
        setRemaining(catalystItem, current - drained);
        return drained;
    }

    public boolean isEmpty() {
        return remainingFluid.isEmpty();
    }

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag entries = new CompoundTag();
        for (Map.Entry<ResourceLocation, Float> entry : remainingFluid.entrySet()) {
            entries.putFloat(entry.getKey().toString(), entry.getValue());
        }
        tag.put(TAG_ENTRIES, entries);
        return tag;
    }

    public void readFromNBT(@Nonnull CompoundTag tag) {
        remainingFluid.clear();
        if (tag.contains(TAG_ENTRIES)) {
            CompoundTag entries = tag.getCompound(TAG_ENTRIES);
            for (String key : entries.getAllKeys()) {
                remainingFluid.put(new ResourceLocation(key), entries.getFloat(key));
            }
        }
    }
}
