package hellfirepvp.astralsorcery.common.perk.reader;

import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Reader for the break-speed perk attribute.
 * The default value is derived from the player's current mining speed
 * against cobblestone (a representative default).
 *
 * <p>1.16 → 1.20: getDigSpeed → getDestroySpeed; Blocks.COBBLESTONE API unchanged.</p>
 */
public class ReaderBreakSpeed extends ReaderFlatAttribute {

    public ReaderBreakSpeed(@Nonnull PerkAttributeType type) {
        super(type, 1.0);
    }

    @Override
    public double getDefaultValue(@Nonnull Player player, @Nonnull LogicalSide side) {
        return player.getDestroySpeed(Blocks.COBBLESTONE.defaultBlockState());
    }
}
