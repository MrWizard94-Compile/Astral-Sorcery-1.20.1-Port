/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.Set;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;

/**
 * Mantle effect for Lucerna (Weak — Light).
 *
 * <p>Client tick highlights nearby entities with Lucerna yellow sparkles,
 * and periodically highlights spawners (dark red) and inventory containers (green)
 * within the configured range.</p>
 */
public class MantleEffectLucerna extends MantleEffect {

    public static final LucernaConfig CONFIG = new LucernaConfig();

    public MantleEffectLucerna() {
        super(ConstellationsAS.LUCERNA);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {}

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected boolean usesClientTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(@Nonnull Player player) {
        if (rand.nextBoolean()) {
            playEntityHighlight(player);
        }
        if (CONFIG.findSpawners.get() && rand.nextInt(10) == 0) {
            playBlockHighlight(player, ColorsAS.MANTLE_LUCERNA_SPAWNER,
                    be -> be instanceof SpawnerBlockEntity);
        }
        if (CONFIG.findChests.get() && rand.nextInt(10) == 0) {
            playBlockHighlight(player, ColorsAS.MANTLE_LUCERNA_INVENTORY,
                    be -> be.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEntityHighlight(@Nonnull Player player) {
        int range = CONFIG.range.get();
        AABB box = player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, box);
        Color entityColor = getAssociatedConstellation().getConstellationColor();
        for (LivingEntity entity : entities) {
            if (!entity.isAlive() || entity.equals(player) || rand.nextInt(8) != 0) continue;

            Vector3 atEntity = Vector3.atEntityCorner(entity);
            if (atEntity.distance(player) < 2) continue;
            atEntity.add(
                    rand.nextFloat() * entity.getBbWidth(),
                    rand.nextFloat() * entity.getBbHeight(),
                    rand.nextFloat() * entity.getBbWidth());

            Vec3 pos = new Vec3(atEntity.getX(), atEntity.getY(), atEntity.getZ());
            EffectHelper.sparkleFloating(pos, entityColor, 0.4F + rand.nextFloat() * 0.4F, 30 + rand.nextInt(15));
            if (rand.nextFloat() > 0.35F) {
                EffectHelper.sparkleFloating(pos, Color.WHITE, 0.2F + rand.nextFloat() * 0.2F, 20 + rand.nextInt(10));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playBlockHighlight(@Nonnull Player player, @Nonnull Color color,
                                    @Nonnull java.util.function.Predicate<net.minecraft.world.level.block.entity.BlockEntity> test) {
        float chance = 0.9F;
        Set<BlockPos> positions = BlockDiscoverer.searchForTileEntitiesAround(
                player.level(), player.blockPosition(), CONFIG.range.get(), test);
        for (BlockPos pos : positions) {
            if (rand.nextFloat() > chance) continue;

            Vector3 at = new Vector3(pos).add(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            if (at.distance(player) < 4) continue;

            Vec3 vec = new Vec3(at.getX(), at.getY(), at.getZ());
            EffectHelper.sparkleFloating(vec, color, 0.4F + rand.nextFloat() * 0.4F, 30 + rand.nextInt(15));
            if (rand.nextFloat() > 0.35F) {
                EffectHelper.sparkleFloating(vec, Color.WHITE, 0.2F + rand.nextFloat() * 0.2F, 20 + rand.nextInt(10));
            }
            chance *= 0.9F;
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class LucernaConfig extends Config {

        public ForgeConfigSpec.IntValue range;
        public ForgeConfigSpec.BooleanValue findSpawners;
        public ForgeConfigSpec.BooleanValue findChests;

        public LucernaConfig() {
            super("lucerna");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.lucerna");
            range = b
                    .comment("Maximum range in blocks for entity/block highlighting.")
                    .defineInRange("range", 48, 0, 512);
            findSpawners = b
                    .comment("Whether spawners in range should be highlighted.")
                    .define("findSpawners", true);
            findChests = b
                    .comment("Whether containers in range should be highlighted.")
                    .define("findChests", true);
            b.pop();
            b.build();
        }
    }
}
