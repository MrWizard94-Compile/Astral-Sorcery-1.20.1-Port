package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Knowledge Fragment — collectable item that accumulates constellation discoveries.
 * Stores a list of constellation ResourceLocations in NBT. When enough fragments
 * are combined, the player discovers a new constellation.
 *
 * <p>1.16 -> 1.20 changes:
 * addInformation -> appendHoverText,
 * ITextComponent -> Component,
 * CompoundNBT -> CompoundTag,
 * ListNBT -> ListTag,
 * StringNBT -> StringTag,
 * INBT -> Tag,
 * Constants.NBT -> Tag.TAG_STRING</p>
 */
public class ItemKnowledgeFragment extends ItemAS {

    private static final String TAG_CONSTELLATIONS = "constellations";

    public ItemKnowledgeFragment() {
        super(defaultProperties().stacksTo(16));
    }

    /**
     * Add a constellation to this knowledge fragment's stored list.
     *
     * @param stack         the item stack
     * @param constellation the constellation to add
     */
    public static void addConstellation(@Nonnull ItemStack stack, @Nonnull ResourceLocation constellation) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(TAG_CONSTELLATIONS, Tag.TAG_STRING);
        String value = constellation.toString();

        // Avoid duplicates
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(value)) {
                return;
            }
        }

        list.add(StringTag.valueOf(value));
        tag.put(TAG_CONSTELLATIONS, list);
    }

    /**
     * Get all constellations stored on this knowledge fragment.
     *
     * @param stack the item stack
     * @return an immutable list of constellation ResourceLocations (never null, may be empty)
     */
    @Nonnull
    public static List<ResourceLocation> getConstellations(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CONSTELLATIONS, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag list = tag.getList(TAG_CONSTELLATIONS, Tag.TAG_STRING);
        List<ResourceLocation> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            String value = list.getString(i);
            if (!value.isEmpty()) {
                result.add(new ResourceLocation(value));
            }
        }
        return List.copyOf(result);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nullable Level level,
                                @Nonnull List<Component> tooltip,
                                @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        List<ResourceLocation> constellations = getConstellations(stack);
        if (!constellations.isEmpty()) {
            tooltip.add(Component.translatable(
                    "astralsorcery.tooltip.knowledge_fragment.count",
                    constellations.size()));
        }
    }
}
