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
 * Reader for multiplicative percentage attributes that are displayed
 * as raw percentages (e.g., "135%" total armor multiplier).
 *
 * <p>Unlike {@link ReaderAddedPercentage}, this shows the full computed
 * value rather than the delta from default.</p>
 *
 * <p>1.16 → 1.20: PerkAttributeMap replaced by {@link PerkAttributeHelper}.</p>
 */
public class ReaderPercentageAttribute extends PerkAttributeReader {

    private double defaultValue;

    public ReaderPercentageAttribute(@Nonnull PerkAttributeType type) {
        super(type);
        this.defaultValue = type.isMultiplicative() ? 1.0 : 0.0;
    }

    @SuppressWarnings("unchecked")
    public <T extends ReaderPercentageAttribute> T setDefaultValue(float defaultValue) {
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
        return PerkAttributeHelper.computeValue(player, allocated, getType().getKey(), defaultValue) - defaultValue;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public PerkStatistic getStatistics(@Nonnull Player player,
                                       @Nonnull Set<ResourceLocation> allocated) {
        double computed = PerkAttributeHelper.computeValue(player, allocated, getType().getKey(), defaultValue);
        int rounded = (int) Math.round(computed * 100);
        String strOut = rounded + "%";
        return new PerkStatistic(getType(), strOut, "", "");
    }
}
