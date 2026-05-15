/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Base class for all perks in the Astral Sorcery perk tree.
 *
 * <p>Each perk has:
 * <ul>
 *   <li>A unique {@link ResourceLocation} key</li>
 *   <li>A position in the tree ({@link PerkTreePoint})</li>
 *   <li>A category determining its visual style and importance</li>
 *   <li>A list of attribute modifiers applied when allocated</li>
 *   <li>Connections to adjacent perks in the tree</li>
 * </ul>
 *
 * <p>Subclasses override {@link #onAllocate(Player)}, {@link #onDeallocate(Player)},
 * and optionally {@link #onPlayerTick(Player)} for custom behavior.</p>
 *
 * <p>1.16 -> 1.20 changes: PlayerEntity -> Player, CompoundNBT -> CompoundTag</p>
 */
public abstract class AbstractPerk {

    @Nonnull
    private final ResourceLocation key;
    @Nonnull
    private final PerkTreePoint treePoint;
    @Nonnull
    private final PerkCategory category;

    @Nonnull
    private final List<PerkAttributeModifier> modifiers = new ArrayList<>();
    @Nonnull
    private final Set<ResourceLocation> adjacentPerks = new LinkedHashSet<>();

    @Nullable
    private ResourceLocation requiredConstellation = null;

    private boolean enabled = true;

    protected AbstractPerk(@Nonnull ResourceLocation key,
                           int treeX, int treeY,
                           @Nonnull PerkCategory category) {
        this.key = key;
        this.treePoint = new PerkTreePoint(key, treeX, treeY);
        this.category = category;
    }

    // ========================================================================
    // Identity & position
    // ========================================================================

    @Nonnull
    public ResourceLocation getKey() {
        return key;
    }

    @Nonnull
    public PerkTreePoint getTreePoint() {
        return treePoint;
    }

    @Nonnull
    public PerkCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // ========================================================================
    // Constellation requirement
    // ========================================================================

    @Nullable
    public ResourceLocation getRequiredConstellation() {
        return requiredConstellation;
    }

    /**
     * Sets a constellation requirement for this perk.
     * The player must be attuned to this constellation to allocate it.
     *
     * @param constellation constellation key, or null for no requirement
     * @return this perk (for builder chaining)
     */
    @Nonnull
    public AbstractPerk setRequiredConstellation(@Nullable ResourceLocation constellation) {
        this.requiredConstellation = constellation;
        return this;
    }

    // ========================================================================
    // Modifiers
    // ========================================================================

    /**
     * Adds an attribute modifier that is applied when this perk is allocated.
     *
     * @param modifier the modifier to add
     * @return this perk (for builder chaining)
     */
    @Nonnull
    public AbstractPerk addModifier(@Nonnull PerkAttributeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    /**
     * Gets all attribute modifiers for this perk.
     */
    @Nonnull
    public List<PerkAttributeModifier> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    /**
     * Gets modifiers scaled for a specific player (e.g., based on perk level).
     * Override in subclasses for dynamic modifier scaling.
     *
     * @param player the player the modifiers will be applied to
     * @return the effective modifiers for the player
     */
    @Nonnull
    public List<PerkAttributeModifier> getEffectiveModifiers(@Nonnull Player player) {
        return getModifiers();
    }

    // ========================================================================
    // Adjacency (tree edges)
    // ========================================================================

    /**
     * Adds an adjacent perk connection. Tree edges are bidirectional —
     * call on both perks when building the tree.
     *
     * @param adjacentKey the key of the adjacent perk
     * @return this perk (for builder chaining)
     */
    @Nonnull
    public AbstractPerk addAdjacent(@Nonnull ResourceLocation adjacentKey) {
        this.adjacentPerks.add(adjacentKey);
        return this;
    }

    @Nonnull
    public Set<ResourceLocation> getAdjacentPerks() {
        return Collections.unmodifiableSet(adjacentPerks);
    }

    /**
     * Whether this perk is adjacent to the perk with the given key.
     */
    public boolean isAdjacentTo(@Nonnull ResourceLocation other) {
        return adjacentPerks.contains(other);
    }

    // ========================================================================
    // Lifecycle callbacks
    // ========================================================================

    /**
     * Called when a player allocates (unlocks) this perk.
     * Override for custom allocation effects.
     *
     * @param player the player allocating the perk
     */
    public void onAllocate(@Nonnull Player player) {}

    /**
     * Called when a player deallocates (refunds) this perk.
     * Override for custom deallocation cleanup.
     *
     * @param player the player deallocating the perk
     */
    public void onDeallocate(@Nonnull Player player) {}

    /**
     * Called every server tick for each player who has this perk allocated.
     * Override for perks with ongoing effects (regeneration, etc.).
     *
     * @param player the player with this perk
     */
    public void onPlayerTick(@Nonnull Player player) {}

    /**
     * Whether this perk ticks (has an onPlayerTick override).
     * Override to return true if the perk needs per-tick processing.
     */
    public boolean hasTickEffect() {
        return false;
    }

    // ========================================================================
    // NBT serialization (for perk tree data, not player allocation)
    // ========================================================================

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key.toString());
        tag.put("treePoint", treePoint.writeToNBT());
        tag.putString("category", category.getSerializedName());
        tag.putBoolean("enabled", enabled);

        if (requiredConstellation != null) {
            tag.putString("requiredConstellation", requiredConstellation.toString());
        }

        ListTag modList = new ListTag();
        for (PerkAttributeModifier mod : modifiers) {
            modList.add(mod.writeToNBT());
        }
        tag.put("modifiers", modList);

        ListTag adjList = new ListTag();
        for (ResourceLocation adj : adjacentPerks) {
            CompoundTag adjTag = new CompoundTag();
            adjTag.putString("key", adj.toString());
            adjList.add(adjTag);
        }
        tag.put("adjacent", adjList);

        return tag;
    }

    /**
     * Reads perk data from NBT into this perk instance.
     * Used when loading the perk tree from saved data.
     */
    public void readFromNBT(@Nonnull CompoundTag tag) {
        this.enabled = tag.getBoolean("enabled");

        if (tag.contains("requiredConstellation")) {
            this.requiredConstellation = new ResourceLocation(tag.getString("requiredConstellation"));
        }

        this.modifiers.clear();
        if (tag.contains("modifiers")) {
            ListTag modList = tag.getList("modifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < modList.size(); i++) {
                modifiers.add(PerkAttributeModifier.readFromNBT(modList.getCompound(i)));
            }
        }

        this.adjacentPerks.clear();
        if (tag.contains("adjacent")) {
            ListTag adjList = tag.getList("adjacent", Tag.TAG_COMPOUND);
            for (int i = 0; i < adjList.size(); i++) {
                adjacentPerks.add(new ResourceLocation(adjList.getCompound(i).getString("key")));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPerk other)) return false;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + key + " (" + category + ")]";
    }

    // ========================================================================
    // Perk categories
    // ========================================================================

    /**
     * Visual/gameplay category for a perk node in the tree.
     */
    public enum PerkCategory implements net.minecraft.util.StringRepresentable {

        /** Root node for a constellation. One per constellation. */
        ROOT("root"),

        /** Key milestone perk — major benefits, limited count. */
        KEY("key"),

        /** Major stat perk — significant bonuses. */
        MAJOR("major"),

        /** Small stat perk — minor bonuses, many available. */
        SMALL("small"),

        /** Focus perk — constellation-specific bonuses. */
        FOCUS("focus"),

        /** Gem socket perk — accepts a perk gem for custom bonuses. */
        GEM_SOCKET("gem_socket");

        private final String serializedName;

        PerkCategory(@Nonnull String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return serializedName;
        }

        @Nonnull
        public static PerkCategory fromString(@Nonnull String name) {
            for (PerkCategory cat : values()) {
                if (cat.serializedName.equalsIgnoreCase(name)) {
                    return cat;
                }
            }
            return SMALL;
        }
    }
}
