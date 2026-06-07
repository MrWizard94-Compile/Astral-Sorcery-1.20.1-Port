package hellfirepvp.astralsorcery.common.item.wand;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.item.base.client.ItemHeldRender;
import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exchange Wand — replaces a connected group of matching blocks with the stored block
 * type(s) from the player's inventory. Consumes alignment charge per block exchanged.
 *
 * <p>1.16 → 1.20: ItemUseContext → UseOnContext, ActionResultType → InteractionResult,
 * ToolType → removed (canHarvestBlock uses tags in 1.20), getBlockHardness → getDestroySpeed,
 * ServerPlayerEntity.interactionManager.tryHarvestBlock → ServerPlayer.gameMode.destroyBlock,
 * WandsConfig.exchangeWandMaxHardness → hardcoded default (50F).</p>
 */
public class ItemExchangeWand extends ItemAS implements ItemBlockStorage, AlignmentChargeConsumer,
        ItemHeldRender, ItemOverlayRender {

    private static final float COST_PER_EXCHANGE = 5F;
    /** 1.16 used WandsConfig.CONFIG.exchangeWandMaxHardness; hardcoded default here. */
    private static final float MAX_EXCHANGE_HARDNESS = 50F;

    public ItemExchangeWand() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        BlockHitResult hitResult = MiscUtils.rayTraceLookBlock(player,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        if (hitResult == null) return 0F;
        return getPlaceStates(player, player.level(), hitResult.getBlockPos(), stack).size() * COST_PER_EXCHANGE;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || stack.isEmpty()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isCrouching()) {
            ItemBlockStorage.storeBlockState(stack, level, pos);
            return InteractionResult.SUCCESS;
        }

        Map<BlockPos, BlockState> placeStates = getPlaceStates(player, level, pos, stack);
        Map<BlockState, Tuple<ItemStack, Integer>> availableStacks = MapStream.of(ItemBlockStorage.getInventoryMatching(player, stack))
                .filter(tpl -> placeStates.containsValue(tpl.getA()))
                .collect(Collectors.toMap(Tuple::getA, Tuple::getB));

        for (BlockPos placePos : placeStates.keySet()) {
            BlockState stateToPlace = placeStates.get(placePos);
            Tuple<ItemStack, Integer> available = availableStacks.get(stateToPlace);
            if (available == null) continue;

            ItemStack extractable = ItemUtils.copyStackWithSize(available.getA(), 1);
            boolean canExtract = player.isCreative();
            if (!canExtract && ItemUtils.consumeFromPlayerInventory(player, stack, extractable, true)) {
                canExtract = true;
            }
            if (!canExtract) continue;

            if ((player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, stack, extractable, true)) &&
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_EXCHANGE, false) &&
                    serverPlayer.gameMode.destroyBlock(placePos) &&
                    MiscUtils.canPlayerPlaceBlockPos(player, stateToPlace, placePos, Direction.UP) &&
                    (player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, stack, extractable, false))) {
                level.setBlock(placePos, stateToPlace, Block.UPDATE_ALL);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isCrouching()) {
            SizeMode nextMode = getSizeMode(held).next();
            setSizeMode(held, nextMode);
            player.displayClientMessage(nextMode.getDisplay(), true);
        }
        return InteractionResultHolder.success(held);
    }

    @Nonnull
    private Map<BlockPos, BlockState> getPlaceStates(Player placer, Level level,
                                                      BlockPos origin, ItemStack refStack) {
        Map<BlockState, Tuple<ItemStack, Integer>> tplStates = ItemBlockStorage.getInventoryMatching(placer, refStack);
        BlockState atState = level.getBlockState(origin);
        SizeMode mode = getSizeMode(refStack);
        Map<BlockPos, BlockState> placeables = Maps.newHashMap();

        BlockState match = BlockUtils.getMatchingState(tplStates.keySet(), atState);
        if (match != null && tplStates.size() <= 1) return placeables;

        float hardness = atState.getDestroySpeed(level, origin);
        if (hardness < 0 || hardness > MAX_EXCHANGE_HARDNESS) return placeables;

        int totalItems;
        if (placer.isCreative()) {
            totalItems = Integer.MAX_VALUE;
        } else {
            totalItems = 0;
            for (Tuple<ItemStack, Integer> t : tplStates.values()) {
                totalItems += (t.getB() == -1 ? 500_000 : t.getB());
            }
        }

        List<BlockPos> foundPositions = BlockDiscoverer.discoverBlocksWithSameStateAround(
                level, origin, true, mode.getSearchRadius(), totalItems, false);
        if (foundPositions.isEmpty()) return placeables;

        Map<BlockState, Integer> placeAmounts = Maps.newHashMap();
        for (BlockState state : tplStates.keySet()) {
            placeAmounts.put(state, placer.isCreative() ? Integer.MAX_VALUE : tplStates.get(state).getB());
        }
        List<BlockState> placeableStates = Lists.newArrayList(placeAmounts.keySet());
        Random rand = ItemBlockStorage.getPreviewRandomFromWorld(level);

        for (BlockPos pos : foundPositions) {
            if (placeableStates.isEmpty()) continue;
            Collections.shuffle(placeableStates, rand);
            BlockState toPlace = placeableStates.get(0);

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
        return placeables;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderInHand(ItemStack stack, PoseStack poseStack, float partialTick) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return true;
        BlockHitResult hit = MiscUtils.rayTraceLookBlock(player, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        if (hit == null) return true;
        Map<BlockPos, BlockState> preview = getPlaceStates(player, player.level(), hit.getBlockPos(), stack);
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

    public static void setSizeMode(@Nonnull ItemStack stack, @Nonnull SizeMode mode) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemExchangeWand)) return;
        NBTHelper.getPersistentData(stack).putInt("sizeMode", mode.ordinal());
    }

    @Nonnull
    public static SizeMode getSizeMode(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemExchangeWand)) {
            return SizeMode.RANGE_2;
        }
        return MiscUtils.getEnumEntry(SizeMode.class,
                NBTHelper.getPersistentData(stack).getInt("sizeMode"));
    }

    public enum SizeMode {

        RANGE_2(2),
        RANGE_3(3),
        RANGE_4(4),
        RANGE_5(5);

        private final int searchRadius;

        SizeMode(int searchRadius) {
            this.searchRadius = searchRadius;
        }

        public int getSearchRadius() {
            return searchRadius;
        }

        public Component getName() {
            return Component.translatable("astralsorcery.misc.exchange.size." + this.searchRadius);
        }

        public Component getDisplay() {
            return Component.translatable("astralsorcery.misc.exchange.size", this.getName());
        }

        @Nonnull
        SizeMode next() {
            int next = (this.ordinal() + 1) % values().length;
            return MiscUtils.getEnumEntry(SizeMode.class, next);
        }
    }
}
