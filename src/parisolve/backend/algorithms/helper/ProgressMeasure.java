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
     * In contrast to the book, we compare measures from the back to the front
     * in order to solve max-parity-games.
     */
    private final Map<ParityVertex, MeasureValue> measure = new ConcurrentHashMap<>();
    private final int[] sizeOfMG;
    private final int maxSumAllowed;

    public ProgressMeasure(final int maxPriority, final int[] sizeOfMG, int maxSumAllowed) {
        this.maxPriority = maxPriority;
        this.sizeOfMG = sizeOfMG;
        this.maxSumAllowed = maxSumAllowed;
    }

    /**
     * the progress measure's value for the vertex given
     * 
     * @param v
     *            the vertex to receive the progress measure's value for
     * @return the progress measure's value
     */
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
        if (!currentValue.isTop()) {
            final boolean searchForMax = v.getPlayer() == Player.B;
            final MeasureValue valueToCompareWith = prog(v, searchForMax);
            if (valueToCompareWith.compareTo(currentValue) > 0) {
                measure.put(v, valueToCompareWith);
                return true;
            }
        }
        return false;
    }

    /**
     * calculates the maximum or minimum value of prog(rho, v, w) for all
     * successors w of v as in LNCS 2500 - Definition 7.22 in order to lift rho
     * at v.The measure rho used is <code>this</code> measure.
     * 
     * @param v
     *            the vertex to calculate prog for
     * @param searchForMax
     *            <code>true</code> iff we are looking for the maximal value and
     *            <code>false</code> iff we are looking for a minimal value
     * @return the maximum or minimum value of prog(rho, v, w) respectively
     */
    private MeasureValue prog(final ParityVertex v, final boolean searchForMax) {
        final Collection<? extends ParityVertex> successors = v.getSuccessors();
        final int maxMinMultiplier = searchForMax ? 1 : -1;
        ParityVertex bestSuccessor = null;
        for (final ParityVertex w : successors) {
            if (bestSuccessor == null
                    || get(bestSuccessor).compareTo(get(w)) * maxMinMultiplier < 0) {
                bestSuccessor = w;
            }
        }
        return get(bestSuccessor).getProgValue(v.getPriority(), sizeOfMG, maxSumAllowed);
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