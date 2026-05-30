package hellfirepvp.astralsorcery.client.screen.journal.perk;

import hellfirepvp.astralsorcery.client.screen.helper.SizeHandler;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * {@link SizeHandler} for the perk tree — bounds the layout by all registered perk positions.
 *
 * <p>1.16 → 1.20: PerkTreePoint.getOffset() → AbstractPerk.getX()/getY();
 * PerkTree.getPerkPoints(LogicalSide) → PerkTree.getAllPerks().</p>
 */
@OnlyIn(Dist.CLIENT)
public class PerkTreeSizeHandler extends SizeHandler {

    public PerkTreeSizeHandler() {
        setWidthHeightNodes(10f);
        setSpaceBetweenNodes(10f);
    }

    @Override
    @Nullable
    public float[] buildRequiredRectangle() {
        float leftMost = 0, rightMost = 0, upperMost = 0, lowerMost = 0;
        for (AbstractPerk perk : PerkTree.getAllPerks()) {
            float x = perk.getX();
            float y = perk.getY();
            if (x < leftMost)  leftMost  = x;
            if (x > rightMost) rightMost = x;
            if (y > lowerMost) lowerMost = y;
            if (y < upperMost) upperMost = y;
        }
        return new float[] { leftMost, rightMost, upperMost, lowerMost };
    }
}
