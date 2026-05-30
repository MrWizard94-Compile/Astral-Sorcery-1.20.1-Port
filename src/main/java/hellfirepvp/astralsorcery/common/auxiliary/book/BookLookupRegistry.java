package hellfirepvp.astralsorcery.common.auxiliary.book;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.item.ItemComparator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping items to their journal pages so right-clicking
 * an item opens the relevant research node.
 *
 * <p>1.16 → 1.20: IItemProvider → ItemLike; LogicalSide removed —
 * client-side access uses {@link PlayerProgressManager#getClientProgress()}.</p>
 */
public class BookLookupRegistry {

    private static final Map<ItemStack, BookLookupInfo> lookupMap = new HashMap<>();

    private BookLookupRegistry() {}

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static BookLookupInfo findPage(ItemStack search) {
        PlayerProgress prog = PlayerProgressManager.getClientProgress();
        for (ItemStack compare : lookupMap.keySet()) {
            if (ItemComparator.compare(compare, search, ItemComparator.Clause.Sets.ITEMSTACK_CRAFTING)) {
                BookLookupInfo info = lookupMap.get(compare);
                if (info.canSee(prog)) {
                    return info;
                }
            }
        }
        return null;
    }

    public static void registerItemLookup(ItemLike item, ResearchNode parentNode, int nodePage,
                                          ResearchProgression neededProgression) {
        registerItemLookup(new ItemStack(item), parentNode, nodePage, neededProgression);
    }

    public static void registerItemLookup(ItemStack stack, ResearchNode parentNode, int nodePage,
                                          ResearchProgression neededProgression) {
        lookupMap.put(stack, new BookLookupInfo(parentNode, nodePage, neededProgression));
    }
}
