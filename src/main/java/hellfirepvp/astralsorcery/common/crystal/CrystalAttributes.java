/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Immutable-ish container of crystal property attributes (property + tier pairs) backed by NBT.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag, ListNBT → ListTag,
 * Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND,
 * ForgeRegistryEntry removed from CrystalProperty.
 * Tooltip rendering implemented: addTooltip overloads with canSee/isDiscovered gating.</p>
 */
public final class CrystalAttributes {

    private static final Random rand = new Random();
    private static final String NBT_KEY = "crystalProperties";

    private LinkedList<Attribute> crystalAttributes = new LinkedList<>();

    private CrystalAttributes() {}

    private CrystalAttributes(Map<CrystalProperty, Integer> crystalAttributes) {
        this.crystalAttributes = new LinkedList<>();
        for (Map.Entry<CrystalProperty, Integer> entry : crystalAttributes.entrySet()) {
            this.crystalAttributes.add(new Attribute(entry.getKey(), entry.getValue()));
        }
        this.crystalAttributes.sort(Comparator.comparing(Attribute::getProperty));
    }

    @Nonnull
    public List<Attribute> getCrystalAttributes() {
        return Collections.unmodifiableList(crystalAttributes);
    }

    @Nullable
    public Attribute getAttribute(CrystalProperty property) {
        for (Attribute attr : this.crystalAttributes) {
            if (attr.getProperty().equals(property)) {
                return attr;
            }
        }
        return null;
    }

    public int getTotalTierLevel() {
        int tier = 0;
        for (Attribute attr : this.getCrystalAttributes()) {
            tier += attr.getTier();
        }
        return tier;
    }

    @Nonnull
    public List<CrystalProperty> getProperties() {
        return this.getCrystalAttributes().stream()
                .map(Attribute::getProperty)
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return this.getTotalTierLevel() <= 0;
    }

    public CrystalAttributes combine(CrystalAttributes other, boolean ignoreTierMax) {
        return combine(other, ignoreTierMax, 1F);
    }

    public CrystalAttributes combine(CrystalAttributes other, boolean ignoreTierMax, float mergeChance) {
        List<Attribute> otherAttributes = other.getCrystalAttributes();
        if (otherAttributes.isEmpty()) {
            return this.copy();
        }
        Builder builder = Builder.newBuilder(ignoreTierMax);
        builder.addAll(this.copy());
        for (Attribute otherAttr : otherAttributes) {
            for (int i = 0; i < otherAttr.getTier(); i++) {
                if (rand.nextFloat() <= mergeChance) {
                    builder.addProperty(otherAttr.getProperty(), 1);
                }
            }
        }
        return builder.build();
    }

    @Nonnull
    public CrystalAttributes modifyLevel(CrystalProperty prop, int change) {
        return modifyLevel(prop, change, false);
    }

    @Nonnull
    public CrystalAttributes modifyLevel(CrystalProperty prop, int change, boolean ignoreTierMax) {
        Attribute existing = getAttribute(prop);
        if (existing != null && change != 0) {
            int newTier = Mth.clamp(existing.getTier() + change, 0,
                    ignoreTierMax ? Integer.MAX_VALUE : prop.getMaxTier());
            if (newTier <= 0) {
                return transform(Function.identity(), new ArrayList<>(), Collections.singletonList(prop));
            } else if (newTier != existing.getTier()) {
                return transform(attribute -> {
                    if (attribute.getProperty().equals(prop)) {
                        attribute.tier = newTier;
                    }
                    return attribute;
                });
            }
        } else if (change > 0) {
            return transform(Function.identity(),
                    Collections.singletonList(new Attribute(prop, Mth.clamp(change, 0, prop.getMaxTier()))),
                    new ArrayList<>());
        }
        return this;
    }

    private CrystalAttributes transform(Function<Attribute, Attribute> modify) {
        return transform(modify, new ArrayList<>(), new ArrayList<>());
    }

