package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Utility for reading and writing item-level dynamic perk attribute modifiers.
 * These are attribute bonuses engraved onto items via the telescope/engraving system.
 *
 * <p>Full NBT serialization and attribute application are deferred until the perk
 * attribute pipeline is complete. Stubs here are safe: getStaticModifiers returns
 * empty so EngravingEffect.ModifierEffect.supports() always proceeds (no pre-check
 * block), and addModifier is a no-op until Phase 9.</p>
 */
public final class DynamicModifierHelper {

    private DynamicModifierHelper() {}

    /**
     * Returns existing static modifiers on this stack, or empty if none.
     * Stub: returns empty until NBT serialization is implemented.
     */
    @Nonnull
    public static List<PerkAttributeModifier> getStaticModifiers(@Nonnull ItemStack stack) {
        return Collections.emptyList();
    }

    /**
     * Adds a modifier to the stack's NBT. No-op stub until Phase 9.
     */
    public static void addModifier(@Nonnull ItemStack stack, @Nonnull UUID id,
                                   @Nonnull PerkAttributeType type,
                                   @Nonnull ModifierType modifierType, float value) {
        // Deferred until Phase 9 perk attribute pipeline is complete
    }
}
