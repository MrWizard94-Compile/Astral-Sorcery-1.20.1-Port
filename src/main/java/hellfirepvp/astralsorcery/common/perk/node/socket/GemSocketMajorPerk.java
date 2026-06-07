/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.socket;

import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A major-category perk that also has a gem socket.
 * Extends {@link MajorPerk} directly and re-implements the gem socket
 * behaviour from {@link hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk},
 * since Java's single-inheritance prevents extending both classes simultaneously.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag; PlayerEntity → Player;
 * split from ForgeRegistryEntry usage; GemSocketPerk converted from interface
 * to class in this port.</p>
 */
public class GemSocketMajorPerk extends MajorPerk {

    private static final String TAG_GEM = "socketedGem";

    @Nullable
    private ItemStack socketedGem = null;

    public GemSocketMajorPerk(@Nonnull ResourceLocation key, int x, int y) {
        super(key, x, y);
    }

    // =========================================================================
    // Gem socket state
    // =========================================================================

    @Nullable
    public ItemStack getSocketedGem() {
        return socketedGem;
    }

    public void setSocketedGem(@Nullable ItemStack gem) {
        this.socketedGem = gem;
    }

    public boolean hasGem() {
        ItemStack gem = socketedGem;
        return gem != null && !gem.isEmpty();
    }

    // =========================================================================
    // Modifier merging
    // =========================================================================

    @Nonnull
    @Override
    public List<PerkAttributeModifier> getEffectiveModifiers(@Nonnull Player player) {
        List<PerkAttributeModifier> effective = new ArrayList<>(getModifiers());
        ItemStack gem = socketedGem;
        if (gem != null && !gem.isEmpty() && gem.getItem() instanceof IPerkGem gemItem) {
            effective.addAll(gemItem.getPerkModifiers(gem));
        }
        return effective;
    }

    // =========================================================================
    // NBT persistence
    // =========================================================================

    @Nonnull
    @Override
    public CompoundTag writeToNBT() {
        CompoundTag tag = super.writeToNBT();
        ItemStack gem = socketedGem;
        if (gem != null && !gem.isEmpty()) {
            tag.put(TAG_GEM, gem.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains(TAG_GEM)) {
            this.socketedGem = ItemStack.of(tag.getCompound(TAG_GEM));
        } else {
            this.socketedGem = null;
        }
    }
}
