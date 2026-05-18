/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Key perk that places temporary light sources in dark areas near the player.
 * Every 15 ticks, attempts to place a light-level-15 light block in a dark
 * air block within a 5-block radius. The block is replaced if non-air is
 * already there, keeping the world clean.
 */
public class KeySpawnLights extends KeyPerk {

    private static final int TICK_RATE = 15;
    private static final int RADIUS = 5;

    public KeySpawnLights(int x, int y) {
        super(AstralSorcery.key("key_spawn_lights"), x, y);
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.tickCount % TICK_RATE != 0) return;

        var rand = level.getRandom();
        int attempts = 4;
        while (attempts-- > 0) {
            int dx = rand.nextIntBetweenInclusive(-RADIUS, RADIUS);
            int dy = rand.nextIntBetweenInclusive(-2, 2);
            int dz = rand.nextIntBetweenInclusive(-RADIUS, RADIUS);
            BlockPos pos = new BlockPos(player.getBlockX() + dx, player.getBlockY() + dy, player.getBlockZ() + dz);
            BlockState current = level.getBlockState(pos);
            if (current.isAir() && level.getRawBrightness(pos, 0) < 8) {
                BlockState light = Blocks.LIGHT.defaultBlockState()
                        .setValue(LightBlock.LEVEL, 15);
                level.setBlockAndUpdate(pos, light);
                break;
            }
        }
    }
}
