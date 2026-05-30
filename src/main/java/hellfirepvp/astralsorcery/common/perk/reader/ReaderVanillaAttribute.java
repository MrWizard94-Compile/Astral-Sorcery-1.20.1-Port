package hellfirepvp.astralsorcery.common.perk.reader;

import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Reader for perk attributes that directly mirror a vanilla {@link Attribute}.
 * Reads the current value from the player's attribute map for display.
 *
 * <p>1.16 → 1.20: RegistryObject replaced by Supplier; Player replaces PlayerEntity;
 * ModifiableAttributeInstance → AttributeInstance.</p>
 */
public class ReaderVanillaAttribute extends PerkAttributeReader {

    protected final Supplier<Attribute> attribute;
    protected boolean formatAsDecimal = false;

    public ReaderVanillaAttribute(@Nonnull PerkAttributeType type,
                                   @Nonnull Supplier<Attribute> reference) {
        super(type);
        this.attribute = reference;
    }

    @SuppressWarnings("unchecked")
    public <T extends ReaderVanillaAttribute> T formatAsDecimal() {
        this.formatAsDecimal = true;
        return (T) this;
    }

    @Override
    public double getDefaultValue(@Nonnull Player player, @Nonnull LogicalSide side) {
        Attribute attr = attribute.get();
        AttributeInstance inst = player.getAttribute(attr);
        return inst != null ? inst.getBaseValue() : 0.0;
    }

    @Override
    public double getModifierValueForMode(@Nonnull Player player,
                                          @Nonnull Set<ResourceLocation> allocated,
                                          @Nonnull LogicalSide side,
                                          @Nonnull ModifierType mode) {
        Attribute attr = attribute.get();
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) {
            return 0.0;
        }
        return inst.getValue() - inst.getBaseValue();
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public PerkStatistic getStatistics(@Nonnull Player player,
                                       @Nonnull Set<ResourceLocation> allocated) {
        Attribute attr = attribute.get();
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) {
            return null;
        }
        double value = inst.getValue();
        String display = formatAsDecimal
                ? (value >= 0 ? "+" : "") + formatDecimal(value)
                : (value >= 0 ? "+" : "") + (int) Math.round(value);
        return new PerkStatistic(getType(), display, "", "");
    }
}
