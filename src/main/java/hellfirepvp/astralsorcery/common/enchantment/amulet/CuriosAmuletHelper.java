package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

/**
 * Isolated helper that calls Curios API to locate the player's worn
 * {@link ItemEnchantmentAmulet} across all curio slots (necklace, charm, etc.).
 *
 * <p>This class references Curios types directly and must ONLY be loaded when
 * Curios is present on the classpath. Always invoke through the double-supplier
 * pattern so the JVM delays class loading until runtime:
 * <pre>
 *   Mods.CURIOS.getIfPresent(() -> () -> CuriosAmuletHelper.findAmulet(player))
 * </pre>
 */
final class CuriosAmuletHelper {

    private CuriosAmuletHelper() {}

    static Optional<ItemStack> findAmulet(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(
                        stack -> stack.getItem() instanceof ItemEnchantmentAmulet))
                .orElse(Optional.empty())
                .map(SlotResult::stack);
    }
}
