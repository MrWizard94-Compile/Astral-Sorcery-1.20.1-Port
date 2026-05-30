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
import java.util.Set;

/**
 * Reader for flat (absolute) perk attributes (e.g., +3 armor).
 * Displays the total value with a sign prefix.
 *
 * <p>1.16 → 1.20: PerkAttributeMap replaced by {@link PerkAttributeHelper};
 * PerkAttributeLimiter deferred; I18n replaced by Component.</p>
 */
public class ReaderFlatAttribute extends PerkAttributeReader {

    private final double defaultValue;
    private boolean formatAsDecimal = false;

    public ReaderFlatAttribute(@Nonnull PerkAttributeType type, double defaultValue) {
        super(type);
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T extends ReaderFlatAttribute> T formatAsDecimal() {
        this.formatAsDecimal = true;
        return (T) this;
    }

    @Override
    public double getDefaultValue(@Nonnull Player player, @Nonnull LogicalSide side) {
        return defaultValue;
    }

    @Override
    public double getModifierValueForMode(@Nonnull Player player,
                                          @Nonnull Set<ResourceLocation> allocated,
                                          @Nonnull LogicalSide side,
                                          @Nonnull ModifierType mode) {
        return PerkAttributeHelper.computeValue(player, allocated, getType().getKey(), defaultValue) - defaultValue;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public PerkStatistic getStatistics(@Nonnull Player player,
                                       @Nonnull Set<ResourceLocation> allocated) {
        double value = PerkAttributeHelper.computeValue(player, allocated, getType().getKey(), defaultValue);
        String display = formatForDisplay(value);
        return new PerkStatistic(getType(), display, "", "");
    }

    private String formatForDisplay(double value) {
        if (formatAsDecimal) {
            return (value >= 0 ? "+" : "") + formatDecimal(value);
        }
        int intVal = (int) Math.round(value);
        return (intVal >= 0 ? "+" : "") + intVal;
    }
}
