package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * helper class for better algorithm, representing a progress measure, which
 * will after all lifting operations become a game progress measur. It assigns a
 * <code>MeasureValue</code> representing a value of M_G^T to every vertex in a
 * arena. It provides <code>lift()</code>, the central method to the
 * <code>BetterAlgorithm</code> and can return the <code>MeasureValue</code> of
 * vertices via <code>get()</code>.
 * 
 * @author Arne Schröder
 */
public class ProgressMeasure {
    /**
     * the maximal priority in the arena considered.
     */
    private int maxPriority;
    /**
     * represents the map from V to M_G^T. In contrast to the book, we compare
     * measures from the back to the front in order to solve max-parity-games.
     */
    private final Map<ParityVertex, MeasureValue> measure = new ConcurrentHashMap<>();
    /**
     * maximal dimensions of M_G before it becomes Top. This is the number of
     * vertices of each odd priority.
     */
    private final int[] sizeOfMG;
    /**
     * can be used to restrict the <code>ProgressMeasure</code> to measure
     * values which's entries sum up to maximal <code>maxSumAllowed</code>. This
     * is used in <code>BigStelAlgorithm</code>.
     */
    private final int maxSumAllowed;

    /**
     * the minimal value a <code>ProgressMeasure</code> can assign to a vertex.
     * It is everywhere 0.
     */
    private final MeasureValue minMeasure;
    private Player player;

    /**
     * create <code>ProgressMeasure</code> on the given vertices which is
     * restricted by <code>maxSumAllowed</code>.
     * 
     * @param vertices
     *            vertices to apply the progress measure on
     * @param player
     *            the player from who's perspective this progress measure is to
     *            be built
     * @param maxSumAllowed
     *            maximal sum of the values in <code>MeasureValue</code>
     *            allowed. This is used in the <code>BigStepAlgorithm</code>
     */
    public ProgressMeasure(final Collection<? extends ParityVertex> vertices,
            final Player player, final int maxSumAllowed) {
        this.player = player;
        this.maxPriority = LinkedArena.getMaxPriority(vertices);
        this.sizeOfMG = getSizeOfMG(vertices, player, maxPriority);
        this.maxSumAllowed = maxSumAllowed;
        minMeasure = new MeasureValue(maxPriority);
    }

    /**
     * the progress measure's value for the vertex given.
     * 
     * @param v
     *            the vertex to receive the progress measure's value for
     * @return the progress measure's value
     */
    public final MeasureValue get(final ParityVertex v) {
        if (!measure.containsKey(v)) {
            measure.put(v, minMeasure);
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
            final boolean searchForMax = v.getPlayer() == player.getOponent();
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
        final int priority = v.getPriority();
        // TODO: check, whether this works with sum-games.
        final Collection<? extends ParityVertex> successors = v.getSuccessors();
        if (priority % 2 == player.getOponent().getNumber()
                && (successors.size() == 1 || v.getPlayer() == player.getOponent())
                && successors.contains(v)) {
            return MeasureValue.getTopValue();
        }
        final MeasureValue bestSuccessorValue;
        if (searchForMax) {
            bestSuccessorValue = getMaxValueOf(successors);
        } else {
            bestSuccessorValue = getMinValueOf(successors);
        }
        return bestSuccessorValue.getProgValue(priority, player, sizeOfMG,
                maxSumAllowed);
    }

    /**
     * find the maximal <code>MeasureValue</code> of the <code>vertices</code>
     * given.
     * 
     * @param vertices
     *            vertices to consider
     * @return the maximal value of the vertices given
     */
    private MeasureValue getMaxValueOf(
            final Collection<? extends ParityVertex> vertices) {
        MeasureValue maxValue = minMeasure;
        for (final ParityVertex vertex : vertices) {
            final MeasureValue value = get(vertex);
            if (maxValue.compareTo(value) < 0) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    /**
     * find the minimal <code>MeasureValue</code> of the <code>vertices</code>
     * given.
     * 
     * @param vertices
     *            vertices to consider
     * @return the minimal value of the vertices given
     */
    private MeasureValue getMinValueOf(
            final Collection<? extends ParityVertex> vertices) {
        MeasureValue minValue = MeasureValue.getTopValue();
        for (final ParityVertex vertex : vertices) {
            final MeasureValue value = get(vertex);
            if (minValue.compareTo(value) > 0) {
                minValue = value;
            }
        }
        return minValue;
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

    /**
     * determines the size of M_G, as argued in the proof of Lemma 7.18. The
     * numbers in the array are the numbers of each priority and are used as
     * maximum value for each component.
     * 
     * @param vertices
     *            the vertices of G to consider
     * @param player
     * @param maxPriority
     *            the maximal priority in G. This could be determined from
     *            vertices. However, handing this as a parameter is saving one
     *            iteration over the vertices.
     * @return an array of the sizes of the components in M_G
     */
    protected static final int[] getSizeOfMG(
            final Collection<? extends ParityVertex> vertices,
            Player player, final int maxPriority) {
        final int[] counts = new int[maxPriority + 1];
        for (final ParityVertex vertex : vertices) {
            final int priority = vertex.getPriority();
            if (priority % 2 == player.getOponent().getNumber()) {
                counts[priority]++;
            }
        }
        return counts;
    }
}