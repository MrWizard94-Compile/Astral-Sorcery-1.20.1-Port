/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.modifier;

import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

/**
 * A single perk attribute modifier that adjusts one attribute type by
 * a specified value using a specified operation.
 *
 * <p>Each modifier has a stable UUID used as the key when applying
 * to vanilla {@code AttributeInstance}s. Two modifiers with the same
 * UUID will overwrite each other — this is intentional for perk stacking.</p>
 *
 * <p>1.16 -> 1.20 changes: CompoundNBT -> CompoundTag</p>
 */
public class PerkAttributeModifier {

    @Nonnull
    private final UUID id;
    @Nonnull
    private final ResourceLocation attributeType;
    @Nonnull
    private final ModifierType modifierType;
    private final double value;

    /**
     * Creates a modifier with a random UUID.
     */
    public PerkAttributeModifier(@Nonnull ResourceLocation attributeType,
                                 @Nonnull ModifierType modifierType,
                                 double value) {
        this(UUID.randomUUID(), attributeType, modifierType, value);
    }

    /**
     * Creates a modifier with a specific UUID (for deterministic application).
     */
    public PerkAttributeModifier(@Nonnull UUID id,
                                 @Nonnull ResourceLocation attributeType,
                                 @Nonnull ModifierType modifierType,
                                 double value) {
        this.id = id;
        this.attributeType = attributeType;
        this.modifierType = modifierType;
        this.value = value;
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    @Nonnull
    public ResourceLocation getAttributeType() {
        return attributeType;
    }

    @Nonnull
    public ModifierType getModifierType() {
        return modifierType;
    }

    public double getValue() {
        return value;
    }

    /**
     * Applies this modifier's value to a running total using the modifier's operation.
     * For ADDITION and ADDED_MULTIPLY, this returns the raw value.
     * For STACKING_MULTIPLY, this returns the multiplied result.
     *
     * <p>The caller is responsible for aggregating ADDITION and ADDED_MULTIPLY
     * modifiers before applying them to the base value.</p>
     *
     * @param currentValue the value to modify (for STACKING_MULTIPLY)
     * @return the modifier contribution or the new value
     */
    public double apply(double currentValue) {
        return switch (modifierType) {
            case ADDITION -> value;
            case ADDED_MULTIPLY -> value;
            case STACKING_MULTIPLY -> currentValue * value;
        };
    }

    // ---- Display ----

    /**
     * Get a human-readable description of this modifier for tooltip display.
     * Format: "+1.0 Attack Damage" or "x1.05 Movement Speed"
     */
    @Nonnull
    public Component getDescription() {
        String prefix = switch (modifierType) {
            case ADDITION -> value >= 0 ? "+" : "";
            case ADDED_MULTIPLY -> value >= 0 ? "+" : "";
            case STACKING_MULTIPLY -> "x";
        };

        String valueStr = switch (modifierType) {
            case ADDITION -> String.format("%.1f", value);
            case ADDED_MULTIPLY -> String.format("%.0f%%", value * 100);
            case STACKING_MULTIPLY -> String.format("%.2f", value);
        };

        String attrName = attributeType.getPath().replace("attr_type_", "").replace('_', ' ');
        // Capitalize first letter of each word
        String[] words = attrName.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        String displayAttr = sb.toString().trim();

        ChatFormatting color = value >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        MutableComponent desc = Component.literal(prefix + valueStr + " " + displayAttr);
        return desc.withStyle(color);
    }

    // ---- NBT serialization ----

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("attributeType", attributeType.toString());
        tag.putString("modifierType", modifierType.getSerializedName());
        tag.putDouble("value", value);
        return tag;
    }

    @Nonnull
    public static PerkAttributeModifier readFromNBT(@Nonnull CompoundTag tag) {
        UUID id = tag.getUUID("id");
        ResourceLocation attrType = new ResourceLocation(tag.getString("attributeType"));
        ModifierType modType = ModifierType.fromString(tag.getString("modifierType"));
        double value = tag.getDouble("value");
        return new PerkAttributeModifier(id, attrType, modType, value);
    }

    /**
     * Creates a scaled copy of this modifier with the given multiplier.
     * The UUID is preserved so the scaled version replaces the original.
     */
    @Nonnull
    public PerkAttributeModifier withMultiplier(double multiplier) {
        return new PerkAttributeModifier(id, attributeType, modifierType, value * multiplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerkAttributeModifier other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("PerkMod[%s %s %.3f (%s)]",
                modifierType.getSerializedName(), attributeType, value, id);
    }
}
