/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active;

import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunePlayerRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Server-side active state for player attunement in the attunement altar.
 * Client-side camera flight, visual effects, and sound are deferred to Phase 12.
 */
public class ActivePlayerAttunementRecipe extends AttunementRecipe.Active<AttunePlayerRecipe> {

    private static final int DURATION = 800;

    @Nullable
    private ResourceLocation constellation;
    @Nullable
    private UUID playerUUID;

    public ActivePlayerAttunementRecipe(@Nonnull AttunePlayerRecipe recipe,
                                        @Nullable ResourceLocation constellation,
                                        @Nullable UUID playerUUID) {
        super(recipe);
        this.constellation = constellation;
        this.playerUUID = playerUUID;
    }

    public ActivePlayerAttunementRecipe(@Nonnull AttunePlayerRecipe recipe,
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
        // Player attunement: apply constellation to player progress.
        // Deferred until ResearchManager / PlayerProgress is fully ported.
    }

    @Override
    public void doTick(@Nonnull LogicalSide side, @Nonnull BlockEntityAttunementAltar altar) {}

    @Override
    public void writeToNBT(@Nonnull CompoundTag nbt) {
        super.writeToNBT(nbt);
        if (constellation != null) {
            nbt.putString("constellation", constellation.toString());
        }
        if (playerUUID != null) {
            nbt.putUUID("player", playerUUID);
        }
    }

    @Override
    protected void readFromNBT(@Nonnull CompoundTag nbt) {
        super.readFromNBT(nbt);
        if (nbt.contains("constellation")) {
            constellation = new ResourceLocation(nbt.getString("constellation"));
        }
        if (nbt.hasUUID("player")) {
            playerUUID = nbt.getUUID("player");
        }
    }
}
