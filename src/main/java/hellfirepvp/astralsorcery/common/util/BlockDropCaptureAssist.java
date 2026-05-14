package hellfirepvp.astralsorcery.common.util;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import javax.annotation.Nonnull;
import java.util.Stack;

/**
 * Captures item drops from block breaking by intercepting entity spawn events.
 * Used to redirect block drops during programmatic block breaking (e.g., vortex, ritual).
 *
 * <p>1.16 → 1.20 changes:
 * EntityJoinWorldEvent → EntityJoinLevelEvent,
 * event.getWorld() → event.getLevel(),
 * ServerWorld → ServerLevel,
 * entity.remove() → entity.discard()</p>
 */
public class BlockDropCaptureAssist {

    public static final BlockDropCaptureAssist INSTANCE = new BlockDropCaptureAssist();

    private static final Stack<NonNullList<ItemStack>> capturing = new Stack<>();

    private BlockDropCaptureAssist() {}

    public void onDrop(@Nonnull EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel
                && event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack itemStack = itemEntity.getItem();
            if (!capturing.isEmpty()) {
                event.setCanceled(true);
                if (!itemStack.isEmpty()) {
                    // Concurrency can sometimes empty the stack between checks
                    if (!capturing.isEmpty()) {
                        capturing.peek().add(itemStack);
                    }
                }
                event.getEntity().discard();
            }
        }
    }

    public static void startCapturing() {
        capturing.push(NonNullList.create());
    }

    @Nonnull
    public static NonNullList<ItemStack> getCapturedStacksAndStop() {
        return capturing.pop();
    }
}
