/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A position in the perk tree UI. Each perk is placed at an (x, y) coordinate
 * on the perk tree visualization. The coordinate space is abstract — the
 * client renderer maps these to screen space.
 *
 * <p>Points are immutable once created.</p>
 */
public class PerkTreePoint {

    @Nonnull
    private final ResourceLocation perkKey;
    private final int offsetX;
    private final int offsetY;

    /**
     * @param perkKey the perk this point represents
     * @param offsetX horizontal position in the tree
     * @param offsetY vertical position in the tree
     */
    public PerkTreePoint(@Nonnull ResourceLocation perkKey, int offsetX, int offsetY) {
        this.perkKey = perkKey;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Nonnull
    public ResourceLocation getPerkKey() {
        return perkKey;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Gets the squared distance from this point to another in tree space.
     */
    public double distanceSqTo(@Nonnull PerkTreePoint other) {
        double dx = this.offsetX - other.offsetX;
        double dy = this.offsetY - other.offsetY;
        return dx * dx + dy * dy;
    }

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("perkKey", perkKey.toString());
        tag.putInt("x", offsetX);
        tag.putInt("y", offsetY);
        return tag;
    }

    @Nonnull
    public static PerkTreePoint readFromNBT(@Nonnull CompoundTag tag) {
        ResourceLocation key = new ResourceLocation(tag.getString("perkKey"));
        int x = tag.getInt("x");
        int y = tag.getInt("y");
        return new PerkTreePoint(key, x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerkTreePoint other)) return false;
        return perkKey.equals(other.perkKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(perkKey);
    }

    @Override
    public String toString() {
        return "TreePoint[" + perkKey + " @ (" + offsetX + ", " + offsetY + ")]";
    }
}
