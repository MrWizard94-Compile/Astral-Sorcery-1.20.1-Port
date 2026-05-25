/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.ConstellationInstance;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class AttuneSelfTrigger extends SimpleCriterionTrigger<ConstellationInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("attune_self");

    @Override
    @Nonnull
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    @Nonnull
    protected ConstellationInstance createInstance(@Nonnull JsonObject json,
                                                    @Nonnull ContextAwarePredicate player,
                                                    @Nonnull DeserializationContext context) {
        return ConstellationInstance.deserialize(getId(), json);
    }

    public void trigger(@Nonnull ServerPlayer player, @Nonnull IMajorConstellation attuned) {
        super.trigger(player, instance -> instance.test(attuned));
    }
}
