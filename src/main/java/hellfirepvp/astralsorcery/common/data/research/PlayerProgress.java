package hellfirepvp.astralsorcery.common.data.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Player research and progression data. Stored as a Forge capability
 * on each Player entity via {@code PlayerCapabilityProvider}.
 *
 * <p>Tracks:
 * <ul>
 *   <li>Progression tier (discovery, attunement, constellation, etc.)</li>
 *   <li>Discovered constellations</li>
 *   <li>Attuned constellation (if player is attuned)</li>
 *   <li>Perk point allocation (deferred to Phase 9)</li>
 * </ul>
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag, ListNBT -> ListTag,
 * StringNBT -> StringTag, INBT -> Tag</p>
 */
public class PlayerProgress {

    private static final String TAG_TIER = "tierReached";
    private static final String TAG_ATTUNED = "onceAttuned";
    private static final String TAG_CONSTELLATION = "attunedConstellation";
    private static final String TAG_DISCOVERED = "discoveredConstellations";
    private static final String TAG_PERK_POINTS = "perkPoints";
    private static final String TAG_PERK_EXP = "perkExp";
    private static final String TAG_ALLOCATED_PERKS = "allocatedPerks";

    @Nonnull
    private ProgressionTier tierReached = ProgressionTier.DISCOVERY;
    private boolean onceAttuned = false;
    @Nullable
    private ResourceLocation attunedConstellation = null;
    @Nonnull
    private final Set<ResourceLocation> discoveredConstellations = new HashSet<>();

    // Perk system fields
    private int perkPoints = 0;
    private long perkExp = 0;
    @Nonnull
    private final Set<ResourceLocation> allocatedPerks = new HashSet<>();

    // ---- Progression tier ----

    @Nonnull
    public ProgressionTier getTierReached() {
        return tierReached;
    }

    public void setTierReached(@Nonnull ProgressionTier tier) {
        this.tierReached = tier;
    }

    public boolean isAtLeast(@Nonnull ProgressionTier tier) {
        return tierReached.isThisLaterOrEqual(tier);
    }

    // ---- Attunement ----

    public boolean wasOnceAttuned() {
        return onceAttuned;
    }

    public void setOnceAttuned(boolean attuned) {
        this.onceAttuned = attuned;
    }

    @Nullable
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        if (constellation != null) {
            this.onceAttuned = true;
        }
    }

    // ---- Constellation discovery ----

    @Nonnull
    public Set<ResourceLocation> getDiscoveredConstellations() {
        return Collections.unmodifiableSet(discoveredConstellations);
    }

    public boolean hasDiscovered(@Nonnull ResourceLocation constellation) {
        return discoveredConstellations.contains(constellation);
    }

    public boolean discoverConstellation(@Nonnull ResourceLocation constellation) {
        return discoveredConstellations.add(constellation);
    }

    public void clearDiscoveredConstellations() {
        discoveredConstellations.clear();
    }

    // ---- Perk system ----

    public int getPerkPoints() {
        return perkPoints;
    }

    public void setPerkPoints(int points) {
        this.perkPoints = Math.max(0, points);
    }

    public long getPerkExp() {
        return perkExp;
    }

    public void setPerkExp(long exp) {
        this.perkExp = Math.max(0, exp);
    }

    /**
     * Returns the set of allocated perk keys (unmodifiable view).
     */
    @Nonnull
    public Set<ResourceLocation> getAllocatedPerks() {
        return Collections.unmodifiableSet(allocatedPerks);
    }

    /**
     * Checks whether a specific perk is allocated.
     */
    public boolean hasPerkAllocated(@Nonnull ResourceLocation perkKey) {
        return allocatedPerks.contains(perkKey);
    }

    /**
     * Allocates a perk. Does not perform validation — use PerkTree.allocate() for validated allocation.
     */
    public void allocatePerk(@Nonnull ResourceLocation perkKey) {
        allocatedPerks.add(perkKey);
    }

    /**
     * Deallocates a perk. Does not perform validation — use PerkTree.deallocate() for validated deallocation.
     */
    public void deallocatePerk(@Nonnull ResourceLocation perkKey) {
        allocatedPerks.remove(perkKey);
    }

    /**
     * Clears all allocated perks (for full respec).
     */
    public void clearAllocatedPerks() {
        allocatedPerks.clear();
    }

    // ---- NBT serialization ----

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_TIER, tierReached.ordinal());
        tag.putBoolean(TAG_ATTUNED, onceAttuned);
        if (attunedConstellation != null) {
            tag.putString(TAG_CONSTELLATION, attunedConstellation.toString());
        }
        ListTag discovered = new ListTag();
        for (ResourceLocation rl : discoveredConstellations) {
            discovered.add(StringTag.valueOf(rl.toString()));
        }
        tag.put(TAG_DISCOVERED, discovered);
        tag.putInt(TAG_PERK_POINTS, perkPoints);
        tag.putLong(TAG_PERK_EXP, perkExp);
        ListTag perkList = new ListTag();
        for (ResourceLocation rl : allocatedPerks) {
            perkList.add(StringTag.valueOf(rl.toString()));
        }
        tag.put(TAG_ALLOCATED_PERKS, perkList);
        return tag;
    }

    public void readFromNBT(@Nonnull CompoundTag tag) {
        int tierOrd = tag.getInt(TAG_TIER);
        ProgressionTier[] tiers = ProgressionTier.values();
        this.tierReached = (tierOrd >= 0 && tierOrd < tiers.length)
                ? tiers[tierOrd] : ProgressionTier.DISCOVERY;
        this.onceAttuned = tag.getBoolean(TAG_ATTUNED);
        this.attunedConstellation = tag.contains(TAG_CONSTELLATION)
                ? new ResourceLocation(tag.getString(TAG_CONSTELLATION))
                : null;
        this.discoveredConstellations.clear();
        if (tag.contains(TAG_DISCOVERED)) {
            ListTag list = tag.getList(TAG_DISCOVERED, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                discoveredConstellations.add(new ResourceLocation(list.getString(i)));
            }
        }
        this.perkPoints = tag.getInt(TAG_PERK_POINTS);
        this.perkExp = tag.getLong(TAG_PERK_EXP);
        this.allocatedPerks.clear();
        if (tag.contains(TAG_ALLOCATED_PERKS)) {
            ListTag perkList = tag.getList(TAG_ALLOCATED_PERKS, Tag.TAG_STRING);
            for (int i = 0; i < perkList.size(); i++) {
                allocatedPerks.add(new ResourceLocation(perkList.getString(i)));
            }
        }
    }

    /**
     * Copy all data from another PlayerProgress instance.
     * Used for syncing or cloning on death/respawn.
     */
    public void copyFrom(@Nonnull PlayerProgress other) {
        this.tierReached = other.tierReached;
        this.onceAttuned = other.onceAttuned;
        this.attunedConstellation = other.attunedConstellation;
        this.discoveredConstellations.clear();
        this.discoveredConstellations.addAll(other.discoveredConstellations);
        this.perkPoints = other.perkPoints;
        this.perkExp = other.perkExp;
        this.allocatedPerks.clear();
        this.allocatedPerks.addAll(other.allocatedPerks);
    }
}
