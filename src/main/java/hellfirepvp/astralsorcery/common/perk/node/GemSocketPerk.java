/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A gem socket perk that accepts a perk gem item. When a gem is socketed,
 * its modifiers are combined with the perk's base modifiers to determine
 * the effective bonuses.
 *
 * <p>Gem state is persisted via NBT alongside the perk's normal data.
 * The gem can be replaced or removed by the player at a perk-reset cost.</p>
 */
public class GemSocketPerk extends AbstractPerk {

    private static final String TAG_SOCKETED_GEM = "socketedGem";

    @Nullable
    private ItemStack socketedGem = null;

    /**
     * @param key unique perk key
     * @param x   horizontal position in the tree
     * @param y   vertical position in the tree
     */
    public GemSocketPerk(@Nonnull ResourceLocation key, int x, int y) {
        super(key, x, y, AbstractPerk.PerkCategory.GEM_SOCKET);
    }

    /**
     * Gets the currently socketed gem, or null if empty.
     */
    @Nullable
    public ItemStack getSocketedGem() {
        return socketedGem;
    }

    /**
     * Sets the socketed gem item. Pass null to clear the socket.
     *
     * @param gem the gem item to socket, or null to remove
     */
    public void setSocketedGem(@Nullable ItemStack gem) {
        this.socketedGem = gem;
    }

    /**
     * Whether this socket currently holds a gem.
     */
    public boolean hasGem() {
        return socketedGem != null && !socketedGem.isEmpty();
    }

    @Override
    @Nonnull
    public List<PerkAttributeModifier> getEffectiveModifiers(@Nonnull Player player) {
        List<PerkAttributeModifier> effective = new ArrayList<>(getModifiers());
        if (hasGem()) {
            // TODO: Extract modifiers from the socketed gem item and merge them
            //  with the base modifiers. This requires the gem item type to expose
            //  its PerkAttributeModifier list (e.g., via an IPerkGem interface).
        }
        return effective;
    }

    @Override
    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = super.writeToNBT();
        if (hasGem()) {
            tag.put(TAG_SOCKETED_GEM, socketedGem.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains(TAG_SOCKETED_GEM)) {
            this.socketedGem = ItemStack.of(tag.getCompound(TAG_SOCKETED_GEM));
        } else {
            this.socketedGem = null;
        }
    }
}
