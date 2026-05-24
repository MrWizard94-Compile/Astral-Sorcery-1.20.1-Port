package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.util.item.ItemComparator;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class AmuletEnchantmentHelper {

    public static final String KEY_AS_OWNER = "AS_Amulet_Holder";

    public static void removeAmuletTagsAndCleanup(Player player, boolean keepEquipped) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) {
            if (i == inv.selected && keepEquipped) {
                continue;
            }
            removeAmuletOwner(inv.items.get(i));
        }
        // Cursor slot (item held in open container UI)
        removeAmuletOwner(player.containerMenu.getCarried());
        if (!keepEquipped) {
            for (ItemStack armor : inv.armor) {
                removeAmuletOwner(armor);
            }
            for (ItemStack offhand : inv.offhand) {
                removeAmuletOwner(offhand);
            }
        }
    }

    @Nonnull
    private static final UUID NIL_UUID = new UUID(0L, 0L);

    @Nonnull
    private static UUID getWornPlayerUUID(ItemStack anyTool) {
        if (DynamicEnchantmentHelper.canHaveDynamicEnchantment(anyTool) && anyTool.hasTag()) {
            net.minecraft.nbt.CompoundTag tag = anyTool.getTag();
            if (tag != null) {
                return NBTHelper.getUUID(tag, KEY_AS_OWNER, NIL_UUID);
            }
        }
        return NIL_UUID;
    }

    public static void applyAmuletOwner(ItemStack tool, Player wearer) {
        if (DynamicEnchantmentHelper.canHaveDynamicEnchantment(tool)) {
            tool.getOrCreateTag().putUUID(KEY_AS_OWNER, wearer.getUUID());
        }
    }

    private static void removeAmuletOwner(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        NBTHelper.removeUUID(stack.getTag(), KEY_AS_OWNER);
        if (stack.getTag().isEmpty()) {
            stack.setTag(null);
        }
    }

    @Nullable
    public static Player getPlayerHavingTool(ItemStack anyTool) {
        UUID plUUID = getWornPlayerUUID(anyTool);
        if (plUUID.getLeastSignificantBits() == 0 && plUUID.getMostSignificantBits() == 0) {
            return null;
        }

        Player player;
        if (anyTool.isEmpty()) return null;

        // Try server first
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            player = server.getPlayerList().getPlayer(plUUID);
        } else {
            player = resolvePlayerClient(plUUID);
        }
        if (player == null) {
            return null;
        }

        int originalDamage = anyTool.getDamageValue();

        boolean foundTool = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            anyTool.setDamageValue(stack.getDamageValue());
            if (ItemComparator.compare(stack, anyTool, ItemComparator.Clause.Sets.ITEMSTACK_STRICT)) {
                foundTool = true;
                break;
            }
        }
        anyTool.setDamageValue(originalDamage);
        if (!foundTool) return null;

        return player;
    }

    @Nullable
    static Optional<ItemStack> getWornAmulet(ItemStack anyTool) {
        Player player = getPlayerHavingTool(anyTool);
        if (player == null) return Optional.empty();

        // Curios integration stub — scan curio slots when Curios is present.
        // For now, fall back to checking offhand.
        Inventory inv = player.getInventory();
        for (ItemStack offhand : inv.offhand) {
            if (!offhand.isEmpty() && offhand.getItem() instanceof ItemEnchantmentAmulet) {
                return Optional.of(offhand);
            }
        }
        return Optional.empty();
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    private static Player resolvePlayerClient(UUID plUUID) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null) {
            return mc.level.getPlayerByUUID(plUUID);
        }
        return null;
    }
}
