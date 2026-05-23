package hellfirepvp.astralsorcery.common.item.useeffect;

import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;

import javax.annotation.Nullable;

public class ItemShiftingStarArmara extends ItemShiftingStar {

    @Nullable
    @Override
    public IMajorConstellation getBaseConstellation() {
        return ConstellationsAS.ARMARA;
    }
}
