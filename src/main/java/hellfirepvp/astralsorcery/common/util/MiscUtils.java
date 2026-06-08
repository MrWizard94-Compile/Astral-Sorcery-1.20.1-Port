package hellfirepvp.astralsorcery.common.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * General utility grab-bag used across the entire mod.
 * Methods are grouped: collection utils, functional combinators,
 * Minecraft world access helpers, entity/player utils.
 *
 * <p>1.16 → 1.20 changes (extensive):
 * TileEntity → BlockEntity, IBlockReader → BlockGetter, IWorld → LevelAccessor,
 * IWorldReader → LevelReader, World → Level, ServerWorld → ServerLevel,
 * PlayerEntity → Player, ServerPlayerEntity → ServerPlayer,
 * Hand → InteractionHand, getHeldItem → getItemInHand,
 * FlowingFluidBlock → LiquidBlock, RayTraceResult → HitResult,
 * BlockRayTraceResult → BlockHitResult, RayTraceContext → ClipContext,
 * Vector3d → Vec3, RegistryKey → ResourceKey,
 * MathHelper → Mth, ISeedReader → WorldGenLevel,
 * AbstractChunkProvider → ChunkSource, ServerChunkProvider → ServerChunkCache,
 * isRemote → isClientSide, getChunkProvider → getChunkSource,
 * isBlockLoaded → hasChunkAt, getTileEntity → getBlockEntity,
 * isAirBlock → isEmptyBlock, LogicalSidedProvider → ServerLifecycleHooks,
 * entity.getPosX/Y/Z → getX/Y/Z, getLookVec → getLookAngle,
 * world.rayTraceBlocks → level.clip, entity.world → entity.level(),
 * rotationYaw/Pitch → getYRot/getXRot,
 * BlockEvent.BreakEvent constructor updated,
 * player.getPlayerIP → getIpAddress,
 * connection.netManager → connection.connection</p>
 */
public class MiscUtils {

    private static final Logger LOG = LogManager.getLogger();

    // ---- Tile/BlockEntity access ----

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T getTileAt(@Nullable BlockGetter world, @Nullable BlockPos pos,
                                  @Nonnull Class<T> tileClass, boolean forceChunkLoad) {
        if (world == null || pos == null) return null;
        if (world instanceof LevelAccessor levelAccessor) {
            if (!levelAccessor.getChunkSource().hasChunk(
                    pos.getX() >> 4, pos.getZ() >> 4) && !forceChunkLoad) {
                return null;
            }
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) return null;
        if (tileClass.isInstance(te)) return (T) te;
        return null;
    }

