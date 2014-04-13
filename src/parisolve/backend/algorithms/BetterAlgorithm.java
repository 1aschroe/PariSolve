package parisolve.backend.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.ProgressMeasure;

/**
 * implementation of the algorithm of Jurdzinski - Small Progress Measures for
 * Solving Parity Games (2000), as described in section 7.5 in LNCS 2500. It
 * also stretches to the classes <code>ProgressMeasure</code> and
 * <code>MeasureValue</code> in the <code>helper</code>-package.
 * 
 * @author Arne Schröder
 */
public class BetterAlgorithm implements Solver {
    static class Liftable implements Iterable<ParityVertex>,
            Iterator<ParityVertex> {
        final private List<ParityVertex> vertices;
        private int currentIndex = 0;

        Liftable(final Collection<? extends ParityVertex> vertices) {
            this.vertices = new ArrayList<>(vertices);

        }

        /**
         * tells the <code>Liftable</code>, whether the given vertex was lifted
         * successfully.
         * 
         * @param vertex
         *            this vertex was lifted successfully.
         */
        void liftWasSuccessful(final ParityVertex vertex) {
            currentIndex = 0;
        }

        @Override
        public Iterator<ParityVertex> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < vertices.size();
        }

        @Override
        public ParityVertex next() {
            return vertices.get(currentIndex++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final Collection<? extends ParityVertex> getWinningRegionForPlayer(
            final Arena arena, final Player player) {
        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        return solveGame(player, vertices.size(), vertices);
    }

    /**
     * calculates a dominion of the player given, using a progress measure of
     * size <code>n</code> on the vertices given. If <code>n</code> equals the
     * number of vertices, the maximal dominion, the winning region of
     * <code>player</code> is calculated. This method is used in
     * <code>BigStepAlgorithm</code>.
     * 
     * @param player
     *            the player to solve the parity game for
     * @param n
     *            size of progress measure to consider
     * @param vertices
     *            the vertices to consider when solving the game
     * @return the winning region
     */
    public static Collection<? extends ParityVertex> solveGame(
            final Player player, final int n,
            final Collection<? extends ParityVertex> vertices) {
        int maxPriority = LinkedArena.getMaxPriority(vertices);
        final ProgressMeasure measure = new ProgressMeasure(maxPriority,
                getSizeOfMG(vertices, maxPriority), n);

        Liftable iterator = new Liftable(vertices);
        for (ParityVertex vertex : iterator) {
            boolean lifted = measure.lift(vertex);
            if (lifted) {
                iterator.liftWasSuccessful(vertex);
            }
        }

        return getWinningRegion(player, vertices, measure);
    }

    /**
     * determines the winning region from the measure given with respect to the
     * player specified. Iff <code>player</code> is player B, then these are all
     * the vertices with measure top. Iff <code>player</code> is player A, then
     * these are all the vertices with a measure not being top. This corresponds
     * to ||rho|| from Definition 7.19 and its complement.
     * 
     * @param player
     *            whose winning region to determine
     * @param vertices
     *            the vertices to consider for the winning region
     * @param measure
     *            the measure from which to derive the winning region
     * @return <code>player</code>'s winning region on <code>vertices</code>
     *         according to <code>measure</code>
     */
    protected static Set<ParityVertex> getWinningRegion(final Player player,
            final Collection<? extends ParityVertex> vertices,
            final ProgressMeasure measure) {
        final Set<ParityVertex> winningRegion = new HashSet<>();
        // TODO: the measure also knows all vertices. Therefore, the
        // parameter "vertices" is redundant
        for (final ParityVertex vertex : vertices) {
            if ((player == Player.B) == measure.get(vertex).isTop()) {
                winningRegion.add(vertex);
            }
        }
        return winningRegion;
    }

    /**
     * determines the size of M_G, as argued in the proof of Lemma 7.18. The
     * numbers in the array are the numbers of each priority and are used as
     * maximum value for each component.
     * 
     * @param vertices
     *            the vertices of G to consider
     * @param maxPriority
     *            the maximal priority in G. This could be determined from
     *            vertices. However, handing this as a parameter is saving one
     *            iteration over the vertices.
     * @return an array of the sizes of the components in M_G
     */
    protected final static int[] getSizeOfMG(
            final Collection<? extends ParityVertex> vertices,
            final int maxPriority) {
        final int[] counts = new int[maxPriority + 1];
        for (final ParityVertex vertex : vertices) {
            int priority = vertex.getPriority();
            if (priority % 2 == 1) {
                counts[priority]++;
            }
        }
        return counts;
    }
}
