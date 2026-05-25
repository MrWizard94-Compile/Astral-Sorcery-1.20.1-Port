/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.advancement.PerkLevelTrigger;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.PerkLevelManager;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;

public class PerkLevelInstance extends AbstractCriterionTriggerInstance {

    private int levelNeeded = 0;

    private PerkLevelInstance(@Nonnull ResourceLocation id, @Nonnull ContextAwarePredicate player) {
        super(id, player);
    }

    public static PerkLevelInstance reachLevel(int level) {
        PerkLevelInstance instance = new PerkLevelInstance(PerkLevelTrigger.ID, ContextAwarePredicate.ANY);
        instance.levelNeeded = level;
        return instance;
    }

    @Override
    @Nonnull
    public JsonObject serializeToJson(@Nonnull SerializationContext context) {
        JsonObject out = super.serializeToJson(context);
        out.addProperty("levelNeeded", this.levelNeeded);
        return out;
    }

    @Nonnull
    public static PerkLevelInstance deserialize(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
        PerkLevelInstance instance = new PerkLevelInstance(id, ContextAwarePredicate.ANY);
        instance.levelNeeded = GsonHelper.getAsInt(json, "levelNeeded", 0);
        return instance;
    }

    public boolean test(@Nonnull ServerPlayer player) {
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null) return false;
        int level = PerkLevelManager.getLevelFromExp(progress.getPerkExp());
        return level >= this.levelNeeded;
    }
}
