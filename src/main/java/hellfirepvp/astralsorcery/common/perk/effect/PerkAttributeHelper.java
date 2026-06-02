/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.effect;

import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeLimiter;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Computes the effective value of a perk attribute for a player by
 * collecting all modifiers from allocated perks and applying them
 * in the correct order (ADDITION, ADDED_MULTIPLY, STACKING_MULTIPLY).
 *
 * <p>For attributes that map to vanilla {@link Attribute}s, this helper
 * also applies/removes {@link AttributeModifier}s on the player's
 * attribute instances directly.</p>
 */
public final class PerkAttributeHelper {

    private PerkAttributeHelper() {}

    /**
     * Computes the modified value of a custom (non-vanilla) attribute for a player.
     *
     * @param player    the player
     * @param allocated the player's allocated perk keys
     * @param typeKey   the attribute type to compute
     * @param baseValue the base value before modifiers
     * @return the final modified value
     */
    public static double computeValue(@Nonnull Player player,
                                      @Nonnull Set<ResourceLocation> allocated,
                                      @Nonnull ResourceLocation typeKey,
                                      double baseValue) {
        List<PerkAttributeModifier> modifiers = collectModifiers(player, allocated, typeKey);
        return PerkAttributeLimiter.clamp(typeKey, applyModifiers(baseValue, modifiers));
    }

    /**
     * Collects all modifiers for a specific attribute type from the player's allocated perks.
     */
    @Nonnull
    public static List<PerkAttributeModifier> collectModifiers(@Nonnull Player player,
                                                                @Nonnull Set<ResourceLocation> allocated,
                                                                @Nonnull ResourceLocation typeKey) {
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        List<PerkAttributeModifier> result = new ArrayList<>();
        for (ResourceLocation perkKey : allocated) {
            if (progress != null && progress.isPerkSealed(perkKey)) continue;
            AbstractPerk perk = PerkTree.getPerk(perkKey);
            if (perk == null || !perk.isEnabled()) continue;

            for (PerkAttributeModifier mod : perk.getEffectiveModifiers(player)) {
                if (mod.getAttributeType().equals(typeKey)) {
                    result.add(mod);
                }
            }
        }
        return result;
    }

    /**
     * Applies a list of modifiers to a base value using the standard order:
     * 1. Sum all ADDITION modifiers, add to base
     * 2. Sum all ADDED_MULTIPLY modifiers, multiply (base + additions) by (1 + sum)
     * 3. Apply each STACKING_MULTIPLY modifier sequentially
     *
     * @param baseValue the starting value
     * @param modifiers the modifiers to apply
     * @return the final modified value
     */
    public static double applyModifiers(double baseValue,
                                        @Nonnull List<PerkAttributeModifier> modifiers) {
        double globalFactor;
        try {
            globalFactor = CommonConfig.CONFIG.perkEffectMultiplier.get();
        } catch (IllegalStateException | NullPointerException e) {
            globalFactor = 1.0;
        }
        double additionSum = 0;
        double addedMultiplySum = 0;
        List<Double> stackingMultipliers = new ArrayList<>();

        for (PerkAttributeModifier mod : modifiers) {
            switch (mod.getModifierType()) {
                case ADDITION        -> additionSum      += mod.getValue() * globalFactor;
                case ADDED_MULTIPLY  -> addedMultiplySum += mod.getValue() * globalFactor;
                // Scale the bonus delta so 1.2× at factor 2 becomes 1.4×, not 2.4×
                case STACKING_MULTIPLY -> stackingMultipliers.add(1.0 + (mod.getValue() - 1.0) * globalFactor);
            }
        }

        double result = baseValue + additionSum;
        result *= (1.0 + addedMultiplySum);
        for (double mult : stackingMultipliers) {
            result *= mult;
        }
        return result;
    }

    /**
     * Applies all perk modifiers for vanilla-backed attributes to the player's
     * attribute instances. Called when perks are allocated/deallocated.
     *
     * @param player    the player
     * @param allocated the player's allocated perks
     */
    public static void applyVanillaModifiers(@Nonnull Player player,
                                             @Nonnull Set<ResourceLocation> allocated) {
        // First, remove all existing perk modifiers
        removeAllVanillaModifiers(player);

        // Then apply current ones
        for (PerkAttributeType type : AttributeTypeRegistry.getAllTypes()) {
            if (!type.hasVanillaAttribute()) continue;

            Attribute vanillaAttr = type.getVanillaAttribute();
            if (vanillaAttr == null) continue;

            AttributeInstance instance = player.getAttribute(vanillaAttr);
            if (instance == null) continue;

            List<PerkAttributeModifier> modifiers = collectModifiers(player, allocated, type.getKey());
            for (PerkAttributeModifier mod : modifiers) {
                AttributeModifier vanilla = new AttributeModifier(
                        mod.getId(),
                        "AstralSorcery perk: " + mod.getAttributeType(),
                        mod.getValue(),
                        PerkAttributeType.toVanillaOperation(mod.getModifierType())
                );
                instance.addTransientModifier(vanilla);
            }
        }
    }

    /**
     * Removes all perk-applied vanilla attribute modifiers from a player.
     */
    public static void removeAllVanillaModifiers(@Nonnull Player player) {
        for (PerkAttributeType type : AttributeTypeRegistry.getAllTypes()) {
            if (!type.hasVanillaAttribute()) continue;

            Attribute vanillaAttr = type.getVanillaAttribute();
            if (vanillaAttr == null) continue;

            AttributeInstance instance = player.getAttribute(vanillaAttr);
            if (instance == null) continue;

            // Remove all transient modifiers with "AstralSorcery perk" in the name
            List<AttributeModifier> toRemove = new ArrayList<>();
            for (AttributeModifier existing : instance.getModifiers()) {
                if (existing.getName().startsWith("AstralSorcery perk")) {
                    toRemove.add(existing);
                }
            }
            for (AttributeModifier mod : toRemove) {
                instance.removeModifier(mod);
            }
        }
    }
}
