/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Key perk that accelerates crop growth in a 3-block radius around the player.
 * Every 10 ticks, attempts to bonemeal one random growable block.
 */
public class KeyGrowables extends KeyPerk {

    private static final int TICK_RATE = 10;
    private static final int RADIUS    = 3;

    public KeyGrowables(int x, int y) {
        super(AstralSorcery.key("key_growables"), x, y);
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
        if (rand.nextFloat() >= 0.30f) return;

        int dx = rand.nextIntBetweenInclusive(-RADIUS, RADIUS);
        int dy = rand.nextIntBetweenInclusive(-1, 1);
        int dz = rand.nextIntBetweenInclusive(-RADIUS, RADIUS);
        BlockPos pos = new BlockPos(player.getBlockX() + dx, player.getBlockY() + dy, player.getBlockZ() + dz);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BonemealableBlock growable
                && growable.isValidBonemealTarget(level, pos, state, false)) {
            growable.performBonemeal(level, rand, pos, state);
        }
    }
}
