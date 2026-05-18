/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.attunement;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active.ActivePlayerAttunementRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class AttunePlayerRecipe extends AttunementRecipe<ActivePlayerAttunementRecipe> {

    public AttunePlayerRecipe() {
        super(AstralSorcery.key("attune_player"));
    }

    @Override
    public boolean canStartCrafting(@Nonnull BlockEntityAttunementAltar altar) {
        Level level = altar.getLevel();
        if (level == null || !DayTimeHelper.isNight(level)) {
            return false;
        }
        return findEligiblePlayer(altar) != null;
    }

    @Nonnull
    @Override
    public ActivePlayerAttunementRecipe createRecipe(@Nonnull BlockEntityAttunementAltar altar) {
        ServerPlayer player = findEligiblePlayer(altar);
        ResourceLocation cstKey = altar.getAttunedConstellation();
        return new ActivePlayerAttunementRecipe(this, cstKey,
                player != null ? player.getUUID() : null);
    }

    @Nonnull
    @Override
    public ActivePlayerAttunementRecipe deserialize(@Nonnull BlockEntityAttunementAltar altar,
                                                    @Nonnull CompoundTag nbt,
                                                    @Nullable ActivePlayerAttunementRecipe previousInstance) {
        return new ActivePlayerAttunementRecipe(this, nbt);
    }

    @Nullable
    private static ServerPlayer findEligiblePlayer(@Nonnull BlockEntityAttunementAltar altar) {
        ResourceLocation cstKey = altar.getAttunedConstellation();
        if (cstKey == null) {
            return null;
        }
        IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
        if (!(cst instanceof IMajorConstellation)) {
            return null;
        }

        Level level = altar.getLevel();
        if (level == null) {
            return null;
        }

        AABB searchBox = new AABB(altar.getBlockPos()).inflate(3.0);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, searchBox);
        return players.isEmpty() ? null : players.get(0);
    }
}
