package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.block.tile.BlockFlareLight;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Tile entity for the player-placed illuminator.
 * When activated by a player, it scans the cave layers beneath it and
 * periodically places flare-light blocks in unlit air pockets.
 *
 * <p>1.16 → 1.20: World/BlockPos → Level/BlockPos, isAirBlock → isEmptyBlock,
 * getLightFor(LightType.SKY) → getBrightness(LightLayer.SKY),
 * getLight → getRawBrightness(pos, 0), pos.offset(dir, n) → pos.relative(dir, n),
 * dir.rotateY() → dir.getClockWise().</p>
 */
public class BlockEntityIlluminator extends BlockEntityTick {

    public static final LightCheck ILLUMINATOR_CHECK = new LightCheck();

    public static final int SEARCH_RADIUS = 64;
    public static final int STEP_WIDTH = 2;

    private List<List<BlockPos>> layerPositions = null;
    private boolean doRecalculation = false;
    private int ticksUntilNextPlacement = 180;
    private boolean playerPlaced = false;
    private int boostedTicks = 0;

    private DyeColor color = DyeColor.YELLOW;

    public BlockEntityIlluminator(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.ILLUMINATOR.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isPlayerPlaced()) {
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        if (!level.isClientSide()) {
            if (layerPositions == null) {
                recalculate();
            }
            placeFlare();
            placeFlare();
            placeFlare();
            if (level.getRandom().nextInt(3) == 0 && placeFlare()) {
                doRecalculation = true;
            }
            if (boostedTicks > 0) {
                boostedTicks--;
            }
            ticksUntilNextPlacement--;
            if (ticksUntilNextPlacement <= 0) {
                ticksUntilNextPlacement = boostedTicks > 0 ? 30 : 180;
                if (doRecalculation) {
                    doRecalculation = false;
                    recalculate();
                }
            }
        }
    }

    private void recalculate() {
        int height = Math.max(0, getBlockPos().getY() - 7);
        int parts = height / 7;
        layerPositions = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            int yPart = 3 + i * 7;
            // LinkedHashSet gives O(1) contains() while preserving insertion order.
            LinkedHashSet<BlockPos> posSet = new LinkedHashSet<>();
            generatePositions(posSet, new BlockPos(getBlockPos().getX(), yPart, getBlockPos().getZ()));
            layerPositions.add(new ArrayList<>(posSet));
        }
    }

    private void generatePositions(LinkedHashSet<BlockPos> positions, BlockPos center) {
        int xPos = center.getX();
        int yPos = center.getY();
        int zPos = center.getZ();
        BlockPos current = center;
        positions.add(current);

        Direction dir = Direction.NORTH;
        while (Math.abs(current.getX() - xPos) <= SEARCH_RADIUS &&
                Math.abs(current.getY() - yPos) <= SEARCH_RADIUS &&
                Math.abs(current.getZ() - zPos) <= SEARCH_RADIUS) {
            current = current.relative(dir, STEP_WIDTH);
            positions.add(current);
            Direction tryDirNext = dir.getClockWise();
            if (!positions.contains(current.relative(tryDirNext, STEP_WIDTH))) {
                dir = tryDirNext;
            }
        }
    }

    private boolean placeFlare() {
        if (layerPositions == null) return false;
        Level level = getLevel();
        if (level == null) return false;

        boolean recalc = false;
        for (List<BlockPos> list : layerPositions) {
            if (list.isEmpty()) {
                recalc = true;
                continue;
            }

            int index = level.getRandom().nextInt(list.size());
            BlockPos at = list.remove(index);
            if (!recalc && list.isEmpty()) {
                recalc = true;
            }
            final BlockPos candidate = at.offset(
                    level.getRandom().nextInt(5) - 2,
                    level.getRandom().nextInt(13) - 6,
                    level.getRandom().nextInt(5) - 2);

            MiscUtils.executeWithChunk(level, candidate, () -> {
                if (doesSeeSky() && ILLUMINATOR_CHECK.test(level, candidate, level.getBlockState(candidate))) {
                    BlockState toPlace = BlocksAS.FLARE_LIGHT.get().defaultBlockState()
                            .setValue(BlockFlareLight.COLOR, color);
                    level.setBlockAndUpdate(candidate, toPlace);
                }
            });
        }
        return recalc;
    }

    public void setColor(DyeColor color) {
        this.color = color;
        markForUpdate();
    }

    public DyeColor getColor() {
        return color;
    }

    public void setPlayerPlaced(boolean playerPlaced) {
        this.playerPlaced = playerPlaced;
        markForUpdate();
    }

    public boolean isPlayerPlaced() {
        return playerPlaced;
    }

    public void onWandUsed(DyeColor color) {
        this.boostedTicks = 10 * 60 * 20;
        setColor(color);
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.playerPlaced = compound.getBoolean("playerPlaced");
        this.color = DyeColor.byId(compound.getInt("color"));
        this.boostedTicks = compound.getInt("boostedTicks");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.putBoolean("playerPlaced", this.playerPlaced);
        compound.putInt("color", this.color.getId());
        compound.putInt("boostedTicks", this.boostedTicks);
    }

    public static class LightCheck implements BlockPredicate {

        @Override
        public boolean test(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
            return state.isAir() &&
                    !level.canSeeSky(pos) &&
                    level.getRawBrightness(pos, 0) < 8 &&
                    level.getBrightness(LightLayer.SKY, pos) < 4;
        }
    }
}
