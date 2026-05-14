package hellfirepvp.astralsorcery.common.structure.observer;

/**
 * Replacement for ObserverLib's BlockStructureObserver.
 * Marker interface for blocks that participate in multiblock structure matching.
 * When a block implementing this interface is placed or removed, the observer
 * system may re-evaluate nearby structures.
 *
 * <p>Implemented by: BlockCollectorCrystal, BlockRitualPedestal, BlockCelestialGateway, etc.</p>
 */
public interface BlockStructureObserver {
    // Marker interface — no methods required.
    // Presence signals that block changes should trigger structure re-evaluation.
}
