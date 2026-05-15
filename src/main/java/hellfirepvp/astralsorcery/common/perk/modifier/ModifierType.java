/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.modifier;

import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

/**
 * Defines how a perk attribute modifier is applied to the base value.
 * Matches Minecraft's {@code AttributeModifier.Operation} semantics:
 *
 * <ol>
 *   <li>{@link #ADDITION} — added to the base value</li>
 *   <li>{@link #ADDED_MULTIPLY} — multiplied by sum of all ADDED_MULTIPLY modifiers + 1,
 *       applied after all additions</li>
 *   <li>{@link #STACKING_MULTIPLY} — each modifier multiplies the running total independently,
 *       applied last</li>
 * </ol>
 *
 * <p>Application order: base + ADDITION → * (1 + sum(ADDED_MULTIPLY)) → * each STACKING_MULTIPLY</p>
 */
public enum ModifierType implements StringRepresentable {

    /**
     * Flat addition to the base value.
     * Example: +2.0 armor points.
     */
    ADDITION("addition"),

    /**
     * Additive percentage increase. All modifiers of this type are summed,
     * then the base (after additions) is multiplied by (1 + sum).
     * Example: +10% damage = 0.10 value.
     */
    ADDED_MULTIPLY("added_multiply"),

    /**
     * Stacking multiplicative modifier. Each modifier multiplies the
     * running total independently.
     * Example: 1.5x crit multiplier.
     */
    STACKING_MULTIPLY("stacking_multiply");

    private final String serializedName;

    ModifierType(@Nonnull String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    @Nonnull
    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Resolves a ModifierType from its serialized name, defaulting to ADDITION if unknown.
     */
    @Nonnull
    public static ModifierType fromString(@Nonnull String name) {
        for (ModifierType type : values()) {
            if (type.serializedName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return ADDITION;
    }
}
