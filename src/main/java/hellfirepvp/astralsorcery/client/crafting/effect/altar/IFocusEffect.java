package hellfirepvp.astralsorcery.client.crafting.effect.altar;

import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Optional marker for {@link AltarRecipeEffect} subclasses that need to query
 * the focus item stack currently held in the altar's focus slot.
 * Implemented by effects that alter their visuals based on the focus constellation.
 */
@OnlyIn(Dist.CLIENT)
public interface IFocusEffect {

    /**
     * Called once per tick before {@link AltarRecipeEffect#onTick} with the
     * current focus item stack so the effect can adapt its visuals.
     *
     * @param altar the altar block entity
     * @param focus the item currently in the focus slot, may be empty
     */
    @OnlyIn(Dist.CLIENT)
    void setFocusItem(@Nonnull BlockEntityAltar altar, @Nonnull ItemStack focus);
}
