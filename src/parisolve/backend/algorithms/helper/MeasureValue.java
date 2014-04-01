package parisolve.backend.algorithms.helper;

import java.util.Arrays;

public class MeasureValue implements Comparable<MeasureValue> {
    Integer[] value;

    // TODO: does -1 work?
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
        value = new Integer[getIndexFromPriority(maxPriority) + 1];
        Arrays.fill(value, 0);
    }

    static MeasureValue getTValue() {
        return T;
    }

    /**
     * compares the two values on their first components up to maxComponents and
     * returns a number greater than zero iff value1 is greater, a number
     * smaller than zero iff value2 is greater and zero iff value1 and value2
     * are the same on the first components.
     * 
     * @param value1
     * @param value2
     * @param maxComponents
     * @return
     */
    public int compareTo(MeasureValue value, int maxComponents) {
        if (value.isT()) {
            return -1;
        }
        for (int i = getIndexFromPriority(maxPriority); i >= getIndexFromPriority(maxComponents); i--) {
            if (this.value[i] != value.value[i]) {
                return this.value[i] - value.value[i];
            }
        }
        return 0;
    }

    private static int getIndexFromPriority(int priority) {
        return priority;
    }

    @Override
    public int compareTo(MeasureValue o) {
        return compareTo(o, maxPriority);
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }

    public boolean isT() {
        return false;
    }

    public MeasureValue getProgValue(int priority, long sizeOfMG) {
        MeasureValue prog = new MeasureValue(maxPriority);
        prog.value = value.clone();
        for (int i = 0; i < getIndexFromPriority(priority); i++) {
            prog.value[i] = 0;
        }
        if (priority % 2 == 1) {
            prog.value[getIndexFromPriority(priority)]++;
            if (prog.value[getIndexFromPriority(priority)] > sizeOfMG) {
                return getTValue();
            }
        }
        return prog;
    }
}
