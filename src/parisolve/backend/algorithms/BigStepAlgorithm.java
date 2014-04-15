package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

public class BigStepAlgorithm extends RecursiveAlgorithm {

    protected WinningRegionPartition removeDominionOfSigmaOpponent(
            final Collection<? extends ParityVertex> vertices,
            final int maxPriority, final Player sigma) {
        return getWinningPartitionFromBetterAlgorithm(sigma.getOponent(),
                pi(vertices.size(), maxPriority + 1), vertices);
    }

    protected WinningRegionPartition takeShortcut(
            Collection<? extends ParityVertex> vertices, int maxPriority) {
        if (maxPriority == 2) {
            return getWinningPartitionFromBetterAlgorithm(Player.A,
                    vertices.size(), vertices);
        }
        return null;
    }

    private WinningRegionPartition getWinningPartitionFromBetterAlgorithm(
            Player sigma, int n, Collection<? extends ParityVertex> vertices) {
        Collection<? extends ParityVertex> winningRegion = BetterAlgorithm
                .solveGame(sigma, n, vertices, liftable);
        Set<ParityVertex> loosingRegion = new HashSet<>(vertices);
        loosingRegion.removeAll(winningRegion);
        return new WinningRegionPartition(winningRegion, loosingRegion,
                sigma);
    }

    private double gamma(final int colours) {
        final double subtrahent;
        if (colours % 2 == 0) {
            subtrahent = 1.0 / (3 * colours) + 4.0 / (colours * colours);
        } else {
            final int coloursHalve = colours / 2;
            subtrahent = 1.0 / (coloursHalve * (coloursHalve + 1));
        }
        return colours / 3.0 + 1.0 / 2 - subtrahent;
    }

    private double beta(int colours) {
        final int coloursHalve = (colours % 2 == 0) ? colours / 2 : colours / 2 + 1;
        return gamma(colours - 1) / coloursHalve;
    }

    private int pi(int n, int c) {
        final double rootExpression = 2.0 * Math.cbrt(c) * Math.pow(n, beta(c));
        return (int) Math.ceil(rootExpression * n + 2 * rootExpression - 2 * n / (n - rootExpression));
    }
}