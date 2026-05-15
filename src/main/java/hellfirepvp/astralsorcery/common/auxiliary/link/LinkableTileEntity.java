package hellfirepvp.astralsorcery.common.auxiliary.link;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface for block entities that can participate in the linking system.
 * A linkable tile entity can be connected to other positions or entities
 * via the linking tool (e.g., linking wand).
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity interface -> BlockEntity interface,
 * World -> Level, PlayerEntity -> Player,
 * ITextComponent -> Component,
 * TranslationTextComponent -> Component.translatable()</p>
 */
public interface LinkableTileEntity {

    /**
     * @return the level this linkable tile exists in
     */
    @Nullable
    Level getLinkWorld();

    /**
     * @return the block position of this linkable tile
     */
    @Nonnull
    BlockPos getLinkPos();

    /**
     * @return an unlocalized display name for link feedback messages
     */
    @Nonnull
    Component getUnLocalizedDisplayName();

    /**
     * Whether this tile accepts incoming link connections.
     *
     * @return true if links can target this tile
     */
    boolean doesAcceptLinks();

    /**
     * Called when a block-to-block link is successfully established.
     *
     * @param player the player who created the link
     * @param other  the position of the other tile in the link
     */
    void onBlockLinkCreate(@Nonnull Player player, @Nonnull BlockPos other);

    /**
     * Called when a block-to-entity link is successfully established.
     *
     * @param player the player who created the link
     * @param entity the entity that was linked
     */
    void onEntityLinkCreate(@Nonnull Player player, @Nonnull Entity entity);

    /**
     * Called when this tile is selected as the first endpoint in a link operation.
     *
     * @param player the player who selected this tile
     * @return true if selection was successful
     */
    boolean onSelect(@Nonnull Player player);

    /**
     * Attempt to create a link from this tile to another block position.
     *
     * @param player the player attempting the link
     * @param other  the target block position
     * @return true if the link was created successfully
     */
    boolean tryLinkBlock(@Nonnull Player player, @Nonnull BlockPos other);

    /**
     * Attempt to create a link from this tile to an entity.
     *
     * @param player the player attempting the link
     * @param entity the target entity
     * @return true if the link was created successfully
     */
    boolean tryLinkEntity(@Nonnull Player player, @Nonnull Entity entity);

    /**
     * Attempt to remove an existing link.
     *
     * @param player the player attempting to unlink
     * @param other  the position to unlink from
     * @return true if the link was removed
     */
    boolean tryUnlink(@Nonnull Player player, @Nonnull BlockPos other);

    /**
     * Get all positions this tile is currently linked to.
     *
     * @return an unmodifiable list of linked block positions
     */
    @Nonnull
    List<BlockPos> getLinkedPositions();
}
