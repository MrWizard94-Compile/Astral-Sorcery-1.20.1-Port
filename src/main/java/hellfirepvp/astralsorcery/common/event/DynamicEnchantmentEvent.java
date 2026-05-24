package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class DynamicEnchantmentEvent {

    /** Fired to collect new dynamic enchantments to apply to an item. */
    @Cancelable
    public static class Add extends Event {

        private final List<DynamicEnchantment> enchantmentsToApply = new LinkedList<>();
        private final ItemStack itemStack;
        private final Player resolvedPlayer;

        public Add(ItemStack itemStack, @Nonnull Player player) {
            this.itemStack = itemStack;
            this.resolvedPlayer = player;
        }

        public ItemStack getEnchantedItemStack() {
            return itemStack;
        }

        @Nonnull
        public Player getResolvedPlayer() {
            return resolvedPlayer;
        }

        public List<DynamicEnchantment> getEnchantmentsToApply() {
            return enchantmentsToApply;
        }
    }

    /** Fired to modify or react to the enchantments collected by the Add event. */
    @Cancelable
    public static class Modify extends Event {

        private final List<DynamicEnchantment> enchantmentsToApply;
        private final ItemStack itemStack;
        private final Player resolvedPlayer;

        public Modify(ItemStack itemStack, List<DynamicEnchantment> enchantmentsToApply, @Nonnull Player resolvedPlayer) {
            this.itemStack = itemStack;
            this.enchantmentsToApply = enchantmentsToApply;
            this.resolvedPlayer = resolvedPlayer;
        }

        @Nonnull
        public Player getResolvedPlayer() {
            return resolvedPlayer;
        }

        public ItemStack getEnchantedItemStack() {
            return itemStack;
        }

        public List<DynamicEnchantment> getEnchantmentsToApply() {
            return enchantmentsToApply;
        }
    }
}
