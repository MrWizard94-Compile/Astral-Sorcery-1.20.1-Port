package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.block.ore.BlockStarmetalOre;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Ticks celestial crystal cluster growth. Grows one stage per chance roll while
 * the cluster can see sky; starmetal ore below accelerates growth but is slowly consumed.
 *
 * <p>Also stores {@link CrystalAttributes} set by the formation recipe, which are
 * transferred to a dropped item when the cluster is harvested.</p>
 *
 * <p>1.16 → 1.20: TileEntityTick → BlockEntityTick, CompoundNBT → CompoundTag,
 * getWorld() → getLevel(), getPos() → getBlockPos(), getDefaultState() → defaultBlockState(),
 * CraftingConfig.getStarmetalRevertBlockState() → Blocks.IRON_ORE (hardcoded default).</p>
 */
public class BlockEntityCelestialCrystals extends BlockEntityTick implements CrystalAttributeTile {

    public static final int TICK_GROWTH_CHANCE = 18_000;

    private CrystalAttributes attributes = null;

    public BlockEntityCelestialCrystals(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.CELESTIAL_CRYSTAL_CLUSTER.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        net.minecraft.world.level.Level level = getLevel();
        if (level == null) return;

        if (!level.isClientSide()) {
            if (getGrowth() < 4 && doesSeeSky()) {
                tryGrowWithChance(TICK_GROWTH_CHANCE);
            }
        }
    }

    private void tryGrowWithChance(int growthChance) {
        net.minecraft.world.level.Level level = getLevel();
        if (level == null) return;

        BlockState downState = level.getBlockState(getBlockPos().below());
        if (downState.getBlock() instanceof BlockStarmetalOre) {
            growthChance = (int) (growthChance * 0.6);
            if (rand.nextInt(400) == 0) {
                level.setBlock(getBlockPos().below(), Blocks.IRON_ORE.defaultBlockState(), 3);
            }
        }

        float dayProgress = DayTimeHelper.getDayProgress(level);
        growthChance = (int) (growthChance * (1F - (0.5F * dayProgress)));
        grow(growthChance);
    }

    private void grow(int chance) {
        int stage = getGrowth();
        if (stage < 4 && rand.nextInt(Math.max(chance, 1)) == 0) {
            setGrowth(stage + 1);
        }
    }

    public int getGrowth() {
        net.minecraft.world.level.Level level = getLevel();
        if (level == null) return 0;
        return level.getBlockState(getBlockPos()).getValue(BlockCelestialCrystalCluster.STAGE);
    }

    public void setGrowth(int stage) {
        net.minecraft.world.level.Level level = getLevel();
        if (level == null) return;
        BlockState next = BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get().defaultBlockState()
                .setValue(BlockCelestialCrystalCluster.STAGE, stage);
        level.setBlock(getBlockPos(), next, 3);
        if (stage == 4 && level instanceof ServerLevel serverLevel) {
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(PktPlayEffect.EffectType.CRYSTAL_FORM, getBlockPos()),
                    serverLevel, getBlockPos());
        }
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.attributes = CrystalAttributes.getCrystalAttributes(compound);
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (this.attributes != null) {
            this.attributes.store(compound);
        } else {
            CrystalAttributes.storeNull(compound);
        }
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        this.attributes = attributes;
        markForUpdate();
    }
}
