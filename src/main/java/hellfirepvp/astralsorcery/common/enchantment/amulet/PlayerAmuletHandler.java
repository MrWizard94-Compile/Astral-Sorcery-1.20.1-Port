package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

/**
 * Drives the amulet enchantment system per player tick.
 * Replaces the 1.16 ITickHandler (observerlib). Registered on the Forge event bus in CommonProxy.
 */
public class PlayerAmuletHandler {

    public static final PlayerAmuletHandler INSTANCE = new PlayerAmuletHandler();

    private PlayerAmuletHandler() {}

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        applyAmuletTags(player);
        clearAmuletTags(player);
    }

    public static void onEnchantmentAdd(DynamicEnchantmentEvent.Add event) {
        if (!DynamicEnchantmentHelper.canHaveDynamicEnchantment(event.getEnchantedItemStack())) {
            return;
        }
        Optional<ItemStack> amulet = AmuletEnchantmentHelper.getWornAmulet(event.getEnchantedItemStack());
        amulet.ifPresent(stack ->
                event.getEnchantmentsToApply().addAll(ItemEnchantmentAmulet.getAmuletEnchantments(stack)));
    }

    private void applyAmuletTags(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            AmuletEnchantmentHelper.applyAmuletOwner(player.getItemBySlot(slot), player);
        }
    }

    private void clearAmuletTags(Player player) {
        AmuletEnchantmentHelper.removeAmuletTagsAndCleanup(player, true);
    }
}
