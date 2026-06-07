/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Key perk that places FlareLight blocks in dark areas near the player.
 * Every 15 ticks, places one FlareLight in a dark air block within 5 blocks.
 * Tracks placed positions per player; removes the oldest when the cap is reached,
 * and removes all on perk deallocation — prevents permanent world pollution.
 */
public class KeySpawnLights extends KeyPerk {

    private static final int TICK_RATE  = 15;
    private static final int RADIUS     = 5;
    private static final int MAX_LIGHTS = 32;

    // Server-side only: tracks recently placed light positions per player.
    private static final Map<UUID, Deque<BlockPos>> placedLights = new ConcurrentHashMap<>();

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
            BlockPos pos = player.blockPosition().offset(dx, dy, dz);
            BlockState current = level.getBlockState(pos);
            if (current.isAir() && level.getRawBrightness(pos, 0) < 8) {
                level.setBlockAndUpdate(pos, BlocksAS.FLARE_LIGHT.get().defaultBlockState());

                Deque<BlockPos> queue = placedLights.computeIfAbsent(
                        player.getUUID(), id -> new ArrayDeque<>());

                // Evict oldest if cap reached
                if (queue.size() >= MAX_LIGHTS) {
                    BlockPos oldest = queue.pollFirst();
                    if (oldest != null && level.getBlockState(oldest).getBlock()
                            == BlocksAS.FLARE_LIGHT.get()) {
                        level.setBlock(oldest, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                queue.addLast(pos.immutable());
                break;
            }
        }
    }

    @Override
    public void onDeallocate(@Nonnull Player player) {
        super.onDeallocate(player);
        if (!(player.level() instanceof ServerLevel level)) return;
        Deque<BlockPos> queue = placedLights.remove(player.getUUID());
        if (queue == null) return;
        for (BlockPos pos : queue) {
            if (level.getBlockState(pos).getBlock() == BlocksAS.FLARE_LIGHT.get()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