    public static boolean canEntityTickAt(@Nonnull LevelAccessor world, @Nonnull BlockPos pos) {
        if (!world.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return false;
        }
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return true;
        }
        return serverLevel.isPositionEntityTicking(pos);
    }

    @Nonnull
    public static List<BlockSnapshot> captureBlockChanges(@Nonnull Level level, @Nonnull Runnable r) {
        level.captureBlockSnapshots = true;
        r.run();
        level.captureBlockSnapshots = false;
        @SuppressWarnings("unchecked")
        List<BlockSnapshot> blockSnapshots =
                (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
        level.capturedBlockSnapshots.clear();
        return blockSnapshots;
    }

    // ---- Collection utilities ----

    @Nullable
    public static <T> T getRandomEntry(@Nullable Collection<T> collection, @Nonnull Random rand) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int index = rand.nextInt(collection.size());
        return Iterables.get(collection, index);
    }

    @Nullable
    public static <T> T getRandomEntry(@Nullable T[] array, @Nonnull Random rand) {
        if (array == null || array.length <= 0) {
            return null;
        }
        return array[rand.nextInt(array.length)];
    }

    @Nullable
    public static ModContainer getCurrentlyActiveMod() {
        return ModLoadingContext.get().getActiveContainer();
    }

    @Nonnull
    public static <T> T getEnumEntry(@Nonnull Class<T> enumClazz, int index) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException(
                    "Called getEnumEntry on class " + enumClazz.getName() + " which isn't an enum.");
        }
        T[] values = enumClazz.getEnumConstants();
        if (values.length == 0) {
            throw new IllegalArgumentException(enumClazz.getName() + " has no enum constants.");
        }
        return values[Mth.clamp(index, 0, values.length - 1)];
    }

    /**
     * Select a weighted random entry from a collection.
     * Inline implementation replacing 1.16 WeightedRandom.getRandomItem
     * to avoid WeightedEntry/RandomSource API changes in 1.20.
     */
    @Nullable
    public static <T> T getWeightedRandomEntry(@Nonnull Collection<T> list,
                                               @Nonnull Random rand,
                                               @Nonnull Function<T, Integer> getWeightFunction) {
        if (list.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (T e : list) {
            totalWeight += getWeightFunction.apply(e);
        }
        if (totalWeight <= 0) {
            return null;
        }
        int roll = rand.nextInt(totalWeight);
        for (T e : list) {
            roll -= getWeightFunction.apply(e);
            if (roll < 0) {
                return e;
            }
        }
        return null;
    }

    @Nullable
    public static <T, V extends Comparable<V>> V getMaxEntry(@Nonnull Collection<T> elements,
                                                             @Nonnull Function<T, V> valueFunction) {
        return getMaxEntry(MiscUtils.transformCollection(elements, valueFunction));
    }

    @Nullable
    public static <T extends Comparable<T>> T getMaxEntry(@Nonnull Collection<T> elements) {
        T maxElement = null;
        for (T element : elements) {
            if (maxElement == null || maxElement.compareTo(element) < 0) {
                maxElement = element;
            }
        }
        return maxElement;
    }

    @Nullable
    public static <T, V extends Comparable<V>> V getMinEntry(@Nonnull Collection<T> elements,
                                                             @Nonnull Function<T, V> valueFunction) {
        return getMinEntry(MiscUtils.transformCollection(elements, valueFunction));
    }

    @Nullable
    public static <T extends Comparable<T>> T getMinEntry(@Nonnull Collection<T> elements) {
        T minElement = null;
        for (T element : elements) {
            if (minElement == null || minElement.compareTo(element) > 0) {
                minElement = element;
            }
        }
        return minElement;
    }

    // ---- Functional combinators ----

    @Nonnull
    public static <T> Runnable apply(@Nonnull Consumer<T> func, @Nonnull Supplier<T> supply) {
        return () -> func.accept(supply.get());
    }

    @Nonnull
    public static <T, U> Consumer<T> apply(@Nonnull BiConsumer<T, U> func,
                                           @Nonnull Supplier<U> supply) {
        return (t) -> func.accept(t, supply.get());
    }

    @Nonnull
    public static <T, U, V> BiConsumer<T, U> apply(@Nonnull TriConsumer<T, U, V> func,
                                                    @Nonnull Supplier<V> supply) {
        return (t, u) -> func.accept(t, u, supply.get());
    }

    @Nonnull
    public static <T, R> Supplier<R> apply(@Nonnull Function<T, R> func,
                                           @Nonnull Supplier<T> supply) {
        return () -> func.apply(supply.get());
    }

    @Nonnull
    public static <T, P, R> Function<P, R> apply(@Nonnull BiFunction<T, P, R> func,
                                                  @Nonnull Supplier<T> supply) {
        return p -> func.apply(supply.get(), p);
    }

    @Nonnull
    public static <T, V> Function<T, V> nullFunction(@Nonnull Runnable run) {
        return nullFunction((v) -> run.run());
    }

    @Nonnull
    public static <T, V> Function<T, V> nullFunction(@Nonnull Consumer<T> run) {
        return (t) -> {
            run.accept(t);
            return null;
        };
    }

    @Nonnull
    public static <T> Supplier<T> nullSupplier(@Nonnull Runnable run) {
        return () -> {
            run.run();
            return null;
        };
    }

    // ---- Collection transforms ----

    @Nonnull
    public static <T, V> List<V> transformList(@Nonnull List<T> list,
                                               @Nonnull Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    @Nonnull
    public static <T, V> Set<V> transformSet(@Nonnull Set<T> set,
                                             @Nonnull Function<T, V> map) {
        return set.stream().map(map).collect(Collectors.toSet());
    }

    @Nonnull
    public static <T, V> Collection<V> transformCollection(@Nonnull Collection<T> list,
                                                           @Nonnull Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    @Nonnull
    public static <K, V, N> Map<K, N> remap(@Nonnull Map<K, V> map,
                                            @Nonnull Function<V, N> remapFct) {
        return MapStream.of(map).mapValue(remapFct).toMap();
    }

    public static <T> void mergeList(@Nonnull Collection<T> src, @Nonnull List<T> dst) {
        for (T element : src) {
            if (!dst.contains(element)) {
                dst.add(element);
            }
        }
    }

    public static <T> void cutList(@Nonnull Collection<? extends T> toRemove,
                                   @Nonnull List<T> from) {
        for (T element : toRemove) {
            from.remove(element);
        }
    }

    @Nonnull
    public static <T> List<T> copyList(@Nonnull List<T> list) {
        return new ArrayList<>(list);
    }

    @Nonnull
    public static <T> Set<T> copySet(@Nonnull Set<T> set) {
        return new HashSet<>(set);
    }

    // ---- Search / match ----

    @Nullable
    public static <T> T iterativeSearch(@Nonnull Collection<T> collection,
                                        @Nonnull Predicate<T> matchingFct) {
        for (T element : collection) {
            if (matchingFct.test(element)) {
                return element;
            }
        }
        return null;
    }

    public static <T> boolean contains(@Nonnull Collection<T> collection,
                                       @Nonnull Predicate<T> matchingFct) {
        return iterativeSearch(collection, matchingFct) != null;
    }

    public static <T> boolean matchesAny(@Nonnull T element,
                                         @Nonnull Collection<Predicate<T>> tests) {
        for (Predicate<T> test : tests) {
            if (test.test(element)) {
                return true;
            }
        }
        return false;
    }

    // ---- Fluid utilities ----

    public static boolean isFluidBlock(@Nonnull BlockState state) {
        return state.getBlock() instanceof LiquidBlock;
    }

    @Nullable
    public static Fluid tryGetFluid(@Nonnull BlockState state) {
        if (!isFluidBlock(state)) {
            return null;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            return fluidState.getType();
        }
        return null;
    }

    // ---- Player combat ----

    public static boolean canPlayerAttackServer(@Nullable LivingEntity source,
                                                @Nonnull LivingEntity target) {
        if (!target.isAlive()) {
            return false;
        }
        if (target instanceof Player plTarget) {
            if (target.level() instanceof ServerLevel serverLevel) {
                MinecraftServer srv = serverLevel.getServer();
                if (srv != null && !srv.isPvpAllowed()) {
                    return false;
                }
            }
            if (plTarget.isSpectator() || plTarget.isCreative()) {
                return false;
            }
            if (source instanceof Player sourcePlayer
                    && !sourcePlayer.canHarmPlayer(plTarget)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlayerBreakBlockPos(@Nonnull Player player,
                                                 @Nonnull BlockPos tryBreak) {
        BlockEvent.BreakEvent ev = new BlockEvent.BreakEvent(
                player.level(), tryBreak,
                player.level().getBlockState(tryBreak), player);
        MinecraftForge.EVENT_BUS.post(ev);
        return !ev.isCanceled();
    }

    public static boolean canPlayerPlaceBlockPos(@Nonnull Player player,
                                                 @Nonnull BlockState tryPlace,
                                                 @Nonnull BlockPos pos,
                                                 @Nonnull Direction againstSide) {
        Level level = player.level();
        level.captureBlockSnapshots = true;
        level.setBlock(pos, tryPlace, 3);
        level.captureBlockSnapshots = false;

        @SuppressWarnings("unchecked")
        List<BlockSnapshot> blockSnapshots =
                (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
        level.capturedBlockSnapshots.clear();

        boolean cancelPlacement = false;
        if (blockSnapshots.size() > 1) {
            cancelPlacement = ForgeEventFactory.onMultiBlockPlace(player, blockSnapshots, againstSide);
        } else if (blockSnapshots.size() == 1) {
            cancelPlacement = ForgeEventFactory.onBlockPlace(player, blockSnapshots.get(0), againstSide);
        }
        for (BlockSnapshot blocksnapshot : Lists.reverse(blockSnapshots)) {
            level.restoringBlockSnapshots = true;
            blocksnapshot.restore(true, false);
            level.restoringBlockSnapshots = false;
        }
        return !cancelPlacement;
    }

    public static boolean isConnectionEstablished(@Nonnull ServerPlayer player) {
        return player.connection != null
                && player.connection.connection != null
                && player.connection.connection.isConnected();
    }

    public static long getRandomWorldSeed(@Nonnull WorldGenLevel world) {
        return new Random(world.getSeed()).nextLong();
    }

    // ---- Hand / item lookup ----

    @Nullable
    public static Tuple<InteractionHand, ItemStack> getMainOrOffHand(
            @Nonnull LivingEntity entity, @Nonnull Item search) {
        return getMainOrOffHand(entity,
                stack -> !stack.isEmpty() && stack.getItem().equals(search));
    }

    @Nullable
    public static Tuple<InteractionHand, ItemStack> getMainOrOffHand(
            @Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> acceptorFnc) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack held = entity.getItemInHand(hand);
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            hand = InteractionHand.OFF_HAND;
            held = entity.getItemInHand(hand);
        }
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            return null;
        }
        return new Tuple<>(hand, held);
    }

    // ---- String utilities ----

    @Nullable
    public static String capitalizeFirst(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toTitleCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Teleports any entity to the given position in the target level.
     * Players use the 1.20 {@code ServerPlayer.teleportTo} API; other entities
     * use {@code changeDimension} with a {@link NoOpTeleporter}.
     */
    public static void transferEntityTo(@Nonnull Entity entity,
                                        @Nonnull ServerLevel targetLevel,
                                        @Nonnull BlockPos target) {
        if (entity instanceof ServerPlayer sp) {
            sp.teleportTo(targetLevel,
                    target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                    sp.getYRot(), sp.getXRot());
        } else {
            entity.changeDimension(targetLevel, new NoOpTeleporter(targetLevel, target));
        }
    }

    // ---- Raytrace ----

    @Nullable
    public static BlockHitResult rayTraceLookBlock(@Nonnull Player player) {
        return rayTraceLookBlock(player, player.getBlockReach());
    }

    @Nonnull
    public static HitResult rayTraceLook(@Nonnull Player player) {
        return rayTraceLook(player, player.getBlockReach());
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(
            @Nonnull Player player,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode) {
        return rayTraceLookBlock(player, blockMode, fluidMode, player.getBlockReach());
    }

    @Nonnull
    public static HitResult rayTraceLook(
            @Nonnull Player player,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode) {
        return rayTraceLook(player, blockMode, fluidMode, player.getBlockReach());
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(@Nonnull Player player, double reachDst) {
        return rayTraceLookBlock(player,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, reachDst);
    }

    @Nonnull
    public static HitResult rayTraceLook(@Nonnull Player player, double reachDst) {
        return rayTraceLook(player,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, reachDst);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(
            @Nonnull Entity entity,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode,
            double reachDst) {
        HitResult rtr = rayTraceLook(entity, blockMode, fluidMode, reachDst);
        if (rtr.getType() == HitResult.Type.BLOCK && rtr instanceof BlockHitResult blockHit) {
            return blockHit;
        }
        return null;
    }

    @Nonnull
    public static HitResult rayTraceLook(
            @Nonnull Entity entity,
            @Nonnull ClipContext.Block blockMode,
            @Nonnull ClipContext.Fluid fluidMode,
            double reachDst) {
        Vec3 pos = new Vec3(entity.getX(),
                entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 lookVec = entity.getLookAngle();
        Vec3 end = pos.add(
                lookVec.x * reachDst, lookVec.y * reachDst, lookVec.z * reachDst);
        ClipContext ctx = new ClipContext(pos, end, blockMode, fluidMode, entity);
        return entity.level().clip(ctx);
    }

    // ---- Color ----

    @Nonnull
    public static Color calcRandomConstellationColor(float perc) {
        return new Color(Color.HSBtoRGB(
                (230F + (50F * perc)) / 360F, 0.8F, 0.8F - (0.3F * perc)));
    }

    // ---- Vector3 geometry utilities ----

    @Nonnull
    public static List<Vector3> getCirclePositions(@Nonnull Vector3 centerOffset, @Nonnull Vector3 axis,
                                                    double radius, int amountOfPointsOnCircle) {
        List<Vector3> out = new LinkedList<>();
        Vector3 circleVec = axis.clone().perpendicular().normalize().multiply(radius);
        double degPerPoint = 360D / amountOfPointsOnCircle;
        for (int i = 0; i < amountOfPointsOnCircle; i++) {
            double deg = i * degPerPoint;
            out.add(circleVec.clone().rotate(Math.toRadians(deg), axis.clone()).add(centerOffset));
        }
        return out;
    }

    @Nonnull
    public static Vector3 getRandomCirclePosition(@Nonnull Vector3 centerOffset, @Nonnull Vector3 axis,
                                                   double radius) {
        return getCirclePosition(centerOffset, axis, radius, Math.random() * 360);
    }

    @Nonnull
    public static Vector3 getCirclePosition(@Nonnull Vector3 centerOffset, @Nonnull Vector3 axis,
                                             double radius, double degree) {
        Vector3 circleVec = axis.clone().perpendicular().normalize().multiply(radius);
        return circleVec.rotate(Math.toRadians(degree), axis.clone()).add(centerOffset);
    }

    @Nonnull
    public static Vector3 limitVelocityToMinecraftLimit(@Nonnull Vector3 velocity) {
        double maxDir = Math.max(Math.abs(velocity.getX()),
                Math.max(Math.abs(velocity.getY()), Math.abs(velocity.getZ())));
        if (maxDir <= 3.9) { // SEntityVelocityPacket: 3.9 * 8000 short value limit
            return velocity;
        }
        return velocity.multiply(3.9 / maxDir);
    }

    public static void applyRandomOffset(@Nonnull Vector3 target, @Nonnull Random rand) {
        applyRandomOffset(target, rand, 1F);
    }

    public static void applyRandomOffset(@Nonnull Vector3 target, @Nonnull Random rand, float multiplier) {
        target.addX(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
        target.addY(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
        target.addZ(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
    }

    public static void applyRandomCircularOffset(@Nonnull Vector3 target, @Nonnull Random rand) {
        applyRandomOffset(target, rand, 1F);
    }

    public static void applyRandomCircularOffset(@Nonnull Vector3 target, @Nonnull Random rand,
                                                  float multiplier) {
        Vector3 v = Vector3.random().normalize().multiply(rand.nextFloat() * multiplier);
        target.addX(v.getX() * (rand.nextBoolean() ? 1 : -1));
        target.addY(v.getY() * (rand.nextBoolean() ? 1 : -1));
        target.addZ(v.getZ() * (rand.nextBoolean() ? 1 : -1));
    }

    // ---- Chunk-safe execution ----

    public static void executeWithChunk(@Nonnull LevelReader world,
                                        @Nonnull ChunkPos pos,
                                        @Nonnull Runnable run) {
        executeWithChunk(world, pos.getWorldPosition(), nullSupplier(run));
    }

    public static void executeWithChunk(@Nonnull LevelReader world,
                                        @Nonnull BlockPos pos,
                                        @Nonnull Runnable run) {
        executeWithChunk(world, pos, nullSupplier(run));
    }

    @Nullable
    public static <T> T executeWithChunk(@Nonnull LevelReader world,
                                         @Nonnull BlockPos pos,
                                         @Nonnull Supplier<T> run) {
        return executeWithChunkDefault(world, pos, run, null);
    }

    @Nullable
    public static <T> T executeWithChunk(@Nonnull LevelReader world,
                                         @Nonnull BlockPos pos,
                                         @Nonnull Supplier<T> run,
                                         @Nullable T defaultValue) {
        return executeWithChunkDefault(world, pos, run, defaultValue);
    }

    /**
     * Internal implementation — separated to avoid overload ambiguity
     * with (T, Consumer) and (T, Function) when passing null default.
     */
    @Nullable
    private static <T> T executeWithChunkDefault(@Nonnull LevelReader world,
                                                 @Nonnull BlockPos pos,
                                                 @Nonnull Supplier<T> run,
                                                 @Nullable T defaultValue) {
        if (world instanceof ServerLevel serverLevel
                && LogCategory.UNINTENDED_CHUNK_LOADING.isEnabled()) {
            ServerChunkCache provider = serverLevel.getChunkSource();
            int prev = provider.getLoadedChunksCount();
            try {
                if (provider.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                    return run.get();
                }
            } finally {
                int current = serverLevel.getChunkSource().getLoadedChunksCount();
                if (current > prev) {
                    LOG.warn("Astral Sorcery loaded a chunk when it intended not to!");
                    LOG.warn("Previous chunk count: {}", prev);
                    LOG.warn("Current chunk count: {}", current);
                    LOG.warn("Loaded {} chunks!", current - prev);
                    LOG.warn("Stacktrace:", new Exception());
                }
            }
        } else if (world instanceof LevelAccessor levelAccessor) {
            ChunkSource provider = levelAccessor.getChunkSource();
            if (provider.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                return run.get();
            }
        } else {
            if (world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false) != null) {
                return run.get();
            }
        }
        return defaultValue;
    }

    public static <T> void executeWithChunk(@Nonnull LevelReader world,
                                            @Nonnull BlockPos pos,
                                            @Nonnull T obj,
                                            @Nonnull Consumer<T> run) {
        executeWithChunk(world, pos, nullSupplier(apply(run, () -> obj)));
    }

    public static <T, U> void executeWithChunk(@Nonnull LevelReader world,
                                               @Nonnull BlockPos pos,
                                               @Nonnull T obj,
                                               @Nonnull U obj1,
                                               @Nonnull BiConsumer<T, U> run) {
        executeWithChunk(world, pos, obj, apply(run, () -> obj1));
    }

    @Nullable
    public static <T, R> R executeWithChunk(@Nonnull LevelReader world,
                                            @Nonnull BlockPos pos,
                                            @Nonnull T obj,
                                            @Nonnull Function<T, R> run) {
        return executeWithChunk(world, pos, apply(run, () -> obj));
    }

    @Nullable
    public static <T, R> R executeWithChunk(@Nonnull LevelReader world,
                                            @Nonnull BlockPos pos,
                                            @Nonnull T obj,
                                            @Nonnull Function<T, R> run,
                                            @Nullable R defaultValue) {
        return executeWithChunk(world, pos, apply(run, () -> obj), defaultValue);
    }

    @Nonnull
    public static <T> Function<T, T> mapWithChunk(@Nonnull LevelReader world,
                                                  @Nonnull Function<T, BlockPos> posFn) {
        return (val) -> executeWithChunk(world, posFn.apply(val), val, Function.identity());
    }

    // ---- Random selection ----

    @SafeVarargs
    @Nullable
    public static <T> T eitherOf(@Nonnull Random r, @Nonnull T... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)];
    }

    @SafeVarargs
    @Nullable
    public static <T> T eitherOf(@Nonnull Random r, @Nonnull Supplier<T>... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)].get();
    }

    @SafeVarargs
    @Nonnull
    public static <T> Optional<T> tryMultiple(@Nonnull Supplier<T>... suppliers) {
        for (Supplier<T> supplier : suppliers) {
            try {
                return Optional.ofNullable(supplier.get());
            } catch (Exception exc) {
                LOG.error("tryMultiple failed for one supplier", exc);
            }
        }
        return Optional.empty();
    }

}
