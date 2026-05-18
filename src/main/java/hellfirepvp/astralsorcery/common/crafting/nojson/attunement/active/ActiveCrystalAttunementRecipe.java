/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active;

import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttuneCrystalRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Server-side active state for crystal attunement in the attunement altar.
 * Client-side visual effects (FXOrbitalCrystalAttunement, camera paths, sounds)
 * are deferred to Phase 12 (client rendering).
 */
public class ActiveCrystalAttunementRecipe extends AttunementRecipe.Active<AttuneCrystalRecipe> {

    private static final int DURATION = 500;

    @Nullable
    private ResourceLocation constellation;
    private int entityId;

    public ActiveCrystalAttunementRecipe(@Nonnull AttuneCrystalRecipe recipe,
                                         @Nullable ResourceLocation constellation,
                                         int crystalEntityId) {
        super(recipe);
        this.constellation = constellation;
        this.entityId = crystalEntityId;
    }

    public ActiveCrystalAttunementRecipe(@Nonnull AttuneCrystalRecipe recipe,
                                         @Nonnull CompoundTag nbt) {
        super(recipe);
        this.readFromNBT(nbt);
    }

    @Override
    public void startCrafting(@Nonnull BlockEntityAttunementAltar altar) {}

    @Override
    public void stopCrafting(@Nonnull BlockEntityAttunementAltar altar) {}

    @Override
    public boolean isFinished(@Nonnull BlockEntityAttunementAltar altar) {
        return getTick() >= DURATION;
    }

    @Override
    public void finishRecipe(@Nonnull BlockEntityAttunementAltar altar) {
        // Crystal attunement: apply constellation to crystal stack.
        // Deferred until ConstellationItem wiring is complete.
    }

    @Override
    public void doTick(@Nonnull LogicalSide side, @Nonnull BlockEntityAttunementAltar altar) {}

    @Override
    public void writeToNBT(@Nonnull CompoundTag nbt) {
        super.writeToNBT(nbt);
        if (constellation != null) {
            nbt.putString("constellation", constellation.toString());
        }
        nbt.putInt("entityId", entityId);
    }

    @Override
    protected void readFromNBT(@Nonnull CompoundTag nbt) {
        super.readFromNBT(nbt);
        if (nbt.contains("constellation")) {
            constellation = new ResourceLocation(nbt.getString("constellation"));
        }
        entityId = nbt.getInt("entityId");
    }
}
