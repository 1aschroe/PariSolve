package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;

/**
 * implementation of the recursive algorithm given in Automatentheorie und Logik
 * 15.1.3 and Jurdzinski (2000) respectively
 */
public class RecursiveAlgorithm implements Solver {
    /**
     * Helper class to return a partition of winning regions.
     */
    class WinningRegionPartition {
        public final Collection<? extends ParityVertex> winningRegionForOne;
        public final Collection<? extends ParityVertex> winningRegionForZero;

        // TODO: do I need this?

        WinningRegionPartition(
                final Collection<? extends ParityVertex> winningRegionForZero,
                final Collection<? extends ParityVertex> winningRegionForOne,
                int sigma) {
            if (sigma == 0) {
                this.winningRegionForZero = winningRegionForZero;
                this.winningRegionForOne = winningRegionForOne;
            } else {
                this.winningRegionForZero = winningRegionForOne;
                this.winningRegionForOne = winningRegionForZero;
            }
        }

        Collection<? extends ParityVertex> getWinningRegionFor(final int player) {
            if (player == 0) {
                return winningRegionForZero;
            } else {
                return winningRegionForOne;
            }
        }
    }

    @Override
    public Collection<? extends ParityVertex> getWinningRegionForPlayer(
            Arena arena, int player) {
        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        WinningRegionPartition partition = solveGame(vertices);
        return partition.getWinningRegionFor(player);
    }

    private final static Set<ParityVertex> EMPTY_SET = new HashSet<ParityVertex>();

    /**
     * corresponds to Abbildung 15.5 in Automatentheorie und Logik
     * 
     * @param vertices
     *            this is G in Abbildung 15.5
     * @return
     */
    private WinningRegionPartition solveGame(
            final Collection<? extends ParityVertex> vertices) {
        // in Abbildung 15.5 this is n
        int maxPriority = Integer.MIN_VALUE;
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPriority() > maxPriority) {
                maxPriority = vertex.getPriority();
            }
        }
        if (maxPriority <= 0) {
            return new WinningRegionPartition(vertices, EMPTY_SET, 0);
        }
        final int sigma = maxPriority % 2;

        // in Abbildung 15.5 this is N
        final Set<ParityVertex> verticesWithMaxPriority = new HashSet<>();
        // TODO: this can be included into the previous loop
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPriority() == maxPriority) {
                verticesWithMaxPriority.add(vertex);
            }
        }

        // in Abbildung 15.5 this is N'
        final Collection<ParityVertex> attractor = getAttractor(
                verticesWithMaxPriority, sigma, vertices);

        final Set<ParityVertex> unsolvedVertices = new HashSet<>(vertices);
        unsolvedVertices.removeAll(attractor);
        final WinningRegionPartition partition = solveGame(unsolvedVertices);

        if (partition.getWinningRegionFor(1 - sigma).isEmpty()) {
            final Set<ParityVertex> winningRegion = new HashSet<>(
                    partition.getWinningRegionFor(sigma));
            winningRegion.addAll(attractor);
            return new WinningRegionPartition(winningRegion, EMPTY_SET, sigma);
        }

        // in Abbildung 15.5 this is N''
        Collection<ParityVertex> attractor2 = getAttractor(
                partition.getWinningRegionFor(1 - sigma), 1 - sigma, vertices);

        final Set<ParityVertex> unsolvedVertices2 = new HashSet<>(vertices);
        unsolvedVertices2.removeAll(attractor2);
        final WinningRegionPartition partition2 = solveGame(unsolvedVertices2);
        final Set<ParityVertex> winningRegion2 = new HashSet<>(
                partition2.getWinningRegionFor(1 - sigma));
        winningRegion2.addAll(attractor2);
        return new WinningRegionPartition(partition2.getWinningRegionFor(sigma),
                winningRegion2, sigma);
    }

    private Collection<ParityVertex> getAttractor(
            final Collection<? extends ParityVertex> vertices, final int sigma,
            Collection<? extends ParityVertex> allVertices) {
        Set<ParityVertex> attractor = new HashSet<>(vertices);
        Set<ParityVertex> otherVertices = new HashSet<>(allVertices);
        int otherSize = otherVertices.size();
        otherVertices.removeAll(attractor);

        while (otherSize > otherVertices.size()) {
            otherSize = otherVertices.size();
            for (ParityVertex vertex : otherVertices) {
                Set<ParityVertex> successorsInSubGame = new HashSet<>(vertex.getSuccessors());
                successorsInSubGame.retainAll(allVertices);
                if ((vertex.getPlayer() == sigma && !Collections.disjoint(
                        attractor, successorsInSubGame))
                        || (vertex.getPlayer() == 1 - sigma && attractor
                                .containsAll(successorsInSubGame))) {
                    attractor.add(vertex);
                }
            }
            // TODO: only remove those which are new
            otherVertices.removeAll(attractor);
        }

        return attractor;
    }
}
