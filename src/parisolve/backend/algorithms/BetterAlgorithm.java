package parisolve.backend.algorithms;

import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.LiftableFactory;
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
    @Override
    public final Solution getSolution(final Arena arena) {
        final Set<? extends ParityVertex> vertices = arena.getVertices();
        final LiftableFactory liftable = new LiftableFactory(vertices);
        return solveGame(vertices.size(), vertices, liftable);
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
     * @param liftable
     *            a liftable factory which has the arena's vertices stored and
     *            can be queried for a liftable instance
     * @return the winning region
     */
    public static Solution solveGame(final int n,
            final Set<? extends ParityVertex> vertices,
            final LiftableFactory liftable) {
        final int maxPriority = LinkedArena.getMaxPriority(vertices);
        final Player sigma = Player.getPlayerForPriority(maxPriority);
        final ProgressMeasure measure = new ProgressMeasure(vertices, sigma, n);

        final Liftable iterator = liftable.getLiftableInstance(vertices, false);
        for (final ParityVertex vertex : iterator) {
            final boolean lifted = measure.lift(vertex);
            if (lifted) {
                iterator.liftWasSuccessful(vertex);
            }
        }

        return measure.getSolution();
    }
}
