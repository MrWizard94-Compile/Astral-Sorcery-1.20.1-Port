package hellfirepvp.astralsorcery.common.crystal;

import javax.annotation.Nullable;

/**
 * Implemented by block entities that store a {@link CrystalAttributes} instance,
 * e.g. celestial crystal clusters placed in the world.
 */
public interface CrystalAttributeTile {

    @Nullable
    CrystalAttributes getAttributes();

    void setAttributes(@Nullable CrystalAttributes attributes);

    default CrystalAttributes getMissingAttributes() {
        return CrystalAttributes.Builder.newBuilder(false).build();
    }
}
