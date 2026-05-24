package hellfirepvp.astralsorcery.common.constellation;

public interface ConstellationTile {

    IWeakConstellation getAttunedConstellation();

    boolean setAttunedConstellation(IWeakConstellation cst);

    IMinorConstellation getTraitConstellation();

    boolean setTraitConstellation(IMinorConstellation cst);
}
