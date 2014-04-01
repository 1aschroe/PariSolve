package parisolve.backend.algorithms.helper;

import java.util.Arrays;

public class MeasureValue implements Comparable<MeasureValue> {
    int[] value;

    static MeasureValue T = new MeasureValue(-1) {
        public MeasureValue getProgValue(int priority, long sizeOfMG) {
            return this;
        };

        public int compareTo(MeasureValue value, int maxComponents) {
            if (value.isT()) {
                return 0;
            }
            return 1;
        };

        public boolean isT() {
            return true;
        };

        @Override
        public String toString() {
            return "T";
        }
    };

    private int maxPriority;

    public MeasureValue(int maxPriority) {
        this.maxPriority = maxPriority;
        value = new int[maxPriority + 1];
    }
    
    private MeasureValue(int[] value) {
        maxPriority = value.length - 1;
        this.value = value.clone();
    }

    static MeasureValue getTValue() {
        return T;
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
    public int compareTo(MeasureValue value, int maxComponents) {
        if (value.isT()) {
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
    public int compareTo(MeasureValue o) {
        return compareTo(o, 0);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }

    public boolean isT() {
        return false;
    }

    public MeasureValue getProgValue(int priority, long sizeOfMG) {
        // FIXME: this is probably where it becomes slow
        MeasureValue prog = new MeasureValue(value);
        for (int i = 0; i < priority; i++) {
            prog.value[i] = 0;
        }
        if (priority % 2 == 1) {
            prog.value[priority]++;
            if (prog.value[priority] > sizeOfMG) {
                return getTValue();
            }
        }
        return prog;
    }
}
