package hellfirepvp.astralsorcery.common;

import hellfirepvp.astralsorcery.common.util.Counter;
import net.minecraft.util.Tuple;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Server-side delayed task queue. Tasks enqueued with {@link #addRunnable(Runnable, int)}
 * are executed after the specified number of server ticks.
 *
 * <p>Thread-safe: tasks may be enqueued from off-thread (e.g., network handler
 * threads) and are flushed into the main queue at the end of each server tick.</p>
 *
 * <p>Register on the Forge event bus via
 * {@code MinecraftForge.EVENT_BUS.register(CommonScheduler.INSTANCE)}.</p>
 *
 * <p>1.16 → 1.20: ITickHandler → Forge SubscribeEvent; Tuple still in net.minecraft.util.</p>
 */
public class CommonScheduler {

    public static final CommonScheduler INSTANCE = new CommonScheduler();

    private static final Object lock = new Object();

    private boolean inTick = false;
    private final LinkedList<Tuple<Runnable, Counter>> queue   = new LinkedList<>();
    private final LinkedList<Tuple<Runnable, Integer>> waiting = new LinkedList<>();

    private CommonScheduler() {}

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
            return;
        }

        synchronized (lock) {
            inTick = true;
            Iterator<Tuple<Runnable, Counter>> it = queue.iterator();
            while (it.hasNext()) {
                Tuple<Runnable, Counter> entry = it.next();
                entry.getB().decrement();
                if (entry.getB().getValue() <= 0) {
                    entry.getA().run();
                    it.remove();
                }
            }
            inTick = false;
            for (Tuple<Runnable, Integer> w : waiting) {
                queue.addLast(new Tuple<>(w.getA(), new Counter(w.getB())));
            }
            waiting.clear();
        }
    }

    /**
     * Schedules {@code r} to run after {@code tickDelay} server ticks.
     * Safe to call from any thread.
     */
    public void addRunnable(Runnable r, int tickDelay) {
        synchronized (lock) {
            if (inTick) {
                waiting.addLast(new Tuple<>(r, tickDelay));
            } else {
                queue.addLast(new Tuple<>(r, new Counter(tickDelay)));
            }
        }
    }
}
