package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * implementation of the algorithm given in FSTTCS 2007 - Solving Parity Games
 * in Big Steps by Sven Schewe. The algorithms pseudo code is given in figure 1
 * of that paper. The idea is to combine the algorithms
 * <code>RecursiveAlgorithm</code> and <code>BetterAlgorithm</code> in such a
 * way that the overall algorithm of <code>RecursiveAlgorithm</code> is used but
 * for <code>maxPriority == 2</code> the original <code>BetterAlgorithm</code>
 * is used and for <code>maxPriority > 2</code> <code>BetterAlgorithm</code> is
 * used while restricting its progress measure. This results in
 * <code>BetterAlgorithm</code> to return much more quickly and calculating
 * dominions rather than winning regions. Dominions are regions, from which the
 * opponent cannot escape and which are winning for the player given.
 * 
 * The implementation is done by extending <code>RecursiveAlgorithm</code>,
 * replacing dummy methods with the appropriate code.
 * 
 * @author Arne Schröder
 */
public class BigStepAlgorithm extends RecursiveAlgorithm {
    /**
     * this is done in Schewe (2007) Fig. 1. line 7. (a)
     */
    @Override
    protected final AttractorStrategyPair getDominionOfSigmaOpponentQuickly(
            final Set<ParityVertex> vertices, final int maxPriority,
            final Player sigma) {
        //TODO: not sure whether we do this correctly as some runs are much slower for BetterAlgorithm than others.
        final Solution approximation = getWinningPartitionFromBetterAlgorithm(sigma.getOponent(),
                pi(vertices.size(), maxPriority + 1), vertices);
        return new AttractorStrategyPair(approximation.getWinningRegionFor(sigma.getOponent()), approximation.getStrategy());
    }

    @Override
    protected final Solution takeShortcut(
            final Set<ParityVertex> vertices, final int maxPriority) {
        if (maxPriority == 2) {
            return getWinningPartitionFromBetterAlgorithm(Player.A,
                    vertices.size(), vertices);
        }
        return null;
    }

    /**
     * helper method to access the <code>BetterAlgorithm</code> to retrieve a
     * partition of the vertices given. Schewe proved in Theorem 5 that this
     * always returns a dominion of <code>sigma</code>'s opponent and returns
     * the correct solution for <code>n = vertices.size()</code>.
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
    private Solution getWinningPartitionFromBetterAlgorithm(final Player sigma,
            final int n, final Set<ParityVertex> vertices) {
        return BetterAlgorithm.solveGame(n, vertices, liftable);
    }

    /**
     * implementation of the formula given in Schewe (2007) p. 457.
     * 
     * @param colours
     *            number of colours
     * @return
     */
    private double gamma(final int colours) {
        if (colours <= 1) {
            return 0;
        }
        final double subtrahent;
        if (colours % 2 == 0) {
            subtrahent = 1.0 / (3 * colours) + 4.0 / (colours * colours);
        } else {
            final int coloursHalve = colours / 2;
            subtrahent = 1.0 / (coloursHalve * (coloursHalve + 1));
        }
        return colours / 3.0 + 1.0 / 2 - subtrahent;
    }

    /**
     * implementation of the formula given in Schewe (2007) p. 457.
     * 
     * @param colours
     *            number of colours
     * @return
     */
    private double beta(final int colours) {
        final int coloursHalve = (colours % 2 == 0) ? colours / 2
                : colours / 2 + 1;
        return gamma(colours - 1) / coloursHalve;
    }

    /**
     * parameter for restricting the codomain of the progress measure used in
     * <code>BetterAlgorithm</code>. This implementation is the solution of the
     * inequation given at the bottom of p. 457:
     * 
     * n / (π(n,c)+2) < n^(1−β(c)) / (2 3√ c )− 1
     * 
     * @param n
     *            number of vertices
     * @param c
     *            number of priorities (Schewe calls them colours)
     * @return smallest natural number that satisfies the inequation
     */
    private int pi(final int n, final int c) {
        final double rootExpression = 2.0 * Math.cbrt(c) * Math.pow(n, beta(c));
        return (int) Math.ceil(rootExpression * n + 2 * rootExpression - 2 * n
                / (n - rootExpression));
    }
}