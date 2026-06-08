package hellfirepvp.astralsorcery.common.item.lens;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.BlockBreakHelper;
import hellfirepvp.astralsorcery.common.auxiliary.CropHelper;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityLens;
import hellfirepvp.astralsorcery.common.util.DamageSourceAS;
import hellfirepvp.astralsorcery.common.util.tile.TileUtils;
import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Colored Lens — modifies the behavior of a Lens block in the starlight network.
 * Each color provides a different effect on blocks or entities in the beam path.
 *
 * <p>Beam effects are dispatched via {@link LensColor#entityInBeam} and
 * {@link LensColor#blockInBeam}, called by {@link hellfirepvp.astralsorcery.common.tile.BlockEntityLens}
 * on each server tick when starlight is flowing through the lens.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * Separate ItemColoredLens* subclass hierarchy collapsed into enum constants;
 * LensColorType abstract class replaced by LensColor enum with abstract methods;
 * World -> Level, Entity.setFire -> Entity.setSecondsOnFire,
 * DamageSource.ON_FIRE -> damageSources().onFire(),
 * Entity.getMotion/setMotion -> getDeltaMovement/setDeltaMovement.</p>
 */
public class ItemColoredLens extends ItemAS {

    private final LensColor lensColor;

    public ItemColoredLens(@Nonnull LensColor lensColor) {
        super(defaultProperties().stacksTo(16));
        this.lensColor = lensColor;
    }

    @Nonnull
    public LensColor getLensColor() {
        return lensColor;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nullable Level level,
                                @Nonnull List<Component> tooltip,
                                @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(
                "astralsorcery.tooltip.colored_lens.type",
                Component.translatable("astralsorcery.lens.color." + lensColor.getSerializedName())));
    }

    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.PASS;
        }

        BlockEntityLens lens = TileUtils.getTileAt(level, ctx.getClickedPos(), BlockEntityLens.class, false);
        if (lens == null) {
            return InteractionResult.PASS;
        }

        ResourceLocation newOverlay = AstralSorcery.key(lensColor.getSerializedName());
        ResourceLocation oldOverlay = lens.getColorOverlay();

        if (oldOverlay != null) {
            // overlay path is the color name (e.g. "fire"); item name is "colored_lens_fire"
            Item oldItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(
                    "astralsorcery", "colored_lens_" + oldOverlay.getPath()));
            if (oldItem != null) {
                ItemStack ret = new ItemStack(oldItem);
                if (!player.getInventory().add(ret)) {
                    ItemUtils.dropItemNaturally(level,
                            player.getX(), player.getY(), player.getZ(), ret);
                }
            }
        }

        lens.setColorOverlay(newOverlay);

        ItemStack held = ctx.getItemInHand();
        if (!player.isCreative()) {
            held.shrink(1);
        }

        level.playSound(null, ctx.getClickedPos(),
                SoundsAS.BLOCK_COLOREDLENS_ATTACH.get(),
                net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.5F);

        return InteractionResult.SUCCESS;
    }

    @Nullable
    public static LensColor fromOverlay(@Nullable ResourceLocation overlay) {
        if (overlay == null) return null;
        String path = overlay.getPath();
        for (LensColor lc : LensColor.values()) {
            if (lc.getSerializedName().equals(path)) return lc;
        }
        return null;
    }

    /** Whether the lens color affects blocks, entities, both, or neither. */
    public enum TargetType {
        ANY, ENTITY, BLOCK, NONE;

        public boolean doEntityInteraction() { return this == ANY || this == ENTITY; }
        public boolean doBlockInteraction()  { return this == ANY || this == BLOCK; }
    }

    public enum LensColor implements StringRepresentable {

        FIRE(new Color(255, 90, 0), TargetType.ANY) {
            @Override
            public void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                     @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec) {
                if (level.isClientSide()) return;
                if (entity instanceof ItemEntity itemEntity) {
                    ItemStack current = itemEntity.getItem();
                    Optional<net.minecraft.world.item.crafting.SmeltingRecipe> result =
                            findSmelt(level, current);
                    if (result.isEmpty()) return;
                    ItemStack smelted = result.get().getResultItem(level.registryAccess());
                    while (exec.canExecute()) {
                        exec.markExecution();
                        if (level.random.nextInt(10) != 0) continue;
                        ItemUtils.dropItemNaturally(level,
                                entity.getX(), entity.getY(), entity.getZ(),
                                smelted.copy());
                        if (current.getCount() > 1) {
                            current.shrink(1);
                            itemEntity.setItem(current);
                        } else {
                            entity.discard();
                        }
                        return;
                    }
                } else if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    var server = level.getServer();
                    if (living instanceof Player && (server == null || !server.isPvpAllowed())) return;
                    DamageSource fire = level.damageSources().onFire();
                    exec.executeAll(() -> {
                        living.hurt(fire, 0.5F);
                        living.setSecondsOnFire(5);
                    });
                }
            }

            @Override
            public void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                    @Nonnull PartialEffectExecutor exec) {
                if (level.isClientSide()) return;
                ItemStack blockStack = ItemUtils.createBlockStack(state);
                if (blockStack.isEmpty()) return;
                Optional<net.minecraft.world.item.crafting.SmeltingRecipe> result = findSmelt(level, blockStack);
                if (result.isEmpty()) return;
                ItemStack smelted = result.get().getResultItem(level.registryAccess());
                exec.executeAll(() -> {
                    if (level.random.nextInt(6) != 0) return;
                    BlockState resState = ItemUtils.createBlockState(smelted);
                    if (resState != null) {
                        level.setBlock(pos, resState, 3);
                    } else {
                        level.removeBlock(pos, false);
                        ItemUtils.dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, smelted.copy());
                    }
                });
            }

            private Optional<net.minecraft.world.item.crafting.SmeltingRecipe> findSmelt(Level level, ItemStack stack) {
                return level.getRecipeManager()
                        .getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMELTING)
                        .stream()
                        .filter(r -> r.getIngredients().stream().anyMatch(ing -> ing.test(stack)))
                        .findFirst();
            }
        },

        BREAK(new Color(160, 160, 160), TargetType.BLOCK) {
            @Override
            public void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                     @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec) {}

            @Override
            public void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                    @Nonnull PartialEffectExecutor exec) {
                if (level.isClientSide() || !(level instanceof net.minecraft.server.level.ServerLevel sl)) return;
                float hardness = state.getDestroySpeed(level, pos);
                if (hardness < 0) return; // unbreakable
                exec.executeAll(() -> BlockBreakHelper.breakBlock(sl, pos, true));
            }
        },

        GROWTH(new Color(60, 200, 80), TargetType.BLOCK) {
            @Override
            public void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                     @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec) {}

            @Override
            public void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                    @Nonnull PartialEffectExecutor exec) {
                if (level.isClientSide() || !(level instanceof net.minecraft.server.level.ServerLevel sl)) return;
                if (!CropHelper.isGrowableCrop(level, pos)) return;
                exec.executeAll(() -> {
                    if (level.random.nextInt(18) != 0) return;
                    CropHelper.tryGrowCrop(sl, pos);
                });
            }
        },

        DAMAGE(new Color(200, 30, 30), TargetType.ENTITY) {
            @Override
            public void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                     @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec) {
                if (level.isClientSide() || !(entity instanceof net.minecraft.world.entity.LivingEntity living)) return;
                var server = level.getServer();
                if (living instanceof Player && (server == null || !server.isPvpAllowed())) return;
                exec.executeAll(() -> living.hurt(DamageSourceAS.stellar(level), 1.5F));
            }

            @Override
            public void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                    @Nonnull PartialEffectExecutor exec) {}
        },

        REGENERATION(new Color(100, 220, 60), TargetType.ENTITY) {
            @Override
            public void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                     @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec) {
                if (level.isClientSide() || !(entity instanceof net.minecraft.world.entity.LivingEntity living)) return;
                if (!living.isAlive()) return;
                var server = level.getServer();
                if (living instanceof Player && (server == null || !server.isPvpAllowed())) return;
                exec.executeAll(() -> {
                    if (level.random.nextInt(8) != 0) return;
                    if (living.isDeadOrDying()) return;
                    if (living.getMobType() == net.minecraft.world.entity.MobType.UNDEAD) {
                        living.hurt(DamageSourceAS.stellar(level), 0.5F);
                    } else {
                        living.heal(0.5F);
                    }
                });
            }

            @Override
            public void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                    @Nonnull PartialEffectExecutor exec) {}
        },

        PUSH(new Color(30, 200, 200), TargetType.ENTITY) {
            @Override
            public void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                     @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec) {
                if (!exec.canExecute()) return;
                if (level.isClientSide()) return;
                var server = level.getServer();
                if (entity instanceof Player && (server == null || !server.isPvpAllowed())) return;
                Vec3 dir = target.subtract(origin).normalize().scale(0.4);
                Vec3 cur = entity.getDeltaMovement();
                entity.setDeltaMovement(
                        Math.min(1.0, cur.x + dir.x),
                        dir.y + 0.04,
                        Math.min(1.0, cur.z + dir.z));
            }

            @Override
            public void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                    @Nonnull PartialEffectExecutor exec) {}
        },

        SPECTRAL(new Color(180, 80, 255), TargetType.NONE) {
            @Override
            public void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                     @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec) {}

            @Override
            public void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                    @Nonnull PartialEffectExecutor exec) {}
        };

        @Nonnull private final Color beamColor;
        @Nonnull private final TargetType targetType;

        LensColor(@Nonnull Color beamColor, @Nonnull TargetType targetType) {
            this.beamColor = beamColor;
            this.targetType = targetType;
        }

        @Nonnull
        public Color getBeamColor() { return beamColor; }

        @Nonnull
        public TargetType getTargetType() { return targetType; }

        public abstract void entityInBeam(@Nonnull Level level, @Nonnull Vec3 origin, @Nonnull Vec3 target,
                                          @Nonnull Entity entity, @Nonnull PartialEffectExecutor exec);

        public abstract void blockInBeam(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                                         @Nonnull PartialEffectExecutor exec);

        @Override
        @Nonnull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
