package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

public class ProgressMeasure {
    private int maxPriority;
    /**
     * represents the map from V to M_G^T. The empty array is interpreted as T.
     * value[0] is a_1, value[1] is a_3 and so on. In contrast to the book, we
     * compare measures from the back to the front in order to solve
     * max-parity-games. This behaviour is encoded in getIndexFromPriority.
     */
    private final Map<ParityVertex, MeasureValue> measure = new ConcurrentHashMap<>();
    private final long sizeOfMG;

    public ProgressMeasure(final int maxPriority, final long sizeOfMG) {
        this.maxPriority = maxPriority;
        this.sizeOfMG = sizeOfMG;
    }

    public MeasureValue get(final ParityVertex v) {
        if (!measure.containsKey(v)) {
            MeasureValue value = new MeasureValue(maxPriority);
            measure.put(v, value);
        }
        return measure.get(v);
    }

    /**
     * lifts vertex u as in Definition 7.22 and assigns its value back to this
     * ProgressMeasure-instance. That is, it implements mu := Lift(mu, v) as of
     * LNCS 2500 top of p. 123
     * 
     * @param v
     *            the vertex to lift the progress measure on
     * @return whether this has changed anything
     */
    public final boolean lift(final ParityVertex v) {
        final MeasureValue currentValue = get(v);
        if (currentValue.isTop()) {
            return false;
        }
        final Collection<? extends ParityVertex> successors = v.getSuccessors();
        final int maxOrMin;
        if (v.getPlayer() == Player.A) {
            maxOrMin = -1;
        } else {
            maxOrMin = 1;
        }
        final MeasureValue valueToCompareWith = prog(v, successors, maxOrMin);
        if (valueToCompareWith.compareTo(currentValue) > 0) {
            measure.put(v, valueToCompareWith);
            return true;
        }
        return false;
    }

    /**
     * 
     * @param v
     * @param successors
     * @param max
     *            is 1 iff we are looking for the maximal value and -1 iff
     *            looking for a minimal value
     * @return
     */
    private MeasureValue prog(final ParityVertex v,
            final Collection<? extends ParityVertex> successors, final int max) {
        ParityVertex bestSuccessor = null;
        for (final ParityVertex w : successors) {
            if (bestSuccessor == null
                    || get(bestSuccessor).compareTo(get(w)) * max < 0) {
                bestSuccessor = w;
            }
        }
        return get(bestSuccessor).getProgValue(v.getPriority(), sizeOfMG);
    }

    /**
     * calculates prog as in LNCS 2500 Definition 7.19. The measure used is
     * <code>this</code> measure.
     * 
     * @param v
     * @param w
     * @return
     */
    public MeasureValue prog(final ParityVertex v, final ParityVertex w) {
        return get(w).getProgValue(v.getPriority(), sizeOfMG);
    }

    @Override
    public String toString() {
        final StringBuilder resultBuilder = new StringBuilder();
        for (final Entry<ParityVertex, MeasureValue> pair : measure.entrySet()) {
            resultBuilder.append(pair.getKey() + " -> " + pair.getValue()
                    + "\n");
        }
        return resultBuilder.toString();
    }
}