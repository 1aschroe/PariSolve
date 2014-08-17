package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.MeasureValue.MeasureValueComparator;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

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
    /**
     * the player from who's perspective this progress measure is to be built,
     * that is Top are winning for player.getOponnent().
     */
    private final Player player;
    private final Set<? extends ParityVertex> vertices;

    /**
     * create <code>ProgressMeasure</code> on the given vertices which is
     * restricted by <code>maxSumAllowed</code>.
     * 
     * @param vertices
     *            vertices to apply the progress measure on
     * @param player
     *            the player from who's perspective this progress measure is to
     *            be built, that is Top are winning for player.getOponnent()
     * @param maxSumAllowed
     *            maximal sum of the values in <code>MeasureValue</code>
     *            allowed. This is used in the <code>BigStepAlgorithm</code>
     */
    public ProgressMeasure(final Set<ParityVertex> vertices,
            final Player player, final int maxSumAllowed) {
        this.vertices = vertices;
        this.player = player;
        final int maxPriority = Arena.getMaxPriority(vertices);
        this.sizeOfMG = getSizeOfMG(vertices, player, maxPriority);
        this.maxSumAllowed = maxSumAllowed;
        minMeasure = new MeasureValue(maxPriority);
    }

    public final Player getPlayer() {
        return player;
    }

    public final Set<? extends ParityVertex> getVertices() {
        return vertices;
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
        final SetView<? extends ParityVertex> successors = Sets.intersection(
                v.getSuccessors(), vertices);
        if (priority % 2 == player.getOponent().getNumber()
                && (successors.size() == 1 || v.getPlayer() == player
                        .getOponent()) && successors.contains(v)) {
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
     * @param successors
     *            vertices to consider
     * @return the maximal value of the vertices given
     */
    private MeasureValue getMaxValueOf(
            final Collection<? extends ParityVertex> successors) {
        return successors.stream().map(this::get)
                .max(MeasureValueComparator.getInstance()).orElse(minMeasure);
    }

    /**
     * find the minimal <code>MeasureValue</code> of the <code>vertices</code>
     * given.
     * 
     * @param successors
     *            vertices to consider
     * @return the minimal value of the vertices given
     */
    private MeasureValue getMinValueOf(
            final Collection<? extends ParityVertex> successors) {
        return successors.stream().map(this::get)
                .min(MeasureValueComparator.getInstance())
                .orElse(MeasureValue.getTopValue());
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
     * determines the winning region from this measure with respect to the
     * player specified. Iff <code>sigma</code> is player B, then these are all
     * the vertices with measure top. Iff <code>sigma</code> is player A, then
     * these are all the vertices with a measure not being top. This corresponds
     * to ||rho|| from Definition 7.19 and its complement.
     * 
     * @param sigma
     *            player whose winning region to determine
     * @return <code>sigma</code>'s winning region
     */
    public final Solution getSolution() {
        final Set<ParityVertex> winningRegionForA = new HashSet<>();
        final Set<ParityVertex> winningRegionForB = new HashSet<>();
        // TODO: extract strategy
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : measure.keySet()) {
            if ((Player.B == player) == get(vertex).isTop()) {
                winningRegionForA.add(vertex);
            } else {
                winningRegionForB.add(vertex);
            }
        }
        return new Solution(winningRegionForA, winningRegionForB, Player.A,
                strategy);
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
            final Player player, final int maxPriority) {
        final int[] counts = new int[maxPriority + 1];
        for (final ParityVertex vertex : vertices) {
            final int priority = vertex.getPriority();
            if (MeasureValue.valueMustBeGreater(priority, player)) {
                counts[priority]++;
            }
        }
        return counts;
    }

    public void setToTop(ParityVertex vertex) {
        measure.put(vertex, MeasureValue.getTopValue());
    }
}