/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.attunement;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active.ActiveCrystalAttunementRecipe;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class AttuneCrystalRecipe extends AttunementRecipe<ActiveCrystalAttunementRecipe> {

    public AttuneCrystalRecipe() {
        super(AstralSorcery.key("attune_crystal"));
    }

    @Override
    public boolean canStartCrafting(@Nonnull BlockEntityAttunementAltar altar) {
        Level level = altar.getLevel();
        if (level == null || !DayTimeHelper.isNight(level)) {
            return false;
        }
        return findApplicableCrystal(altar) != null;
    }

    @Nonnull
    @Override
    public ActiveCrystalAttunementRecipe createRecipe(@Nonnull BlockEntityAttunementAltar altar) {
        ItemEntity crystal = findApplicableCrystal(altar);
        int entityId = crystal != null ? crystal.getId() : -1;
        return new ActiveCrystalAttunementRecipe(this, altar.getAttunedConstellation(), entityId);
    }

    @Nonnull
    @Override
    public ActiveCrystalAttunementRecipe deserialize(@Nonnull BlockEntityAttunementAltar altar,
                                                     @Nonnull CompoundTag nbt,
                                                     @Nullable ActiveCrystalAttunementRecipe previousInstance) {
        return new ActiveCrystalAttunementRecipe(this, nbt);
    }

    @Nullable
    private static ItemEntity findApplicableCrystal(@Nonnull BlockEntityAttunementAltar altar) {
        ResourceLocation cstKey = altar.getAttunedConstellation();
        if (cstKey == null) {
            return null;
        }
        IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
        if (cst == null) {
            return null;
        }

        AABB boxAt = new AABB(altar.getBlockPos().above()).inflate(1.0);
        Level level = altar.getLevel();
        if (level == null) {
            return null;
        }

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, boxAt);
        for (ItemEntity item : items) {
            if (isApplicableCrystal(item, cst)) {
                return item;
            }
        }
        return null;
    }

    private static boolean isApplicableCrystal(@Nonnull ItemEntity entity, @Nonnull IConstellation cst) {
        ItemStack stack = entity.getItem();
        if (!entity.isAlive() || stack.isEmpty() || !(stack.getItem() instanceof ItemCrystalBase)) {
            return false;
        }
        if (!(stack.getItem() instanceof ConstellationItem ci)) {
            return cst instanceof IWeakConstellation;
        }
        IWeakConstellation attuned = ci.getAttunedConstellation(stack);
        IMinorConstellation trait = ci.getTraitConstellation(stack);
        if (attuned == null && cst instanceof IWeakConstellation) {
            return true;
        }
        return trait == null && cst instanceof IMinorConstellation;
    }
}
