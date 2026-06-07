/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Defines a type of attribute that perks can modify (armor, damage, health, etc.).
 * Each type has a unique {@link ResourceLocation} key and optionally maps to
 * a vanilla {@link Attribute} for direct application.
 *
 * <p>Instances are registered in {@code AttributeTypeRegistry} and referenced
 * via constants in {@code PerkAttributeTypesAS}.</p>
 *
 * <p>Attribute types that map to vanilla attributes (e.g., armor → ARMOR)
 * will have their modifiers applied/removed from the player's attribute map
 * automatically by the perk effect system. Custom types (e.g., starlight
 * collection bonus) are queried programmatically.</p>
 *
 * <p>1.16 -> 1.20 changes: no significant API changes for Attribute</p>
 */
public class PerkAttributeType {

    @Nonnull
    private final ResourceLocation key;
    @Nullable
    private final Supplier<Attribute> vanillaAttribute;
    private final boolean isMultiplicative;

    /**
     * Creates an attribute type that maps to a vanilla attribute.
     *
     * @param key              unique registry key
     * @param vanillaAttribute supplier for the vanilla attribute (lazy for safe access)
     * @param isMultiplicative whether the base operation should be multiplicative
     */
    public PerkAttributeType(@Nonnull ResourceLocation key,
                             @Nonnull Supplier<Attribute> vanillaAttribute,
                             boolean isMultiplicative) {
        this.key = key;
        this.vanillaAttribute = vanillaAttribute;
        this.isMultiplicative = isMultiplicative;
    }

    /**
     * Creates a custom attribute type with no vanilla backing.
     * Values are computed programmatically when needed.
     *
     * @param key unique registry key
     */
    public PerkAttributeType(@Nonnull ResourceLocation key) {
        this.key = key;
        this.vanillaAttribute = null;
        this.isMultiplicative = false;
    }

    @Nonnull
    public ResourceLocation getKey() {
        return key;
    }

    /**
     * Whether this type maps to a vanilla attribute that can be
     * directly applied to a player's attribute instances.
     */
    public boolean hasVanillaAttribute() {
        return vanillaAttribute != null;
    }

    /**
     * Gets the vanilla attribute this type maps to, or null for custom types.
     */
    @Nullable
    public Attribute getVanillaAttribute() {
        return vanillaAttribute != null ? vanillaAttribute.get() : null;
    }

    /**
     * Whether the base operation for this type is multiplicative.
     * Used to determine default modifier type when no explicit type is given.
     */
    public boolean isMultiplicative() {
        return isMultiplicative;
    }

    /**
     * Converts this type's {@link ModifierType} to the vanilla
     * {@link AttributeModifier.Operation} for direct attribute application.
     */
    @Nonnull
    public static AttributeModifier.Operation toVanillaOperation(@Nonnull ModifierType type) {
        return switch (type) {
            case ADDITION -> AttributeModifier.Operation.ADDITION;
            case ADDED_MULTIPLY -> AttributeModifier.Operation.MULTIPLY_BASE;
            case STACKING_MULTIPLY -> AttributeModifier.Operation.MULTIPLY_TOTAL;
        };
    }

    /**
     * Converts a vanilla operation back to this mod's modifier type.
     */
    @Nonnull
    public static ModifierType fromVanillaOperation(@Nonnull AttributeModifier.Operation op) {
        return switch (op) {
            case ADDITION -> ModifierType.ADDITION;
            case MULTIPLY_BASE -> ModifierType.ADDED_MULTIPLY;
            case MULTIPLY_TOTAL -> ModifierType.STACKING_MULTIPLY;
        };
    }

    /**
     * Returns the unlocalized (i18n translation key) name for this attribute type.
     * Format: {@code "perk.attribute.astralsorcery.<path>"}.
     */
    @Nonnull
    public String getUnlocalizedName() {
        return "perk.attribute." + key.getNamespace() + "." + key.getPath().replace('/', '.');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerkAttributeType other)) return false;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "PerkAttrType[" + key + "]";
    }
}
