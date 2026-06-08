/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.engraving;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.List;

/**
 * Represents a star map engraved onto a glass lens or similar item.
 *
 * <p>The star map tracks which constellations were drawn on it and their
 * overlap-based distribution percentages. Constellations with more star
 * intersections score higher and apply stronger engraving effects.</p>
 *
 * <p>1.16 → 1.20 changes:
 * CompoundNBT → CompoundTag, ListNBT → ListTag,
 * Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND,
 * DayTimeHelper.getCurrentDaytimeDistribution → CelestialHandler.getTimeOfDayFactor,
 * ResourceLocation import moved to net.minecraft.resources,
 * Mth.clamp used instead of MathHelper.clamp</p>
 */
public class EngravedStarMap {

    private static final Random rand = new Random();

    private final Map<ResourceLocation, Float> distributions;
    private final List<DrawnConstellation> drawInformation;

    private EngravedStarMap(Map<ResourceLocation, Float> distributions,
                            List<DrawnConstellation> drawnConstellations) {
        this.distributions = distributions;
        this.drawInformation = drawnConstellations;
    }

    /**
     * Builds a star map from a set of drawn constellations and the current
     * world time. The overlap between star positions determines each
     * constellation's distribution score.
     */
    @Nonnull
    public static EngravedStarMap buildStarMap(@Nonnull Level level,
                                               @Nonnull List<DrawnConstellation> constellations) {
        float nightPerc = CelestialHandler.getTimeOfDayFactor(level);

        Map<DrawnConstellation, List<Rectangle.Double>> cstCoordinates = new HashMap<>();
        for (DrawnConstellation drawnCst : constellations) {
            cstCoordinates.put(drawnCst, createConstellationOffsets(drawnCst));
        }

        Map<ResourceLocation, Float> distributionMap = new HashMap<>();
        for (Map.Entry<DrawnConstellation, List<Rectangle.Double>> drawnEntry : cstCoordinates.entrySet()) {
            DrawnConstellation drawn = drawnEntry.getKey();
            List<Rectangle.Double> positions = drawnEntry.getValue();
            Set<Rectangle.Double> foundPositions = new HashSet<>();

            for (Map.Entry<DrawnConstellation, List<Rectangle.Double>> otherEntry : cstCoordinates.entrySet()) {
                DrawnConstellation otherCst = otherEntry.getKey();
                if (drawn.equals(otherCst) ||
                        drawn.getConstellation().equals(otherCst.getConstellation())) {
                    continue;
                }
                List<Rectangle.Double> otherPositions = otherEntry.getValue();
                for (Rectangle.Double starPos : positions) {
                    for (Rectangle.Double otherStarPos : otherPositions) {
                        if (starPos.intersects(otherStarPos)) {
                            foundPositions.add(starPos);
                        }
                    }
                }
            }

            IConstellation drawnConstellation = drawn.getConstellation();
            float percent = 0.1F + 0.9F * Mth.clamp(
                    ((foundPositions.size() * 1.5F) / positions.size()) * nightPerc, 0F, 1F);
            float existingPercent = distributionMap.getOrDefault(
                    drawnConstellation.getRegistryName(), 0.1F);
            if (percent >= existingPercent) {
                distributionMap.put(drawnConstellation.getRegistryName(), percent);
            }
        }
        return new EngravedStarMap(distributionMap, constellations);
    }

    private static List<Rectangle.Double> createConstellationOffsets(DrawnConstellation cst) {
        float width = DrawnConstellation.CONSTELLATION_STAR_SIZE;
        List<Rectangle.Double> positions = new ArrayList<>();
        for (StarLocation star : cst.getConstellation().getStars()) {
            double starX = star.x * DrawnConstellation.CONSTELLATION_SIZE_PART
                    + cst.getPoint().getX()
                    - DrawnConstellation.CONSTELLATION_DRAW_SIZE / 2F
                    - (width / 2F);
            double starY = star.y * DrawnConstellation.CONSTELLATION_SIZE_PART
                    + cst.getPoint().getY()
                    - DrawnConstellation.CONSTELLATION_DRAW_SIZE / 2F
                    - (width / 2F);
            positions.add(new Rectangle.Double(starX, starY, width, width));
        }
        return positions;
    }

