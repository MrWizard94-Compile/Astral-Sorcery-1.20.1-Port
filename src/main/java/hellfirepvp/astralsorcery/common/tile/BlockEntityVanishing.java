package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Block entity for the vanishing block placed by the Aevitas constellation perk.
 * Removes itself unless an Aevitas-mantled player is standing on or hovering above it.
 *
 * <p>1.16 → 1.20: AxisAlignedBB → AABB, getEntitiesWithinAABB → getEntitiesOfClass,
 * getPosY() → getY(), isOnGround() → onGround(), isSneaking() → isShiftKeyDown(),
 * removeBlock → removeBlock (unchanged).</p>
 */
public class BlockEntityVanishing extends BlockEntityTick {

    private static final AABB SEARCH_BOX = new AABB(-4, 0, -4, 4, 3, 4);

    public BlockEntityVanishing(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.VANISHING.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        Level level = getLevel();
        if (level == null) return;

        if (!level.isClientSide() && getTicksExisted() % 5 == 0) {
            boolean removeBlock = true;

            List<Player> players = level.getEntitiesOfClass(Player.class,
                    SEARCH_BOX.move(getBlockPos()));
            for (Player player : players) {
                if (ItemMantle.getEffect(player, ConstellationsAS.AEVITAS) != null) {
                    double yDiff = player.getY() - getBlockPos().getY();

                    if (player.onGround() && yDiff >= 0.95 && yDiff <= 1.15) {
                        if (player.isShiftKeyDown()) {
                            break;
                        }
                        removeBlock = false;
                    } else if (player.isShiftKeyDown() && yDiff >= 0.95 && yDiff <= 2.15) {
                        removeBlock = false;
                    }
                }
            }

            if (removeBlock) {
                level.removeBlock(getBlockPos(), false);
            }
        }
    }
}
