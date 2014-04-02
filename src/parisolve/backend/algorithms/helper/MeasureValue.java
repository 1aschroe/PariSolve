package parisolve.backend.algorithms.helper;

import java.util.Arrays;

/**
 * This is a helper class for the better algorithm, representing the values in
 * M_G^T, their handling and comparison.
 * 
 * @author Arne Schröder
 * 
 */
public class MeasureValue implements Comparable<MeasureValue> {
    // TODO: make MeasureValue immutable
    private int[] value;

    /**
     * this field is the single instance of the maximal element Top. It
     * therefore alters a few methods, eliminating the need to have an
     * if-statement like <code>if (isT())</code> in the main class's methods.
     */
    private static final MeasureValue TOP = new MeasureValue(-1) {
        @Override
        public MeasureValue getProgValue(final int priority,
                final int[] sizeOfMG) {
            return this;
        };

        @Override
        public int compareTo(final MeasureValue value, final int maxComponents) {
            if (value.isTop()) {
                return 0;
            }
            return 1;
        };

        @Override
        public boolean isTop() {
            return true;
        };

        @Override
        public String toString() {
            return "T";
        }
    };

    private final int maxPriority;

    /**
     * create a new minimal <code>MeasureValue</code>
     * 
     * @param maxPriority
     */
    public MeasureValue(final int maxPriority) {
        this.maxPriority = maxPriority;
        value = new int[maxPriority + 1];
    }

    /**
     * create a new <code>MeasureValue</code> containing the values given.
     * 
     * @param value
     *            the value to be initialized from
     */
    private MeasureValue(final int[] value) {
        maxPriority = value.length - 1;
        this.value = value.clone();
    }

    /**
     * returns the singleton of the top-value.
     * 
     * @return TOP, the maximal <code>MeasureValue</code>
     */
    public static final MeasureValue getTopValue() {
        return TOP;
    }

    /**
     * compares the two values on their first components up to maxComponents and
     * returns a number greater than zero iff this is greater, a number smaller
     * than zero iff value is greater and zero iff this and value are equal on
     * the first components.
     * 
     * @param value
     * @param maxComponents
     * @return
     */
    public int compareTo(final MeasureValue value, final int maxComponents) {
        if (value.isTop()) {
            return -1;
        }
        for (int i = maxPriority; i >= maxComponents; i--) {
            if (this.value[i] != value.value[i]) {
                return this.value[i] - value.value[i];
            }
        }
        return 0;
    }

    @Override
    public final int compareTo(final MeasureValue o) {
        return compareTo(o, 0);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }

    /**
     * whether or not this <code>MeasureValue</code> is TOP. Note that this
     * implementation should be the same as
     * <code>this == MeasureValue.getTopValue()</code>.
     * 
     * @return whether this is TOP
     */
    public boolean isTop() {
        return false;
    }

    public MeasureValue getProgValue(final int priority, final int[] sizeOfMG) {
        // FIXME: this is probably where it becomes slow
        MeasureValue prog = new MeasureValue(value);
        for (int i = 0; i < priority; i++) {
            prog.value[i] = 0;
        }
        if (priority % 2 == 1) {
            prog.value[priority]++;
            if (prog.value[priority] > sizeOfMG[priority]) {
                prog = getTopValue();
            }
        }
        return prog;
    }
}
