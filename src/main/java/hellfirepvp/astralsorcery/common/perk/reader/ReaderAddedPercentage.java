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
 * Reader for percentage-based perk attributes (e.g., +15% crit chance).
 * Displays the modifier as a signed percentage relative to the default.
 *
 * <p>1.16 → 1.20: PerkAttributeMap replaced by {@link PerkAttributeHelper} calls;
 * I18n.format replaced by Component translation; PerkAttributeLimiter deferred.</p>
 */
public class ReaderAddedPercentage extends PerkAttributeReader {

    protected float defaultValue;

    public ReaderAddedPercentage(@Nonnull PerkAttributeType attribute) {
        super(attribute);
        this.defaultValue = attribute.isMultiplicative() ? 1F : 0F;
    }

    @SuppressWarnings("unchecked")
    public <T extends ReaderAddedPercentage> T setDefaultValue(float defaultValue) {
        if (!getType().isMultiplicative()) {
            this.defaultValue = defaultValue;
        }
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
        double value = PerkAttributeHelper.computeValue(player, allocated, getType().getKey(), 1.0) - 1.0;
        if (mode == ModifierType.ADDITION) {
            value /= 100.0;
            value += 1;
        }
        return value;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public PerkStatistic getStatistics(@Nonnull Player player,
                                       @Nonnull Set<ResourceLocation> allocated) {
        double computed = PerkAttributeHelper.computeValue(player, allocated,
                getType().getKey(), (double) defaultValue);
        if (getType().isMultiplicative()) {
            computed -= 1.0;
        }
        String strOut = (computed >= 0 ? "+" : "") + formatDecimal(computed * 100.0) + "%";
        return new PerkStatistic(getType(), strOut, "", "");
    }
}
