package hellfirepvp.astralsorcery.common.util;

/**
 * Simple mutable integer counter, used where a final-requirement lambda
 * needs a mutable int (e.g. forEach counting).
 */
public class Counter {

    private int value;

    public Counter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void decrement() {
        value--;
    }

    public void increment() {
        value++;
    }
}
