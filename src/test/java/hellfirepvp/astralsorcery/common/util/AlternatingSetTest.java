package hellfirepvp.astralsorcery.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlternatingSet — the ObserverLib replacement
 * used by EffectHandler for tick-and-prune iteration.
 */
class AlternatingSetTest {

    private AlternatingSet<String> set;

    @BeforeEach
    void setUp() {
        set = new AlternatingSet<>();
    }

    @Test
    void addAndSize() {
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        set.add("alpha");
        set.add("beta");

        assertFalse(set.isEmpty());
        assertEquals(2, set.size());
    }

    @Test
    void removeElement() {
        set.add("alpha");
        set.add("beta");
        set.add("gamma");

        assertTrue(set.remove("beta"));
        assertEquals(2, set.size());

        assertFalse(set.remove("nonexistent"));
        assertEquals(2, set.size());
    }

    @Test
    void forEachKeepsWhenTrue() {
        set.add("alpha");
        set.add("beta");
        set.add("gamma");

        List<String> visited = new ArrayList<>();
        set.forEach(elem -> {
            visited.add(elem);
            return true; // keep all
        });

        assertEquals(3, visited.size());
        assertEquals(3, set.size());
    }

    @Test
    void forEachRemovesWhenFalse() {
        set.add("alpha");
        set.add("beta");
        set.add("gamma");

        // Remove "beta" during iteration
        set.forEach(elem -> !elem.equals("beta"));

        assertEquals(2, set.size());
        List<String> remaining = new ArrayList<>();
        set.forEach(elem -> {
            remaining.add(elem);
            return true;
        });
        assertTrue(remaining.contains("alpha"));
        assertFalse(remaining.contains("beta"));
        assertTrue(remaining.contains("gamma"));
    }

    @Test
    void forEachRemovesMultiple() {
        set.add("keep");
        set.add("remove1");
        set.add("remove2");
        set.add("keep2");

        set.forEach(elem -> elem.startsWith("keep"));

        assertEquals(2, set.size());
    }

    @Test
    void clear() {
        set.add("alpha");
        set.add("beta");
        assertFalse(set.isEmpty());

        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    void forEachElementVisitsAll() {
        set.add("alpha");
        set.add("beta");

        List<String> visited = new ArrayList<>();
        set.forEachElement(visited::add);
        assertEquals(2, visited.size());
        assertEquals("alpha", visited.get(0));
        assertEquals("beta", visited.get(1));
    }
}
