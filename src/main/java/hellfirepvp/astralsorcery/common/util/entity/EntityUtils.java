package hellfirepvp.astralsorcery.common.util.entity;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Entity utility methods: player lookup, spawning, loot generation, and selection predicates.
 *
 * <p>1.16 -> 1.20 changes:
 * PlayerEntity -> Player, MobEntity -> Mob,
 * EntityClassification -> MobCategory, SpawnReason -> MobSpawnType,
 * EffectInstance -> MobEffectInstance,
 * getActivePotionEffect -> getEffect, addPotionEffect -> addEffect,
 * LogicalSidedProvider.INSTANCE.get(SERVER) -> ServerLifecycleHooks.getCurrentServer(),
 * Minecraft.getInstance().world -> Minecraft.getInstance().level,
 * getPlayerByUuid -> getPlayerByUUID,
 * getEntitiesWithinAABB -> getEntitiesOfClass,
 * AxisAlignedBB -> AABB, IWorld -> LevelAccessor,
 * LootContext.Builder -> LootParams.Builder,
 * LootParameterSets -> LootContextParamSets,
 * LootParameters -> LootContextParams,
 * EntitySpawnPlacementRegistry -> SpawnPlacements,
 * WorldEntitySpawner -> NaturalSpawner,
 * MobSpawnInfo.Spawners -> MobSpawnSettings.SpawnerData</p>
 */
public class EntityUtils {

    private static final Random rand = new Random();

    @Nullable
    public static Player getPlayerServer(@Nonnull UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(playerUUID);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static Player getPlayerClient(@Nonnull UUID playerUUID) {
        ClientLevel clWorld = Minecraft.getInstance().level;
        if (clWorld == null) {
            return null;
        }
        return clWorld.getPlayerByUUID(playerUUID);
    }

    public static void applyPotionEffectAtHalf(@Nonnull LivingEntity entity,
                                                @Nonnull MobEffectInstance effect) {
        MobEffectInstance activeEffect = entity.getEffect(effect.getEffect());
        if (activeEffect != null) {
            if (activeEffect.getDuration() <= effect.getDuration() / 2) {
                entity.addEffect(effect);
            }
        } else {
            entity.addEffect(effect);
        }
    }

    public static void applyVortexMotion(@Nonnull Supplier<Vector3> positionSupplier,
                                          @Nonnull Consumer<Vector3> addMotion,
                                          @Nonnull Vector3 to,
                                          double vortexRange,
                                          double multiplier) {
        Vector3 pos = positionSupplier.get();
        double diffX = (to.getX() - pos.getX()) / vortexRange;
        double diffY = (to.getY() - pos.getY()) / vortexRange;
        double diffZ = (to.getZ() - pos.getZ()) / vortexRange;
        double dist = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        if (1.0D - dist > 0.0D) {
            double dstFactorSq = (1.0D - dist) * (1.0D - dist);
            Vector3 toAdd = new Vector3();
            toAdd.setX(diffX / dist * dstFactorSq * 0.15D * multiplier);
            toAdd.setY(diffY / dist * dstFactorSq * 0.15D * multiplier);
            toAdd.setZ(diffZ / dist * dstFactorSq * 0.15D * multiplier);
            addMotion.accept(toAdd);
        }
    }

    @Nonnull
    public static List<ItemStack> generateLoot(@Nonnull LivingEntity entity,
                                                @Nonnull DamageSource srcDeath,
                                                @Nullable LivingEntity lastAttacker) {
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) {
            return Collections.emptyList();
        }
        ServerLevel sw = (ServerLevel) entity.level();

        if (!sw.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            return Collections.emptyList();
        }

        ResourceLocation lootTableKey = entity.getLootTable();
        LootTable table = srv.getLootData().getLootTable(lootTableKey);
        LootParams.Builder builder = new LootParams.Builder(sw)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, srcDeath)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, srcDeath.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, srcDeath.getDirectEntity());
        if (lastAttacker instanceof Player player) {
            builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withLuck(player.getLuck());
        }

        return table.getRandomItems(builder.create(LootContextParamSets.ENTITY));
    }

    @Nullable
    public static <T extends Entity> T getClosestEntity(@Nonnull LevelAccessor world,
                                                         @Nonnull Class<T> type,
                                                         @Nonnull AABB box,
                                                         @Nonnull Vector3 closestTo) {
        List<T> entities = world.getEntitiesOfClass(type, box, Entity::isAlive);
        return selectClosest(entities, closestTo::distanceSquared);
    }

    @Nonnull
    @SafeVarargs
    public static Predicate<Entity> selectEntities(@Nonnull Class<? extends Entity>... entities) {
        return entity -> {
            if (entity == null || !entity.isAlive()) return false;
            Class<? extends Entity> clazz = entity.getClass();
            for (Class<? extends Entity> test : entities) {
                if (test.isAssignableFrom(clazz)) return true;
            }
            return false;
        };
    }

    @Nonnull
    public static Predicate<Entity> selectItemClassInstanceof(@Nonnull Class<?> itemClass) {
        return entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity itemEntity)) return false;
            ItemStack i = itemEntity.getItem();
            if (i.isEmpty()) return false;
            return itemClass.isAssignableFrom(i.getItem().getClass());
        };
    }

    @Nonnull
    public static Predicate<Entity> selectItem(@Nonnull Item item) {
        return entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity itemEntity)) return false;
            ItemStack i = itemEntity.getItem();
            if (i.isEmpty()) return false;
            return i.getItem().equals(item);
        };
    }

    @Nonnull
    public static Predicate<Entity> selectItemStack(@Nonnull Function<ItemStack, Boolean> acceptor) {
        return entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity itemEntity)) return false;
            ItemStack i = itemEntity.getItem();
            if (i.isEmpty()) return false;
            return acceptor.apply(i);
        };
    }

    @Nullable
    public static <T> T selectClosest(@Nonnull Collection<T> elements,
                                       @Nonnull Function<T, Double> dstFunc) {
        if (elements.isEmpty()) return null;

        double dstClosest = Double.MAX_VALUE;
        T closestElement = null;
        for (T element : elements) {
            double dst = dstFunc.apply(element);
            if (dst < dstClosest) {
                closestElement = element;
                dstClosest = dst;
            }
        }
        return closestElement;
    }

    // Spawning methods deferred — the 1.20 spawning API (SpawnPlacements,
    // NaturalSpawner, biome mob settings) is substantially restructured.
    // Ported when constellation effects are implemented (Phase 9).

    public static class SpawnConditionFlags {

        public static final int IGNORE_PLACEMENT_RULES         = 0b0001;
        public static final int IGNORE_ENTITY_COLLISION        = 0b0010;
        public static final int IGNORE_BLOCK_COLLISION         = 0b0100;
        public static final int IGNORE_ENTITY_SPAWN_CONDITIONS = 0b1000;

        public static final int IGNORE_COLLISIONS = IGNORE_BLOCK_COLLISION | IGNORE_ENTITY_COLLISION;
        public static final int IGNORE_SPAWN_CONDITIONS = IGNORE_PLACEMENT_RULES | IGNORE_ENTITY_SPAWN_CONDITIONS;
        public static final int IGNORE_ALL = IGNORE_COLLISIONS | IGNORE_SPAWN_CONDITIONS;

        public static boolean isSet(int flags, int flag) {
            return (flags & flag) != 0;
        }
    }
}
