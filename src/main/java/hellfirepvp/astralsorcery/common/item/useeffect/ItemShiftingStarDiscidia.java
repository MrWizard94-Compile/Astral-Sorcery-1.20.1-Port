package hellfirepvp.astralsorcery.common.item.useeffect;

import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;

import javax.annotation.Nullable;

public class ItemShiftingStarDiscidia extends ItemShiftingStar {

    @Nullable
    @Override
    public IMajorConstellation getBaseConstellation() {
        return ConstellationsAS.DISCIDIA;
    }
}
