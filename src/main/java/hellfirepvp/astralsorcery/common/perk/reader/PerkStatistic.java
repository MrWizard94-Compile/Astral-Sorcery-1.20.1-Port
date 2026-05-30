package hellfirepvp.astralsorcery.common.perk.reader;

import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;

import javax.annotation.Nonnull;

/**
 * A snapshot of a perk attribute's current value, formatted for display
 * in the journal perk tree UI.
 *
 * <p>1.16 → 1.20: no API changes; unlocalized name is the same.</p>
 */
public class PerkStatistic {

    @Nonnull
    private final PerkAttributeType type;
    @Nonnull
    private final String unlocPerkTypeName;
    @Nonnull
    private final String perkValue;
    @Nonnull
    private final String suffix;
    @Nonnull
    private final String postProcessInfo;

    public PerkStatistic(@Nonnull PerkAttributeType type, @Nonnull String perkValue,
                         @Nonnull String suffix, @Nonnull String postProcessInfo) {
        this.type = type;
        this.unlocPerkTypeName = type.getUnlocalizedName();
        this.perkValue = perkValue;
        this.suffix = suffix;
        this.postProcessInfo = postProcessInfo;
    }

    @Nonnull
    public PerkAttributeType getType() {
        return type;
    }

    @Nonnull
    public String getUnlocPerkTypeName() {
        return unlocPerkTypeName;
    }

    @Nonnull
    public String getPerkValue() {
        return perkValue;
    }

    @Nonnull
    public String getSuffix() {
        return suffix;
    }

    @Nonnull
    public String getPostProcessInfo() {
        return postProcessInfo;
    }
}
