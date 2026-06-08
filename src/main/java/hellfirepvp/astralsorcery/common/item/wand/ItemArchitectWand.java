package hellfirepvp.astralsorcery.common.item.wand;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import hellfirepvp.astralsorcery.common.item.base.client.ItemHeldRender;
import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.block.BlockGeometry;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Architect Wand — places blocks in configurable geometric patterns at the targeted
 * position. Consumes alignment charge per block placed.
 * Stored block types are drawn from the player's inventory.
 *
 * <p>1.16 → 1.20: ItemUseContext → UseOnContext, ActionResultType → InteractionResult,
 * RayTraceContext → ClipContext, BlockRayTraceResult → BlockHitResult,
 * rtr.getPos() → rtr.getBlockPos(), rtr.getFace() → rtr.getDirection(),
 * player.isSneaking() → player.isCrouching(), world.setBlockState → level.setBlock,
 * player.sendStatusMessage → player.displayClientMessage,
 * player.getHeldItem → player.getItemInHand.
 */
public class ItemArchitectWand extends ItemAS implements ItemBlockStorage, AlignmentChargeConsumer,
        ItemHeldRender, ItemOverlayRender {

    private static final float COST_PER_PLACEMENT = 8F;

    public ItemArchitectWand() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        PlaceMode mode = getPlaceMode(stack);
        return getPlayerPlaceableStates(player, stack).size() * COST_PER_PLACEMENT * mode.getPlaceCostMultiplier();
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack held = player != null ? player.getItemInHand(context.getHand()) : ItemStack.EMPTY;
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide() || !(player instanceof ServerPlayer) || held.isEmpty()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isCrouching()) {
            ItemBlockStorage.storeBlockState(held, level, pos);
            return InteractionResult.SUCCESS;
        } else {
            return attemptPlaceBlocks(level, player, held).getResult();
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isCrouching()) {
            PlaceMode nextMode = getPlaceMode(held).next();
            setPlaceMode(held, nextMode);
            player.displayClientMessage(nextMode.getDisplay(), true);
            return InteractionResultHolder.success(held);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(held);
        }
        return attemptPlaceBlocks(level, player, held);
    }

    private InteractionResultHolder<ItemStack> attemptPlaceBlocks(Level level, Player player, ItemStack held) {
        Map<BlockPos, BlockState> placeStates = getPlayerPlaceableStates(player, held);
        if (placeStates.isEmpty()) {
            return InteractionResultHolder.fail(held);
        }

        Map<BlockState, Tuple<ItemStack, Integer>> availableStacks = MapStream.of(ItemBlockStorage.getInventoryMatching(player, held))
                .filter(tpl -> placeStates.containsValue(tpl.getA()))
                .collect(Collectors.toMap(Tuple::getA, Tuple::getB));

        for (Map.Entry<BlockPos, BlockState> placeEntry : placeStates.entrySet()) {
            BlockPos placePos = placeEntry.getKey();
            BlockState stateToPlace = placeEntry.getValue();
            Tuple<ItemStack, Integer> available = availableStacks.get(stateToPlace);
            if (available == null) continue;

            ItemStack extractable = ItemUtils.copyStackWithSize(available.getA(), 1);
            boolean canExtract = player.isCreative();
            if (!canExtract && ItemUtils.consumeFromPlayerInventory(player, held, extractable, true)) {
                canExtract = true;
            }
            if (!canExtract) continue;

            if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_PLACEMENT, true) &&
                    (player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, held, extractable, true)) &&
                    MiscUtils.canPlayerPlaceBlockPos(player, stateToPlace, placePos, Direction.UP) &&
                    (player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, held, extractable, false)) &&
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_PLACEMENT, false)) {
                level.setBlock(placePos, stateToPlace, Block.UPDATE_ALL);
            }
        }
        return InteractionResultHolder.success(held);
    }

    @Nonnull
    private Map<BlockPos, BlockState> getPlayerPlaceableStates(Player player, ItemStack stack) {
        PlaceMode mode = getPlaceMode(stack);
        Level level = player.level();

        BlockHitResult rtr = MiscUtils.rayTraceLookBlock(player, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, 60F);
        if (rtr == null && mode.needsOffset()) {
            return new HashMap<>();
        }

        if (rtr != null && rtr.getType() != HitResult.Type.MISS) {
            Direction placingAgainst = rtr.getDirection();
            BlockPos at = rtr.getBlockPos().relative(rtr.getDirection());
            return getPlaceStates(player, level, at, placingAgainst, stack);
        } else {
            return getPlaceStates(player, level, null, null, stack);
        }
    }

    @Nonnull
    private Map<BlockPos, BlockState> getPlaceStates(Player placer, Level level,
                                                      @Nullable BlockPos origin,
                                                      @Nullable Direction placingAgainst,
                                                      ItemStack refStack) {
        Map<BlockState, Tuple<ItemStack, Integer>> tplStates = ItemBlockStorage.getInventoryMatching(placer, refStack);
        PlaceMode placeMode = getPlaceMode(refStack);
        Map<BlockPos, BlockState> placeables = Maps.newHashMap();

        int totalItems;
        if (placer.isCreative()) {
            totalItems = Integer.MAX_VALUE;
        } else {
            totalItems = 0;
            for (Tuple<ItemStack, Integer> t : tplStates.values()) {
                totalItems += (t.getB() == -1 ? 500_000 : t.getB());
            }
        }

        List<BlockPos> foundPositions = placeMode.generatePlacementPositions(level, placer, placingAgainst, origin);
        if (foundPositions.isEmpty()) return placeables;
        foundPositions = foundPositions.subList(0, Math.min(foundPositions.size(), totalItems));

        Map<BlockState, Integer> placeAmounts = Maps.newHashMap();
        for (Map.Entry<BlockState, Tuple<ItemStack, Integer>> tplEntry : tplStates.entrySet()) {
            placeAmounts.put(tplEntry.getKey(), placer.isCreative() ? Integer.MAX_VALUE : tplEntry.getValue().getB());
        }
        List<BlockState> placeableStates = Lists.newArrayList(placeAmounts.keySet());
        Random rand = ItemBlockStorage.getPreviewRandomFromWorld(level);

        for (BlockPos pos : foundPositions) {
            if (placeableStates.isEmpty()) continue;
            Collections.shuffle(placeableStates, rand);
            BlockState toPlace = placeableStates.get(0);

            MiscUtils.executeWithChunk(level, pos, () -> {
                if (BlockUtils.isReplaceable(level, pos)) {
                    if (!placer.isCreative()) {
                        int count = placeAmounts.getOrDefault(toPlace, 0) - 1;
                        if (count <= 0) {
                            placeAmounts.remove(toPlace);
                            placeableStates.remove(toPlace);
                        } else {
                            placeAmounts.put(toPlace, count);
                        }
                    }
                    placeables.put(pos, toPlace);
                }
            });
        }
        return placeables;
    }

    public static void setPlaceMode(@Nonnull ItemStack stack, @Nonnull PlaceMode mode) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemArchitectWand)) return;
        NBTHelper.getPersistentData(stack).putInt("placeMode", mode.ordinal());
    }

    @Nonnull
    public static PlaceMode getPlaceMode(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemArchitectWand)) {
            return PlaceMode.TOWARDS_PLAYER;
        }
        return MiscUtils.getEnumEntry(PlaceMode.class,
                NBTHelper.getPersistentData(stack).getInt("placeMode"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderInHand(ItemStack stack, PoseStack poseStack, float partialTick) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return true;
        Map<BlockPos, BlockState> preview = getPlayerPlaceableStates(player, stack);
        if (preview.isEmpty()) return true;
        hellfirepvp.astralsorcery.client.util.WandRenderHelper.renderGhostBlocks(preview, poseStack);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderOverlay(@Nonnull GuiGraphics graphics, @Nonnull ItemStack stack, float partialTick) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        return hellfirepvp.astralsorcery.client.util.WandRenderHelper.renderStoredBlocksOverlay(
                graphics, ItemBlockStorage.getInventoryMatchingItemStacks(player, stack));
    }

    public enum PlaceMode {

        TOWARDS_PLAYER("towards", true, 3F) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player,
                                                              @Nullable Direction placedAgainst,
                                                              @Nullable BlockPos center) {
                if (placedAgainst == null || center == null) return Lists.newLinkedList();
                List<BlockPos> blocks = new ArrayList<>();
                double cmpFrom, cmpTo;
                switch (placedAgainst.getAxis()) {
                    case X -> { cmpFrom = center.getX(); cmpTo = player.getX(); }
                    case Y -> { cmpFrom = center.getY(); cmpTo = player.getY(); }
                    case Z -> { cmpFrom = center.getZ(); cmpTo = player.getZ(); }
                    default -> { return Lists.newLinkedList(); }
                }
                int length = (int) Math.min(20, Math.abs(cmpFrom + 0.5 - cmpTo));
                for (int i = 0; i < length; i++) {
                    BlockPos at = center.relative(placedAgainst, i);
                    if (Boolean.TRUE.equals(MiscUtils.executeWithChunk(level, at, () -> !BlockUtils.isReplaceable(level, at), true))) break;
                    blocks.add(at);
                }
                return blocks;
            }
        },
        FROM_PLAYER("line", false) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player,
                                                              @Nullable Direction placedAgainst,
                                                              @Nullable BlockPos center) {
                HitResult result = player.pick(60F, 1F, false);
                BlockPos hit = result instanceof BlockHitResult bhr ? bhr.getBlockPos() : BlockPos.containing(result.getLocation());
                List<BlockPos> line = new ArrayList<>();
                new RaytraceAssist(
                        hellfirepvp.astralsorcery.common.util.data.Vector3.atEntityCorner(player),
                        new hellfirepvp.astralsorcery.common.util.data.Vector3(hit.getX(), hit.getY(), hit.getZ())
                ).forEachBlockPos(pos -> MiscUtils.executeWithChunk(level, pos, () -> {
                    if (BlockUtils.isReplaceable(level, pos)) { line.add(pos); return true; }
                    return false;
                }, false));
                return line;
            }
        },
        H_PLANE("plane", true) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player,
                                                              @Nullable Direction placedAgainst,
                                                              @Nullable BlockPos center) {
                if (center == null) return Collections.emptyList();
                return MiscUtils.transformList(BlockGeometry.getPlane(Direction.UP, 5), at -> at.offset(center));
            }
        },
        V_PLANE("wall", true) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player,
                                                              @Nullable Direction placedAgainst,
                                                              @Nullable BlockPos center) {
                if (center == null) return Collections.emptyList();
                return MiscUtils.transformList(BlockGeometry.getPlane(player.getDirection(), 5), at -> at.offset(center));
            }
        },
        SPHERE("sphere", true, 0.2F) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player,
                                                              @Nullable Direction placedAgainst,
                                                              @Nullable BlockPos center) {
                if (center == null) return Collections.emptyList();
                return MiscUtils.transformList(BlockGeometry.getSphere(5), at -> at.offset(center));
            }
        },
        SPHERE_HOLLOW("sphere_hollow", true, 0.5F) {
            @Override
            public List<BlockPos> generatePlacementPositions(Level level, Player player,
                                                              @Nullable Direction placedAgainst,
                                                              @Nullable BlockPos center) {
                if (center == null) return Collections.emptyList();
                return MiscUtils.transformList(BlockGeometry.getHollowSphere(5, 4), at -> at.offset(center));
            }
        };

        private final String name;
        private final boolean needsOffset;
        private final float placeCostMultiplier;

        PlaceMode(String name, boolean needsOffset) {
            this(name, needsOffset, 1F);
        }

        PlaceMode(String name, boolean needsOffset, float placeCostMultiplier) {
            this.name = name;
            this.needsOffset = needsOffset;
            this.placeCostMultiplier = placeCostMultiplier;
        }

        public Component getName() {
            return Component.translatable("astralsorcery.misc.architect.mode." + this.name);
        }

        public Component getDisplay() {
            return Component.translatable("astralsorcery.misc.architect.mode", this.getName());
        }

        public float getPlaceCostMultiplier() {
            return placeCostMultiplier;
        }

        public boolean needsOffset() {
            return needsOffset;
        }

        public abstract List<BlockPos> generatePlacementPositions(Level level, Player player,
                                                                   @Nullable Direction placedAgainst,
                                                                   @Nullable BlockPos center);

        @Nonnull
        PlaceMode next() {
            int next = (this.ordinal() + 1) % values().length;
            return MiscUtils.getEnumEntry(PlaceMode.class, next);
        }
    }
}
