package hellfirepvp.astralsorcery.common.perk.reader;

import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Client-side helper that resolves the correct {@link PerkAttributeReader} for each
 * attribute type and returns formatted {@link PerkStatistic}s for the perk tree UI.
 *
 * <p>Readers can be overridden per-type via {@link Builder#overrideReader}
 * for special display cases (e.g. the telescope perk tree showing unmodified values).</p>
 *
 * <p>1.16 → 1.20: PerkAttributeMap removed; player's allocated perk set is
 * read directly from {@link ResearchHelper} on the client side.</p>
 */
@OnlyIn(Dist.CLIENT)
public class PerkAttributeInterpreter {

    private final Map<PerkAttributeType, PerkAttributeReader> readerOverrides = new HashMap<>();
    @Nonnull
    private final Player player;
    @Nonnull
    private final Set<ResourceLocation> allocated;

    private PerkAttributeInterpreter(@Nonnull Player player) {
        this.player = player;
        this.allocated = ResearchHelper.getClientProgress().getAllocatedPerks();
    }

    /**
     * Returns a statistic for the given attribute type, or null if no reader is
     * registered for it.
     */
    @Nullable
    public PerkStatistic getValue(@Nonnull PerkAttributeType type) {
        PerkAttributeReader reader = readerOverrides.getOrDefault(type,
                AttributeTypeRegistry.getReader(type));
        if (reader == null) {
            return null;
        }
        return reader.getStatistics(player, allocated);
    }

    // ---- Builder ----

    @OnlyIn(Dist.CLIENT)
    public static class Builder {

        @Nonnull
        private final PerkAttributeInterpreter interpreter;

        private Builder(@Nonnull Player player) {
            this.interpreter = new PerkAttributeInterpreter(player);
        }

        @Nonnull
        public static Builder newBuilder(@Nonnull Player player) {
            return new Builder(player);
        }

        @Nonnull
        public Builder overrideReader(@Nonnull PerkAttributeType type,
                                       @Nonnull PerkAttributeReader reader) {
            interpreter.readerOverrides.put(type, reader);
            return this;
        }

        @Nonnull
        public PerkAttributeInterpreter build() {
            return interpreter;
        }
    }

    /**
     * Convenience factory for a default interpreter using the player's current allocations.
     */
    @Nonnull
    public static PerkAttributeInterpreter defaultInterpreter(@Nonnull Player player) {
        return Builder.newBuilder(player).build();
    }
}
