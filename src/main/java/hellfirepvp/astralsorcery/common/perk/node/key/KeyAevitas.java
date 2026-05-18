/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Key perk for the Aevitas constellation branch.
 * Effect: Randomly grows nearby crops each tick; heals via life-steal attribute.
 * Provides +4 max health and 5% life steal.
 */
public class KeyAevitas extends KeyPerk {

    private static final int RADIUS = 4;
    private static final int TICK_RATE = 3; // attempt growth every 3 ticks

    public KeyAevitas(int x, int y) {
        super(AstralSorcery.key("key_aevitas"), x, y);
        setRequiredConstellation(AstralSorcery.key("aevitas"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH.getKey(),
                ModifierType.ADDITION, 4.0f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL.getKey(),
                ModifierType.ADDITION, 0.05f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player.tickCount % TICK_RATE != 0) return;

        var rand = serverLevel.getRandom();
        int dx = rand.nextIntBetweenInclusive(-RADIUS, RADIUS);
        int dy = rand.nextIntBetweenInclusive(-2, 2);
        int dz = rand.nextIntBetweenInclusive(-RADIUS, RADIUS);
        BlockPos target = new BlockPos(player.getBlockX() + dx, player.getBlockY() + dy, player.getBlockZ() + dz);

        BlockState state = serverLevel.getBlockState(target);
        if (state.getBlock() instanceof BonemealableBlock growable
                && growable.isValidBonemealTarget(serverLevel, target, state, false)) {
            growable.performBonemeal(serverLevel, rand, target, state);
        }
    }
}
