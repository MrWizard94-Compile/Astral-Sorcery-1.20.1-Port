package hellfirepvp.astralsorcery.common.item.useeffect;

import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;

import javax.annotation.Nullable;

public class ItemShiftingStarVicio extends ItemShiftingStar {

    @Nullable
    @Override
    public IMajorConstellation getBaseConstellation() {
        return ConstellationsAS.VICIO;
    }
}