    /**
     * Returns true if any constellation on this map has an engraving effect
     * applicable to the given item stack.
     */
    public boolean canAffect(@Nonnull ItemStack stack) {
        for (ResourceLocation key : getConstellationKeys()) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst == null) continue;
            EngravingEffect effect = cst.getEngravingEffect();
            if (effect != null && !effect.getApplicableEffects(stack).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies all eligible engraving effects from each constellation on this map
     * to the given item stack. Each constellation's distribution score scales
     * the effect strength.
     */
    @Nonnull
    public ItemStack applyEffects(@Nonnull ItemStack stack) {
        List<EngravingEffect.ApplicableEffect> incompatible = new ArrayList<>();
        List<Map.Entry<EngravingEffect.ApplicableEffect, Float>> toApply = new ArrayList<>();

        for (ResourceLocation key : getConstellationKeys()) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst == null) continue;
            EngravingEffect effect = cst.getEngravingEffect();
            if (effect == null) continue;
            float dist = distributions.getOrDefault(key, 0F);
            for (EngravingEffect.ApplicableEffect applicable : effect.getApplicableEffects(stack)) {
                if (applicable instanceof EngravingEffect.EnchantmentEffect ench
                        && !ench.isIgnoreCompatibility()) {
                    // Collect non-compat enchantments; apply only the best one later
                    incompatible.add(applicable);
                } else {
                    toApply.add(Map.entry(applicable, dist));
                }
            }
        }

        // Apply exactly one non-compatibility-ignored enchantment (highest distribution wins)
        if (!incompatible.isEmpty()) {
            EngravingEffect.ApplicableEffect best = incompatible.get(0);
            float bestDist = 0F;
            for (ResourceLocation key : getConstellationKeys()) {
                float d = distributions.getOrDefault(key, 0F);
                if (d >= bestDist) { bestDist = d; best = incompatible.get(0); }
            }
            toApply.add(Map.entry(best, bestDist));
        }

        for (Map.Entry<EngravingEffect.ApplicableEffect, Float> entry : toApply) {
            stack = entry.getKey().apply(stack, entry.getValue(), rand);
        }
        return stack;
    }

    @Nonnull
    public Collection<DrawnConstellation> getDrawnConstellations() {
        return Collections.unmodifiableCollection(drawInformation);
    }

    @Nonnull
    public Collection<ResourceLocation> getConstellationKeys() {
        return Collections.unmodifiableCollection(distributions.keySet());
    }

    public float getDistribution(@Nonnull IConstellation cst) {
        return distributions.getOrDefault(cst.getRegistryName(), 0F);
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();

        ListTag list = new ListTag();
        distributions.forEach((constellationKey, percent) -> {
            CompoundTag cstTag = new CompoundTag();
            NBTHelper.setResourceLocation(cstTag, "cst", constellationKey);
            cstTag.putFloat("percent", percent);
            list.add(cstTag);
        });
        tag.put("distributions", list);

        ListTag listDrawn = new ListTag();
        drawInformation.forEach(drawCst -> {
            CompoundTag cstTag = new CompoundTag();
            NBTHelper.setResourceLocation(cstTag, "cst",
                    drawCst.getConstellation().getRegistryName());
            cstTag.putInt("x", drawCst.getPoint().x);
            cstTag.putInt("y", drawCst.getPoint().y);
            listDrawn.add(cstTag);
        });
        tag.put("drawInformation", listDrawn);

        return tag;
    }

    @Nonnull
    public static EngravedStarMap deserialize(@Nonnull CompoundTag tag) {
        Map<ResourceLocation, Float> distributionMap = new HashMap<>();
        ListTag list = tag.getList("distributions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag cstTag = list.getCompound(i);
            ResourceLocation key = NBTHelper.getResourceLocation(cstTag, "cst");
            float percent = cstTag.getFloat("percent");
            if (key != null && percent > 0) {
                distributionMap.put(key, percent);
            }
        }

        List<DrawnConstellation> drawnConstellations = new ArrayList<>();
        ListTag listDrawn = tag.getList("drawInformation", Tag.TAG_COMPOUND);
        for (int i = 0; i < listDrawn.size(); i++) {
            CompoundTag cstTag = listDrawn.getCompound(i);
            ResourceLocation cstKey = NBTHelper.getResourceLocation(cstTag, "cst");
            if (cstKey == null) {
                continue;
            }
            IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
            if (cst == null) {
                continue;
            }
            Point offset = new Point(cstTag.getInt("x"), cstTag.getInt("y"));
            drawnConstellations.add(new DrawnConstellation(offset, cst));
        }
        return new EngravedStarMap(distributionMap, drawnConstellations);
    }
}
