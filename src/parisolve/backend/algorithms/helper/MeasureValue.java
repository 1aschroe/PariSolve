package parisolve.backend.algorithms.helper;

import java.util.Arrays;
import java.util.Comparator;

import parisolve.backend.Player;

/**
 * This is a helper class for the better algorithm, representing the values in
 * M_G^T, their handling and comparison.
 * 
 * @author Arne Schröder
 */
public class MeasureValue implements Comparable<MeasureValue> {
    public static class MeasureValueComparator implements Comparator<MeasureValue>{
        final static MeasureValueComparator SINGLETON = new MeasureValueComparator();
        
        private MeasureValueComparator() {
            
        }
        
        public static MeasureValueComparator getInstance() {
            return SINGLETON;
        }
        
        @Override
        public int compare(MeasureValue o1, MeasureValue o2) {
            return o1.compareTo(o2);
        }
    }
    
    // TODO: make MeasureValue immutable
    /**
     * the measure value wrapped in this class.
     */
    private final int[] value;

    /**
     * this field is the single instance of the maximal element Top. It
     * therefore alters a few methods, eliminating the need to have an
     * if-statement like <code>if (isT())</code> in the main class's methods.
     */
    private static final MeasureValue TOP = new MeasureValue(-1) {
        @Override
        public MeasureValue getProgValue(final int priority,
                final Player player, final int[] sizeOfMG, final int n) {
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

    /**
     * the maximal priority a vertex can have.
     */
    private final int maxPriority;

    /**
     * create a minimal <code>MeasureValue</code> with the maximal priority
     * given. This should only be called once by the
     * <code>ProgressMeasure</code> to create a minimal value.
     * 
     * @param maxPriority
     *            the maximal priority in the arena given
     */
    public MeasureValue(final int maxPriority) {
        this.maxPriority = maxPriority;
        value = new int[maxPriority + 1];
    }

    /**
     * create a new <code>MeasureValue</code> containing the values given.
     * Should only be used by <code>MeasureValue</code> to create a
     * <code>MeasureValue</code> consistent with <code>prog</code> as in LNCS
     * 2500 - Definition 7.19.
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
     * compares the two values on their first components up to
     * <code>maxComponents</code> and returns a number greater than zero iff
     * this is greater, a number smaller than zero iff value is greater and zero
     * iff this and value are equal on the first components.
     * 
     * @param otherValue
     *            value to compare to
     * @param maxComponents
     *            maximal component to which the values are compared
     * @return value such that <code>sign(value) = this - otherValue</code>
     */
    public int compareTo(final MeasureValue otherValue, final int maxComponents) {
        if (otherValue.isTop()) {
            return -1;
        }
        for (int i = maxPriority; i >= maxComponents; i--) {
            if (this.value[i] != otherValue.value[i]) {
                return this.value[i] - otherValue.value[i];
            }
        }
        return 0;
    }

    @Override
    public final int compareTo(final MeasureValue otherValue) {
        return compareTo(otherValue, 0);
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

    /**
     * calculates m = prog(rho, v, w) \in M_G^T as in LNCS 2500 - Definition
     * 7.19 for <code>this</code> being rho(w), <code>priority</code> = Omega(v)
     * and <code>sizeOfMG</code> being the maximal value for a
     * <code>MeasureValue</code> before it can be treated as Top.
     * 
     * @param priority
     *            the priority of v, the predecessor of w, which's value
     *            <code>this</code> is
     * @param sizeOfMG
     *            maximal value of <code>MeasureValue</code>
     * @param maxSumAllowed
     *            maximal sum of the values in <code>MeasureValue</code>
     *            allowed. This is used in the <code>BigStepAlgorithm</code>
     * @return prog(rho, v, w)
     */
    public MeasureValue getProgValue(final int priority, final Player player,
            final int[] sizeOfMG, final int maxSumAllowed) {
        if (valueMustBeGreater(priority, player)) {
            // prioToRaise points to the smallest priority which one can
            // increase to become greater than this without becoming TOP
            int prioToRaise = priority;
            do {
                if (value[prioToRaise] < sizeOfMG[prioToRaise]) {
                    break;
                }
                prioToRaise += 2;
            } while (prioToRaise <= maxPriority);
            if (prioToRaise > maxPriority
                    || value[prioToRaise] >= sizeOfMG[prioToRaise]) {
                return getTopValue();
            }
            int sum = 0;
            for (int prio = prioToRaise; prio <= maxPriority; prio++) {
                sum += value[prio];
            }
            if (sum >= maxSumAllowed) {
                return getTopValue();
            }
            MeasureValue prog = getMinimalValueEqualToPriority(prioToRaise);
            prog.value[prioToRaise]++;
            return prog;
        } else {
            return getMinimalValueEqualToPriority(priority);
        }
    }

    /**
     * @param priority
     *            priority from which to compare to <code>this</code>
     * @return minimal value which is equal to <code>this</code> from
     *         <code>priority</code> on.
     */
    protected MeasureValue getMinimalValueEqualToPriority(final int priority) {
        // FIXME: this is probably where it becomes slow
        MeasureValue prog = new MeasureValue(value);
        for (int i = 0; i < priority; i++) {
            prog.value[i] = 0;
        }
        return prog;
    }

    /**
     * @return whether or not m has to be greater or only greater or equal to
     *         rho(w) as in Definition 7.19. The idea behind this condition is
     *         that this vertex is good in terms of priority for player's
     *         opponent.
     */
    public static boolean valueMustBeGreater(final int priority, final Player player) {
        return priority % 2 == player.getOponent().getNumber();
    }
}
