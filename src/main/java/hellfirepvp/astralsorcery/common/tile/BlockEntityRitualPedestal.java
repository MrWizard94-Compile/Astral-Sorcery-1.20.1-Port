package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Block entity for the Ritual Pedestal.
 * Holds an attuned crystal to produce constellation-based area effects.
 * The active effect depends on the crystal's attuned constellation.
 *
 * <p>Uses a held crystal ItemStack field rather than TileInventory,
 * since only a single crystal can be placed on the pedestal at a time.</p>
 *
 * <p>Note: Will implement starlight network interfaces in a later phase
 * to receive starlight for powering rituals.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * ResourceLocation for constellation keying</p>
 */
public class BlockEntityRitualPedestal extends BlockEntityTick implements IStarlightReceiver {

    private static final int DEFAULT_EFFECT_RANGE = 16;

    /** Maximum starlight the pedestal can store for ritual operation. */
    private static final double STARLIGHT_CAPACITY = 5000.0;

    /** Starlight consumed per application tick. */
    private static final double STARLIGHT_DRAIN_PER_TICK = 2.0;

    /** How often to check multiblock validity (ticks). */
    private static final int STRUCTURE_CHECK_INTERVAL = 60;

    /** How often to apply ritual effects (ticks). */
    private static final int EFFECT_INTERVAL = 40;

    @Nonnull
    private ItemStack heldCrystal = ItemStack.EMPTY;

    @Nullable
    private ResourceLocation attunedConstellation = null;

    private int ticksExisted = 0;
    private boolean ritualActive = false;
    private int effectRange = DEFAULT_EFFECT_RANGE;
    private boolean hasMultiblock = false;
    private double storedStarlight = 0;
    private boolean registeredInNetwork = false;