    private CrystalAttributes transform(Function<Attribute, Attribute> modify,
                                        Collection<Attribute> additions,
                                        Collection<CrystalProperty> removals) {
        List<Attribute> current = new ArrayList<>();
        for (Attribute attr : this.getCrystalAttributes()) {
            current.add(new Attribute(attr.getProperty(), attr.getTier()));
        }
        List<Attribute> modified = current.stream().map(modify).collect(Collectors.toList());
        for (Attribute added : additions) {
            Attribute existing = null;
            for (Attribute m : modified) {
                if (m.getProperty().equals(added.getProperty())) {
                    existing = m;
                    break;
                }
            }
            if (existing != null) {
                existing.tier += added.getTier();
            } else {
                modified.add(new Attribute(added.getProperty(), added.getTier()));
            }
        }
        modified.removeIf(attr -> removals.contains(attr.getProperty()));
        return new CrystalAttributes(modified.stream()
                .collect(Collectors.toMap(Attribute::getProperty, Attribute::getTier)));
    }

    @Nonnull
    public CrystalAttributes copy() {
        return deserialize(serialize());
    }

    public void store(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            this.store(NBTHelper.getPersistentData(stack));
        }
    }

    public void store(@Nonnull CompoundTag baseTag) {
        baseTag.put(NBT_KEY, this.serialize());
    }

    public static void storeNull(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            storeNull(NBTHelper.getPersistentData(stack));
        }
    }

    public static void storeNull(@Nonnull CompoundTag baseTag) {
        baseTag.remove(NBT_KEY);
    }

    @Nullable
    public static CrystalAttributes getCrystalAttributes(@Nonnull ItemStack stack) {
        return !stack.isEmpty() ? getCrystalAttributes(NBTHelper.getPersistentData(stack)) : null;
    }

    @Nullable
    public static CrystalAttributes getCrystalAttributes(@Nonnull CompoundTag baseTag) {
        if (!baseTag.contains(NBT_KEY)) {
            return null;
        }
        CompoundTag tag = baseTag.getCompound(NBT_KEY);
        if (tag.size() == 0) {
            return null;
        }
        return deserialize(tag);
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Attribute attr : crystalAttributes) {
            list.add(attr.serialize());
        }
        tag.put("attributes", list);
        return tag;
    }

    @Nonnull
    public static CrystalAttributes deserialize(@Nonnull CompoundTag tag) {
        CrystalAttributes attributes = new CrystalAttributes();
        ListTag list = tag.getList("attributes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            Attribute attr = Attribute.deserialize(list.getCompound(i));
            if (attr != null) {
                attributes.crystalAttributes.add(attr);
            }
        }
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrystalAttributes that = (CrystalAttributes) o;
        return Objects.equals(crystalAttributes, that.crystalAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crystalAttributes);
    }

    @OnlyIn(Dist.CLIENT)
    public TooltipResult addTooltip(List<Component> tooltip) {
        return addTooltip(tooltip, PlayerProgressManager.getClientProgress(), new CalculationContext.Builder().build());
    }

    @OnlyIn(Dist.CLIENT)
    public TooltipResult addTooltip(List<Component> tooltip, PlayerProgress progress) {
        return addTooltip(tooltip, progress, new CalculationContext.Builder().build());
    }

    @OnlyIn(Dist.CLIENT)
    public TooltipResult addTooltip(List<Component> tooltip, CalculationContext ctx) {
        return addTooltip(tooltip, PlayerProgressManager.getClientProgress(), ctx);
    }

    @OnlyIn(Dist.CLIENT)
    private TooltipResult addTooltip(List<Component> tooltip, PlayerProgress progress, CalculationContext ctx) {
        boolean missing = false, addedAtLeastOne = false;
        for (Attribute attr : this.getCrystalAttributes()) {
            if (attr.getTier() > 0) {
                CrystalProperty prop = attr.getProperty();
                if (!prop.hasUsageFor(ctx) && !ctx.isEmpty()) continue;
                if (!prop.canSee(progress) || !attr.isDiscovered()) {
                    missing = true;
                } else {
                    Component enchLevel = Component.translatable("enchantment.level." + attr.getTier())
                            .withStyle(ChatFormatting.GOLD);
                    Component propName = prop.getName(attr.getTier())
                            .copy().withStyle(ChatFormatting.GRAY);
                    tooltip.add(propName.copy().append(Component.literal(" ")).append(enchLevel));
                    addedAtLeastOne = true;
                }
            }
        }
        if (missing) {
            tooltip.add(Component.translatable("astralsorcery.progress.missing.knowledge")
                    .withStyle(ChatFormatting.GRAY));
        }
        return missing && !addedAtLeastOne ? TooltipResult.ALL_MISSING
                : missing ? TooltipResult.ADDED_ALL_WITH_MISSING
                : TooltipResult.ADDED_ALL;
    }

    public enum TooltipResult {
        ADDED_ALL,
        ADDED_ALL_WITH_MISSING,
        ALL_MISSING
    }

    public static class Builder {

        private final Map<CrystalProperty, Integer> properties = new LinkedHashMap<>();
        private final boolean ignoreTierCap;

        private Builder(boolean ignoreTierCap) {
            this.ignoreTierCap = ignoreTierCap;
        }

        public static Builder newBuilder(boolean ignoreTierCap) {
            return new Builder(ignoreTierCap);
        }

        @Nonnull
        public Builder addAll(@Nonnull CrystalAttributes other) {
            for (Attribute attr : other.getCrystalAttributes()) {
                CrystalProperty property = attr.getProperty();
                int cTier = this.properties.getOrDefault(property, 0);
                cTier = Mth.clamp(cTier + attr.getTier(), 0,
                        this.ignoreTierCap ? Integer.MAX_VALUE : property.getMaxTier());
                this.properties.put(property, cTier);
            }
            return this;
        }

        @Nonnull
        public Builder addProperty(@Nonnull CrystalProperty property, int tier) {
            int cTier = this.properties.getOrDefault(property, 0);
            cTier = Mth.clamp(cTier + tier, 0,
                    this.ignoreTierCap ? Integer.MAX_VALUE : property.getMaxTier());
            this.properties.remove(property);
            if (cTier > 0) {
                this.properties.put(property, cTier);
            }
            return this;
        }

        public int getPropertyLvl(@Nonnull CrystalProperty property, int defaultValue) {
            return this.properties.getOrDefault(property, defaultValue);
        }

        @Nonnull
        public List<CrystalProperty> getProperties() {
            return new ArrayList<>(this.properties.keySet());
        }

        @Nonnull
        public CrystalAttributes buildAverage(int count) {
            Map<CrystalProperty, Integer> average = new LinkedHashMap<>();
            for (CrystalProperty prop : this.properties.keySet()) {
                int newLevel = Mth.ceil(this.properties.getOrDefault(prop, 0) / (float) count);
                if (newLevel > 0) {
                    average.put(prop, newLevel);
                }
            }
            return new CrystalAttributes(average);
        }

        @Nonnull
        public CrystalAttributes build() {
            return new CrystalAttributes(properties);
        }
    }

    public static class Attribute {

        private int tier;
        private final CrystalProperty property;

        private Attribute(@Nonnull CrystalProperty property, int tier) {
            this.tier = tier;
            this.property = property;
        }

        @Nonnull
        public CrystalProperty getProperty() {
            return property;
        }

        public int getTier() {
            return this.tier;
        }

        public boolean isDiscovered() {
            return true;
        }

        @Nonnull
        CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("property", property.getRegistryName().toString());
            tag.putInt("pLevel", tier);
            return tag;
        }

        @Nullable
        static Attribute deserialize(@Nonnull CompoundTag tag) {
            ResourceLocation key = new ResourceLocation(tag.getString("property"));
            CrystalProperty prop = CrystalPropertyRegistry.INSTANCE.getProperty(key);
            if (prop == null) {
                return null;
            }
            int tier = tag.getInt("pLevel");
            return new Attribute(prop, tier);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Attribute that = (Attribute) o;
            return tier == that.tier && Objects.equals(property, that.property);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tier, property);
        }
    }
}
