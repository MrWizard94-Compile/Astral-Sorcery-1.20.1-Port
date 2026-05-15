package hellfirepvp.astralsorcery.common.block.base;

/**
 * Marker interface for blocks that contain or process liquid starlight.
 * Used by the starlight collection system to identify blocks that
 * participate in starlight network interactions as passive collectors.
 *
 * <p>Implementing blocks: Lightwell, Chalice, Infuser, Starlight-containing blocks.</p>
 */
public interface LiquidStarlightOwned {

    /**
     * The maximum amount of liquid starlight (in mB) this block can hold.
     *
     * @return max fluid capacity
     */
    int getMaxLiquidStarlight();
}
