/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.PerkLevelInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class PerkLevelTrigger extends SimpleCriterionTrigger<PerkLevelInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("perk_level");

    @Override
    @Nonnull
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    @Nonnull
    protected PerkLevelInstance createInstance(@Nonnull JsonObject json,
                                               @Nonnull ContextAwarePredicate player,
                                               @Nonnull DeserializationContext context) {
        return PerkLevelInstance.deserialize(getId(), json);
    }

    public void trigger(@Nonnull ServerPlayer player) {
        super.trigger(player, instance -> instance.test(player));
    }
}
