package parisolve.backend.algorithms;

import java.util.Set;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.RecursiveAlgorithm;
import parisolve.backend.algorithms.helper.Solution;

public class MediumSizedStepAlgorithm extends RecursiveAlgorithm {

    @Override
    protected final Solution takeShortcut(final Set<ParityVertex> vertices,
            final int maxPriority) {
        if (maxPriority == 2) {
            return getWinningPartitionFromSmallMeasureAlgorithm(Player.A,
                    vertices.size(), vertices);
        }
        return null;
    }

    /**
     * helper method to access the <code>SmallMeasureAlgorithm</code> to
     * retrieve a partition of the vertices given. Schewe proved in Theorem 5
     * that this always returns a dominion of <code>sigma</code>'s opponent and
     * returns the correct solution for <code>n = vertices.size()</code>.
     * 
     * @param sigma
     *            player who we want to find a loosing region for
     * @param n
     *            restriction on the progress measure
     * @param vertices
     *            vertices to consider
     * @return partition into (V\D, D) with D being a dominion of
     *         <code>sigma</code>'s opponent
     */
    private Solution getWinningPartitionFromSmallMeasureAlgorithm(
            final Player sigma, final int n, final Set<ParityVertex> vertices) {
        return SmallMeasureAlgorithm.solveGame(n, vertices, liftable);
    }
}
