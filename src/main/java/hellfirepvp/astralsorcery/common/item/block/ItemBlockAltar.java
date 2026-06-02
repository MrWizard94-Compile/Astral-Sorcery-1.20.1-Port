package hellfirepvp.astralsorcery.common.item.block;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.item.base.ItemBlockAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * BlockItem for the Starlight Crafting Altar that places the altar block
 * with a specific tier (AltarType) pre-set in the blockstate.
 *
 * <p>The altar is a single block with an AltarType blockstate property.
 * We register 4 separate items (one per tier) so each tier appears
 * independently in the creative tab and recipe outputs.</p>
 */
public class ItemBlockAltar extends ItemBlockAS {

    private final BlockAltar.AltarType altarType;

    public ItemBlockAltar(@Nonnull BlockAltar.AltarType altarType) {
        super(BlocksAS.ALTAR.get());
        this.altarType = altarType;
    }

    @Nonnull
    @Override
    public String getDescriptionId() {
        return "block.astralsorcery.altar_" + altarType.getSerializedName();
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(@Nonnull BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) return null;
        return state.setValue(BlockAltar.ALTAR_TYPE, altarType);
    }
}
