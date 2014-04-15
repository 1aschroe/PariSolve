package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.ProgressMeasure;
import parisolve.backend.algorithms.helper.SetStackLiftable;

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
        final ProgressMeasure measure = new ProgressMeasure(vertices, n);

        final Liftable iterator = new SetStackLiftable(vertices, false);
        for (final ParityVertex vertex : iterator) {
            final boolean lifted = measure.lift(vertex);
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
}
