/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.compat.curios;

import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Isolated Curios API integration. This class references Curios types directly and must ONLY be
 * loaded when Curios is present on the classpath. Always invoke via the double-supplier pattern:
 * <pre>
 *   Mods.CURIOS.executeIfPresent(() -> CuriosIntegration::register);
 * </pre>
 *
 * <p>Registers {@link hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet} as a curio
 * item so it can be placed in the "amulet" curio slot (defined in
 * {@code data/astralsorcery/curios/slots/amulet.json}). The amulet's tick/equip logic is driven
 * by {@link hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler} on the Forge
 * event bus; no curio tick override is needed here.</p>
 */
public final class CuriosIntegration {

    private CuriosIntegration() {}

    /**
     * Registers the enchantment amulet item with Curios. Curios will automatically attach an
     * {@link top.theillusivec4.curios.api.type.capability.ICurio} capability to every
     * {@link hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet} ItemStack, delegating
     * all curio callbacks to the default {@link ICurioItem} implementations. The item is usable
     * in the {@code amulet} slot type defined by the mod's data pack.
     */
    public static void register() {
        CuriosApi.registerCurio(ItemsAS.ENCHANTMENT_AMULET.get(), new ICurioItem() {
            // All methods delegate to ICurioItem defaults.
            // Tick/equip behavior is handled by PlayerAmuletHandler on the Forge event bus.
        });
    }
}
