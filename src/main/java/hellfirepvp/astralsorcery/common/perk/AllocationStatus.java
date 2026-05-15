/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

/**
 * Result of attempting to allocate or deallocate a perk point.
 */
public enum AllocationStatus {

    /** Successfully allocated the perk. */
    SUCCESS,

    /** The perk is already allocated. */
    ALREADY_ALLOCATED,

    /** The perk is not allocated (for deallocation attempts). */
    NOT_ALLOCATED,

    /** Player does not have enough perk points. */
    INSUFFICIENT_POINTS,

    /** The perk has no allocated neighbor (not reachable from root). */
    NOT_REACHABLE,

    /** Player has not reached the required progression tier. */
    INSUFFICIENT_TIER,

    /** The perk requires a specific constellation attunement. */
    WRONG_CONSTELLATION,

    /** Cannot deallocate this perk because other perks depend on it. */
    WOULD_DISCONNECT,

    /** Generic failure (perk disabled, etc.). */
    FAILED
}
