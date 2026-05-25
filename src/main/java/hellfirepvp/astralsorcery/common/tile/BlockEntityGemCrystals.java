package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.block.tile.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Ticks gem crystal cluster growth. The cluster grows through 3 stages within its
 * variant type (DAY/NIGHT/SKY) and slowly degrades at the final stage.
 *
 * <p>1.16 → 1.20: 3 stages (0-2) become 9 enum variants grouped by type;
 * growth advances within the variant bucket (type * 3 + level).</p>
 */
public class BlockEntityGemCrystals extends BlockEntityTick {

    public static final int TICK_GROWTH_CHANCE = 10_000;

    public BlockEntityGemCrystals(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.GEM_CRYSTAL_CLUSTER.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (getLevel() == null) return;

        if (!getLevel().isClientSide()) {
            int level = getGrowthLevel();
            if (level < 2 && doesSeeSky()) {
                tryGrowWithChance(TICK_GROWTH_CHANCE);
            } else if (level == 2 && rand.nextInt(4000) == 0) {
                setGrowthLevel(level - 1);
            }
        }
    }

    private void tryGrowWithChance(int growthChance) {
        if (getLevel() == null) return;
        float dayProgress = DayTimeHelper.getDayProgress(getLevel());
        growthChance = (int) (growthChance * (1F - (0.2F * dayProgress)));
        if (rand.nextInt(Math.max(growthChance, 1)) == 0) {
            setGrowthLevel(getGrowthLevel() + 1);
        }
    }

    /** Returns 0, 1, or 2 — the growth level within the variant type. */
    public int getGrowthLevel() {
        if (getLevel() == null) return 0;
        BlockGemCrystalCluster.GrowthStageType stage =
                getLevel().getBlockState(getBlockPos()).getValue(BlockGemCrystalCluster.STAGE);
        return stage.ordinal() % 3;
    }

    /** Keeps the current variant type and sets the growth level within it. */
    public void setGrowthLevel(int level) {
        if (getLevel() == null) return;
        BlockGemCrystalCluster.GrowthStageType current =
                getLevel().getBlockState(getBlockPos()).getValue(BlockGemCrystalCluster.STAGE);
        int typeBase = (current.ordinal() / 3) * 3;
        int clampedLevel = Math.max(0, Math.min(2, level));
        int targetOrdinal = typeBase + clampedLevel;
        BlockGemCrystalCluster.GrowthStageType[] values = BlockGemCrystalCluster.GrowthStageType.values();
        BlockState next = BlocksAS.GEM_CRYSTAL_CLUSTER.get().defaultBlockState()
                .setValue(BlockGemCrystalCluster.STAGE, values[targetOrdinal]);
        getLevel().setBlock(getBlockPos(), next, 3);
        if (clampedLevel == 2 && getLevel() instanceof ServerLevel serverLevel) {
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(PktPlayEffect.EffectType.CRYSTAL_FORM, getBlockPos()),
                    serverLevel, getBlockPos());
        }
    }
}