    public BlockEntityRitualPedestal(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.RITUAL_PEDESTAL.get(), pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isClientSide() && registeredInNetwork) {
            StarlightNetworkHelper.removeNode(getLevel(), getBlockPos());
            registeredInNetwork = false;
        }
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide() && !registeredInNetwork) {
            StarlightNetworkHelper.registerReceiver(getLevel(), getBlockPos(), this);
            registeredInNetwork = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // Client-side: ritual effect particles handled by renderer
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Periodically check multiblock structure
        if (ticksExisted % STRUCTURE_CHECK_INTERVAL == 0) {
            hasMultiblock = validateStructure();
        }

        // Activation conditions: valid structure, crystal present, constellation attuned
        boolean shouldBeActive = hasMultiblock
                && !heldCrystal.isEmpty()
                && attunedConstellation != null
                && storedStarlight > STARLIGHT_DRAIN_PER_TICK;

        if (shouldBeActive != ritualActive) {
            ritualActive = shouldBeActive;
            markForUpdate();
        }

        if (!ritualActive) {
            return;
        }

        // Drain starlight for operation
        storedStarlight -= STARLIGHT_DRAIN_PER_TICK;

        // Apply ritual effects at interval
        if (ticksExisted % EFFECT_INTERVAL == 0) {
            applyConstellationEffect((ServerLevel) level);
        }
    }

    /**
     * Applies the constellation-specific ritual effect to the area.
     * Each constellation provides a unique beneficial area effect.
     */
    private void applyConstellationEffect(@Nonnull ServerLevel level) {
        if (attunedConstellation == null) return;

        String path = attunedConstellation.getPath();
        AABB area = new AABB(worldPosition).inflate(effectRange);

        switch (path) {
            case "aevitas" -> applyAevitasEffect(level, area);
            case "discidia" -> applyDiscidiaEffect(level, area);
            case "armara" -> applyArmaraEffect(level, area);
            case "vicio" -> applyVicioEffect(level, area);
            case "evorsio" -> applyEvorsioEffect(level, area);
            case "lucerna" -> applyLucernaEffect(level, area);
            case "mineralis" -> applyMineralisEffect(level, area);
            case "horologium" -> applyHorologiumEffect(level, area);
            case "octans" -> applyOctansEffect(level, area);
            case "bootes" -> applyBootesEffect(level, area);
            case "fornax" -> applyFornaxEffect(level, area);
            case "pelotrio" -> applyPelotrioEffect(level, area);
            default -> AstralSorcery.log.debug(
                    "No ritual effect defined for constellation: {}", path);
        }
    }

    // ---- Constellation-specific effects ----

    /** Aevitas: Growth — accelerate crops and heal living entities. */
    private void applyAevitasEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        // Heal nearby players and animals
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class, area,
                e -> e instanceof Player || e instanceof Animal);
        for (LivingEntity entity : entities) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(1.0F);
            }
        }

        // Grow random crops in range
        int attempts = 3;
        for (int i = 0; i < attempts; i++) {
            BlockPos cropPos = worldPosition.offset(
                    level.getRandom().nextIntBetweenInclusive(-effectRange, effectRange),
                    level.getRandom().nextIntBetweenInclusive(-3, 3),
                    level.getRandom().nextIntBetweenInclusive(-effectRange, effectRange));
            BlockState state = level.getBlockState(cropPos);
            if (state.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget(level, cropPos, state, false)) {
                    growable.performBonemeal(level, level.getRandom(), cropPos, state);
                }
            }
        }
    }

    /** Discidia: Damage — hurt hostile mobs in the area. */
    private void applyDiscidiaEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, area);
        for (Monster monster : monsters) {
            monster.hurt(level.damageSources().magic(), 3.0F);
        }
    }

    /** Armara: Protection — give nearby players resistance. */
    private void applyArmaraEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, true, false));
        }
    }

    /** Vicio: Speed — give nearby players speed and jump boost. */
    private void applyVicioEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 80, 1, true, false));
        }
    }

    /** Evorsio: Destruction — break random blocks in the area (mining). */
    private void applyEvorsioEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        // Break a random weak block in range (like stone)
        BlockPos target = worldPosition.offset(
                level.getRandom().nextIntBetweenInclusive(-effectRange, effectRange),
                level.getRandom().nextIntBetweenInclusive(-3, 3),
                level.getRandom().nextIntBetweenInclusive(-effectRange, effectRange));
        BlockState state = level.getBlockState(target);
        if (!state.isAir() && state.getDestroySpeed(level, target) >= 0
                && state.getDestroySpeed(level, target) <= 3.0F) {
            level.destroyBlock(target, true);
        }
    }

    /** Lucerna: Light — prevent mob spawning by boosting light. */
    private void applyLucernaEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 80, 0, true, false));
        }
    }

    /** Mineralis: Ore reveal — give glowing to reveal nearby ores (simplified). */
    private void applyMineralisEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        // Simplified: give nearby players Luck effect for better drops
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 80, 1, true, false));
        }
    }

    /** Horologium: Time — accelerate nearby block entity ticks. */
    private void applyHorologiumEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        // Simplified: speed up crops (similar to Aevitas but faster)
        int attempts = 6;
        for (int i = 0; i < attempts; i++) {
            BlockPos cropPos = worldPosition.offset(
                    level.getRandom().nextIntBetweenInclusive(-effectRange, effectRange),
                    level.getRandom().nextIntBetweenInclusive(-3, 3),
                    level.getRandom().nextIntBetweenInclusive(-effectRange, effectRange));
            BlockState state = level.getBlockState(cropPos);
            if (state.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget(level, cropPos, state, false)) {
                    growable.performBonemeal(level, level.getRandom(), cropPos, state);
                }
            }
        }
    }

    /** Octans: Water — give water breathing and fishing luck. */
    private void applyOctansEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 80, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 80, 2, true, false));
        }
    }

    /** Bootes: Animal — breed and grow nearby animals. */
    private void applyBootesEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);
        for (Animal animal : animals) {
            if (animal.isBaby()) {
                animal.ageUp(60); // Accelerate growth
            }
        }
    }

    /** Fornax: Fire — smelt items on ground, give fire resistance. */
    private void applyFornaxEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0, true, false));
        }
    }

    /** Pelotrio: Fertility — spawn passive mobs occasionally. */
    private void applyPelotrioEffect(@Nonnull ServerLevel level, @Nonnull AABB area) {
        // Simplified: give regeneration to nearby animals
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);
        for (Animal animal : animals) {
            if (animal.getHealth() < animal.getMaxHealth()) {
                animal.heal(2.0F);
            }
        }
    }

    /**
     * Validates the ritual pedestal multiblock structure.
     */
    private boolean validateStructure() {
        Level level = getLevel();
        if (level == null) return false;

        // Must have sky access
        if (!level.canSeeSky(worldPosition.above(2))) {
            return false;
        }

        // Check for support pillars at ±2 positions
        BlockPos[] pillarPositions = {
                worldPosition.offset(2, 0, 0),
                worldPosition.offset(-2, 0, 0),
                worldPosition.offset(0, 0, 2),
                worldPosition.offset(0, 0, -2)
        };

        for (BlockPos pillar : pillarPositions) {
            if (level.getBlockState(pillar).isAir()) {
                return false;
            }
        }
        return true;
    }

    // ========================================================================
    // IStarlightReceiver implementation
    // ========================================================================

    @Override
    public void receiveStarlight(double amount, @Nullable ResourceLocation constellation) {
        double space = STARLIGHT_CAPACITY - storedStarlight;
        if (space > 0) {
            storedStarlight += Math.min(amount, space);
            setChanged();
        }
    }

    @Override
    public double getMaxStarlightInput() {
        return STARLIGHT_CAPACITY * 0.1;
    }

    @Nullable
    @Override
    public Level getReceiverLevel() {
        return getLevel();
    }

    @Nonnull
    @Override
    public BlockPos getLocationPos() {
        return getBlockPos();
    }

    @Nonnull
    public ItemStack getHeldCrystal() {
        return heldCrystal;
    }

    public void setHeldCrystal(@Nonnull ItemStack stack) {
        this.heldCrystal = stack;
        markForUpdate();
    }

    @Nullable
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        markForUpdate();
    }

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean isRitualActive() {
        return ritualActive;
    }

    public int getEffectRange() {
        return effectRange;
    }

    public void setEffectRange(int range) {
        this.effectRange = Math.max(1, range);
    }

    public boolean hasMultiblock() {
        return hasMultiblock;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.heldCrystal = compound.contains("heldCrystal")
                ? ItemStack.of(compound.getCompound("heldCrystal"))
                : ItemStack.EMPTY;
        if (compound.contains("attunedConstellation")) {
            this.attunedConstellation = new ResourceLocation(compound.getString("attunedConstellation"));
        } else {
            this.attunedConstellation = null;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (!heldCrystal.isEmpty()) {
            CompoundTag crystalTag = new CompoundTag();
            heldCrystal.save(crystalTag);
            compound.put("heldCrystal", crystalTag);
        }
        if (attunedConstellation != null) {
            compound.putString("attunedConstellation", attunedConstellation.toString());
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.ritualActive = compound.getBoolean("ritualActive");
        this.effectRange = compound.contains("effectRange")
                ? compound.getInt("effectRange") : DEFAULT_EFFECT_RANGE;
        this.hasMultiblock = compound.getBoolean("hasMultiblock");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putBoolean("ritualActive", ritualActive);
        compound.putInt("effectRange", effectRange);
        compound.putBoolean("hasMultiblock", hasMultiblock);
    }
}
