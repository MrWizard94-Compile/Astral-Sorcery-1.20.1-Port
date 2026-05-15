package hellfirepvp.astralsorcery.common.auxiliary.link;

import hellfirepvp.astralsorcery.common.item.base.OverrideInteractItem;

/**
 * Marker interface for items that can create links between block entities.
 * Items implementing this participate in the linking system — right-clicking
 * a {@link LinkableTileEntity} selects it, right-clicking a second one
 * attempts to create a link between them.
 *
 * <p>No 1.16 -> 1.20 changes beyond parent interface.</p>
 */
public interface IItemLinkingTool extends OverrideInteractItem {
}
