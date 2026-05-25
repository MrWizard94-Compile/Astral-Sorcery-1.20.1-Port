/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

/**
 * Custom advancement criterion triggers for Astral Sorcery progression.
 *
 * <p>Typed triggers carry predicate data (which constellation, which recipe, etc.)
 * so advancement JSONs can match specific events. Generic triggers fire with no
 * extra conditions and are used where no matching data is needed.</p>
 *
 * <p>1.16 → 1.20 changes:
 * ICriterionTrigger/ListenerCriterionTrigger → SimpleCriterionTrigger (manages
 * listeners internally); ICriterionInstance → AbstractCriterionTriggerInstance;
 * EntityPredicate.AndPredicate.ANY_AND → ContextAwarePredicate.ANY;
 * ConditionArrayParser → DeserializationContext.</p>
 */
public final class AstralAdvancementTriggers {

    private AstralAdvancementTriggers() {}

    // Typed triggers — carry predicate data for fine-grained advancement matching

    public static final AltarCraftTrigger            ALTAR_CRAFT            = new AltarCraftTrigger();
    public static final AttuneCrystalTrigger          ATTUNE_CRYSTAL         = new AttuneCrystalTrigger();
    public static final AttuneSelfTrigger             ATTUNE_SELF            = new AttuneSelfTrigger();
    public static final DiscoverConstellationTrigger  DISCOVER_CONSTELLATION = new DiscoverConstellationTrigger();
    public static final PerkLevelTrigger              PERK_LEVEL             = new PerkLevelTrigger();

    // Generic triggers — fire with no predicate; advancement JSON matches by ID only

    public static final GenericASTrigger REACH_TIER    = new GenericASTrigger(AstralSorcery.key("reach_tier"));
    public static final GenericASTrigger ALLOCATE_PERK = new GenericASTrigger(AstralSorcery.key("allocate_perk"));
    public static final GenericASTrigger ALTAR_UPGRADE = new GenericASTrigger(AstralSorcery.key("altar_upgrade"));
    public static final GenericASTrigger INFUSION_CRAFT = new GenericASTrigger(AstralSorcery.key("infusion_craft"));

    public static void init() {
        CriteriaTriggers.register(ALTAR_CRAFT);
        CriteriaTriggers.register(ATTUNE_CRYSTAL);
        CriteriaTriggers.register(ATTUNE_SELF);
        CriteriaTriggers.register(DISCOVER_CONSTELLATION);
        CriteriaTriggers.register(PERK_LEVEL);
        CriteriaTriggers.register(REACH_TIER);
        CriteriaTriggers.register(ALLOCATE_PERK);
        CriteriaTriggers.register(ALTAR_UPGRADE);
        CriteriaTriggers.register(INFUSION_CRAFT);
    }

    public static class GenericASTrigger extends SimpleCriterionTrigger<GenericASTrigger.Instance> {

        @Nonnull private final ResourceLocation id;

        public GenericASTrigger(@Nonnull ResourceLocation id) {
            this.id = id;
        }

        @Override
        @Nonnull
        public ResourceLocation getId() {
            return id;
        }

        @Override
        @Nonnull
        protected Instance createInstance(@Nonnull JsonObject json,
                                          @Nonnull ContextAwarePredicate player,
                                          @Nonnull DeserializationContext context) {
            return new Instance(id, player);
        }

        public void trigger(@Nonnull ServerPlayer player) {
            super.trigger(player, instance -> true);
        }

        public static class Instance extends AbstractCriterionTriggerInstance {
            public Instance(@Nonnull ResourceLocation criterion,
                            @Nonnull ContextAwarePredicate player) {
                super(criterion, player);
            }
        }
    }
}
