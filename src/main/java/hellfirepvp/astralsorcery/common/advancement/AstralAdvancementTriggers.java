/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
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
 * Custom advancement criterion triggers for Astral Sorcery progression milestones.
 * Each trigger fires when the player reaches a specific point in the mod's progression.
 *
 * <p>Triggers are registered via {@link CriteriaTriggers#register(String, net.minecraft.advancements.CriterionTrigger)}
 * during common setup. Datapack advancement JSONs reference them by their
 * {@code astralsorcery:*} IDs.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * ICriterionTrigger -> CriterionTrigger,
 * ICriterionInstance -> AbstractCriterionTriggerInstance,
 * EntityPredicate.AndPredicate -> ContextAwarePredicate,
 * CriteriaTriggers.register(CriterionTrigger) signature stable</p>
 */
public final class AstralAdvancementTriggers {

    private AstralAdvancementTriggers() {}

    /** Triggered when a player attunes to a constellation. */
    public static final GenericASTrigger ATTUNE_PLAYER =
            new GenericASTrigger(AstralSorcery.key("attune_player"));

    /** Triggered when a player discovers a new constellation. */
    public static final GenericASTrigger DISCOVER_CONSTELLATION =
            new GenericASTrigger(AstralSorcery.key("discover_constellation"));

    /** Triggered when a player reaches a new progression tier. */
    public static final GenericASTrigger REACH_TIER =
            new GenericASTrigger(AstralSorcery.key("reach_tier"));

    /** Triggered when a player allocates a perk in the tree. */
    public static final GenericASTrigger ALLOCATE_PERK =
            new GenericASTrigger(AstralSorcery.key("allocate_perk"));

    /** Triggered when a player crafts at a starlight altar. */
    public static final GenericASTrigger ALTAR_CRAFT =
            new GenericASTrigger(AstralSorcery.key("altar_craft"));

    /**
     * Registers all triggers. Must be called once during common setup.
     */
    public static void init() {
        CriteriaTriggers.register(ATTUNE_PLAYER);
        CriteriaTriggers.register(DISCOVER_CONSTELLATION);
        CriteriaTriggers.register(REACH_TIER);
        CriteriaTriggers.register(ALLOCATE_PERK);
        CriteriaTriggers.register(ALTAR_CRAFT);
    }

    /**
     * A generic criterion trigger that simply fires for a player
     * with no additional predicate matching. Advancement JSONs can
     * use these triggers by ID without extra conditions.
     *
     * <p>For triggers that need to match specific data (which constellation,
     * which tier, etc.), extend this class and add predicate fields
     * to the TriggerInstance.</p>
     */
    public static class GenericASTrigger extends SimpleCriterionTrigger<GenericASTrigger.Instance> {

        @Nonnull
        private final ResourceLocation id;

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
                                          @Nonnull ContextAwarePredicate playerPredicate,
                                          @Nonnull DeserializationContext context) {
            return new Instance(id, playerPredicate);
        }

        /**
         * Fires this trigger for the given player with no extra conditions.
         */
        public void trigger(@Nonnull ServerPlayer player) {
            super.trigger(player, instance -> true);
        }

        /**
         * Trigger instance that always matches (no additional predicates).
         */
        public static class Instance extends AbstractCriterionTriggerInstance {

            public Instance(@Nonnull ResourceLocation criterion,
                            @Nonnull ContextAwarePredicate playerPredicate) {
                super(criterion, playerPredicate);
            }

            @Override
            @Nonnull
            public JsonObject serializeToJson(@Nonnull net.minecraft.advancements.critereon.SerializationContext context) {
                return super.serializeToJson(context);
            }
        }
    }
}
