package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Concrete constellation implementations: Major, Weak, WeakSpecial, Minor.
 *
 * <p>1.16 → 1.20 changes:
 * PlayerEntity → Player,
 * MathHelper → Mth,
 * setRegistryName(ResourceLocation) → stored directly via BaseConstellation,
 * MiscUtils.getCurrentlyActiveMod() → simplified to always use AS modid,
 * IForgeRegistryEntry removed</p>
 */
public abstract class Constellation extends BaseConstellation {

    private static int counter = 0;

    private final List<Supplier<Ingredient>> signatureItems = new LinkedList<>();

    private final String name;
    private final String simpleName;
    private final Color color;
    private final int id;

    public Constellation(@Nonnull String name) {
        this(name, ColorsAS.CONSTELLATION_TYPE_MAJOR);
    }

    public Constellation(@Nonnull String name, @Nonnull Color color) {
        this.id = counter++;
        this.simpleName = name;
        this.setRegistryName(AstralSorcery.key(name));
        this.name = AstralSorcery.MODID + ".constellation." + name;
        this.color = color;
    }

    @Override
    @Nonnull
    public IConstellation addSignatureItem(@Nonnull Supplier<Ingredient> ingredient) {
        this.signatureItems.add(ingredient);
        return this;
    }

    @Override
    @Nonnull
    public List<Ingredient> getConstellationSignatureItems() {
        return this.signatureItems.stream()
                .map(Supplier::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canDiscover(@Nullable Player player, @Nonnull PlayerProgress progress) {
        return true;
    }

    @Override
    public int getSortingId() {
        return this.id;
    }

    @Override
    @Nonnull
    public Color getConstellationColor() {
        return color;
    }

    @Override
    @Nonnull
    public String getTranslationKey() {
        return this.name;
    }

    @Override
    @Nonnull
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String toString() {
        return "Constellation={name:" + this.name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constellation that = (Constellation) o;
        return Objects.equals(getRegistryName(), that.getRegistryName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegistryName());
    }

    @Override
    public int compareTo(IConstellation o) {
        return Integer.compare(this.getSortingId(), o.getSortingId());
    }

    // ---- Concrete constellation types ----

    public static class Major extends Weak implements IMajorConstellation {

        public Major(@Nonnull String name) {
            super(name);
        }

        public Major(@Nonnull String name, @Nonnull Color color) {
            super(name, color);
        }

        @Override
        public boolean canDiscover(@Nullable Player player, @Nonnull PlayerProgress progress) {
            return true;
        }
    }

    public static class Weak extends Constellation implements IWeakConstellation {

        public Weak(@Nonnull String name) {
            super(name);
        }

        public Weak(@Nonnull String name, @Nonnull Color color) {
            super(name, color);
        }

        @Override
        public boolean canDiscover(@Nullable Player player, @Nonnull PlayerProgress progress) {
            return super.canDiscover(player, progress)
                    && progress.getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT)
                    && progress.wasOnceAttuned();
        }
    }

    public static abstract class WeakSpecial extends Weak implements IConstellationSpecialShowup {

        public WeakSpecial(@Nonnull String name) {
            super(name);
        }

        public WeakSpecial(@Nonnull String name, @Nonnull Color color) {
            super(name, color);
        }
    }

    public static class Minor extends Constellation implements IMinorConstellation {

        private final List<MoonPhase> phases;

        public Minor(@Nonnull String name, @Nonnull MoonPhase... applicablePhases) {
            super(name);
            phases = new ArrayList<>(applicablePhases.length);
            for (MoonPhase ph : applicablePhases) {
                if (ph == null) {
                    throw new IllegalArgumentException(
                            "null MoonPhase passed to Minor constellation registration for " + name);
                }
                phases.add(ph);
            }
        }

        public Minor(@Nonnull String name, @Nonnull Color color, @Nonnull MoonPhase... applicablePhases) {
            super(name, color);
            phases = new ArrayList<>(applicablePhases.length);
            for (MoonPhase ph : applicablePhases) {
                if (ph == null) {
                    throw new IllegalArgumentException(
                            "null MoonPhase passed to Minor constellation registration for " + name);
                }
                phases.add(ph);
            }
        }

        @Override
        @Nonnull
        public List<MoonPhase> getShowupMoonPhases(long rSeed) {
            List<MoonPhase> shifted = new ArrayList<>(phases.size());
            MoonPhase[] allPhases = MoonPhase.values();
            for (MoonPhase mp : this.phases) {
                int index = mp.ordinal() + (((int) (rSeed % allPhases.length)) + allPhases.length);
                while (index >= allPhases.length) {
                    index -= allPhases.length;
                }
                index = Mth.clamp(index, 0, allPhases.length - 1);
                MoonPhase offset = allPhases[index];
                if (!shifted.contains(offset)) {
                    shifted.add(offset);
                }
            }
            return shifted;
        }

        @Override
        public boolean canDiscover(@Nullable Player player, @Nonnull PlayerProgress progress) {
            return super.canDiscover(player, progress)
                    && progress.wasOnceAttuned()
                    && progress.getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT);
        }
    }
}
