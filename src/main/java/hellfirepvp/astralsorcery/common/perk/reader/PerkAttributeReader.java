package hellfirepvp.astralsorcery.common.perk.reader;

import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Set;

/**
 * Computes and formats the display value of a perk attribute for the perk tree UI.
 * Each attribute type registers exactly one reader via {@link PerkAttributeType}.
 *
 * <p>In 1.16 this class extended {@code ForgeRegistryEntry<PerkAttributeReader>}.
 * In 1.20, Forge's registry entry system changed; we use a simple map lookup
 * keyed by {@link ResourceLocation} instead.</p>
 *
 * <p>1.16 → 1.20: ForgeRegistryEntry removed; Player replaces PlayerEntity;
 * PerkAttributeMap replaced by direct {@link PerkAttributeHelper} calls.</p>
 */
public abstract class PerkAttributeReader {

    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    @Nonnull
    private final PerkAttributeType type;

    protected PerkAttributeReader(@Nonnull PerkAttributeType type) {
        this.type = type;
    }

    @Nonnull
    public final PerkAttributeType getType() {
        return type;
    }

    /**
     * Builds a display-ready statistic for the given player's current attribute value.
     *
     * @param player   the player to query
     * @param allocated the player's currently allocated perk keys
     * @return the formatted statistic, or null if the attribute is unavailable
     */
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public abstract PerkStatistic getStatistics(@Nonnull Player player,
                                                @Nonnull Set<ResourceLocation> allocated);

    /**
     * Returns the base value of this attribute before any perk modifiers.
     *
     * @param player   the player
     * @param side     the logical side
     * @return the default (unmodified) value
     */
    public abstract double getDefaultValue(@Nonnull Player player, @Nonnull LogicalSide side);

    /**
     * Returns the total modifier value for a specific modifier mode.
     *
     * @param player    the player
     * @param allocated the player's allocated perk keys
     * @param side      the logical side
     * @param mode      the modifier type to query
     * @return the combined modifier value for the given mode
     */
    public abstract double getModifierValueForMode(@Nonnull Player player,
                                                   @Nonnull Set<ResourceLocation> allocated,
                                                   @Nonnull LogicalSide side,
                                                   @Nonnull ModifierType mode);

    /**
     * Formats a decimal number to two decimal places.
     */
    @Nonnull
    public static String formatDecimal(double decimal) {
        return DECIMAL_FORMAT.format(decimal);
    }

    /**
     * Computes the total modifier for this attribute type using {@link PerkAttributeHelper}.
     */
    protected double computeModifier(@Nonnull Player player,
                                     @Nonnull Set<ResourceLocation> allocated) {
        return PerkAttributeHelper.computeValue(player, allocated, type.getKey(), 1.0) - 1.0;
    }
}
