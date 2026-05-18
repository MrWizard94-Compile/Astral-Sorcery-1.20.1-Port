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
 * Key perk for the Bootes constellation branch.
 * Effect: Crops grow faster in a wide radius; XP bonus via attribute.
 * Grows 2 random plants per tick in a 5-block radius.
 */
public class KeyBootes extends KeyPerk {

    private static final int RADIUS = 5;
    private static final int TICK_RATE = 2;
    private static final int ATTEMPTS = 2;

    public KeyBootes(int x, int y) {
        super(AstralSorcery.key("key_bootes"), x, y);
        setRequiredConstellation(AstralSorcery.key("bootes"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
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
        for (int i = 0; i < ATTEMPTS; i++) {
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
}
