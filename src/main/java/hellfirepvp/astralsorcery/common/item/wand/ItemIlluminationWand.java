package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.block.tile.BlockFlareLight;
import hellfirepvp.astralsorcery.common.block.tile.BlockTranslucentBlock;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityIlluminator;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTranslucentBlock;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Illumination Wand — places invisible flare-light sources or converts solid blocks
 * to translucent glowing versions. Consumes alignment charge per use.
 *
 * <p>1.16 → 1.20: ItemUseContext → UseOnContext, ActionResultType → InteractionResult,
 * Constants.BlockFlags.DEFAULT_AND_RERENDER → Block.UPDATE_ALL,
 * state.hasTileEntity() → state.hasBlockEntity(),
 * player.canPlayerEdit() → player.mayUseItemAt(),
 * isValidPosition → canSurvive, SoundCategory → SoundSource.</p>
 */
public class ItemIlluminationWand extends ItemAS implements AlignmentChargeConsumer {

    private static final float COST_PER_ILLUMINATION = 650F;
    private static final float COST_PER_FLARE = 300F;

    public ItemIlluminationWand() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        return player.isCrouching() ? COST_PER_ILLUMINATION : COST_PER_FLARE;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Level level = context.getLevel();
        Direction dir = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide() || player == null || stack.isEmpty()) {
            return InteractionResult.SUCCESS;
        }

        BlockState state = level.getBlockState(pos);

        if (player.isCrouching()) {
            if (state.getBlock() instanceof BlockTranslucentBlock) {
                BlockEntityTranslucentBlock tb = MiscUtils.getTileAt(level, pos, BlockEntityTranslucentBlock.class, true);
                if (tb != null && (tb.getPlayerUUID() == null || tb.getPlayerUUID().equals(player.getUUID()))) {
                    if (tb.revert()) {
                        SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_UNHIGHLIGHT.get(),
                                SoundSource.BLOCKS, level, pos, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                    }
                }
            } else {
                if (level.getBlockEntity(pos) == null &&
                        !state.hasBlockEntity() &&
                        player.mayUseItemAt(pos, dir, stack) &&
                        state.getShape(level, pos).equals(net.minecraft.world.phys.shapes.Shapes.block())) {
                    if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_ILLUMINATION, false)) {
                        if (level.setBlock(pos, BlocksAS.TRANSLUCENT_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL)) {
                            SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_HIGHLIGHT.get(),
                                    SoundSource.BLOCKS, level, pos, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                            BlockEntityTranslucentBlock tb = MiscUtils.getTileAt(level, pos, BlockEntityTranslucentBlock.class, true);
                            if (tb != null) {
                                tb.setFakedState(state);
                                tb.setOverlayColor(ColorUtils.flareColorFromDye(getConfiguredColor(stack)));
                                tb.setPlayerUUID(player.getUUID());
                            } else {
                                level.setBlock(pos, state, Block.UPDATE_ALL);
                            }
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        BlockEntityIlluminator illum = MiscUtils.getTileAt(level, pos, BlockEntityIlluminator.class, true);
        if (illum != null) {
            illum.onWandUsed();
            SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT.get(),
                    SoundSource.BLOCKS, level, pos, 0.6F, 1F);
            return InteractionResult.SUCCESS;
        }

        BlockPos placePos = BlockUtils.isReplaceable(level, pos) ? pos : pos.relative(dir);
        if (!BlockUtils.isReplaceable(level, placePos)) {
            return InteractionResult.SUCCESS;
        }

        BlockState placeState = getPlacingState(stack);
        if (player.mayUseItemAt(placePos, dir, stack)) {
            if (level.getBlockState(placePos).equals(placeState)) {
                if (level.setBlock(placePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) {
                    SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT.get(),
                            SoundSource.BLOCKS, level, pos, 0.6F, 1F);
                }
            } else if (placeState.canSurvive(level, placePos)) {
                if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_FLARE, false)) {
                    if (level.setBlock(placePos, placeState, Block.UPDATE_ALL)) {
                        SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT.get(),
                                SoundSource.BLOCKS, level, pos, 0.6F, 1F);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static void setConfiguredColor(ItemStack stack, DyeColor color) {
        NBTHelper.getPersistentData(stack).putInt("color",
                color != null ? color.getId() : DyeColor.YELLOW.getId());
    }

    @Nonnull
    public static DyeColor getConfiguredColor(ItemStack stack) {
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (tag.contains("color")) {
            return DyeColor.byId(tag.getInt("color"));
        }
        return DyeColor.YELLOW;
    }

    @Nonnull
    public static BlockState getPlacingState(ItemStack wand) {
        return BlocksAS.FLARE_LIGHT.get().defaultBlockState()
                .setValue(BlockFlareLight.COLOR, getConfiguredColor(wand));
    }
}
