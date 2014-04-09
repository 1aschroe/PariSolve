package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * implementation of a recursive algorithm given in McNaughton (1993), Zielonka
 * (1998), LNCS 2500 (Figure 6.3 and Figure 6.4) and
 * "Automatentheorie und Logik" by Hofmann and Lange (2011) section 15.1.3
 * respectively. However, in this implementation no winning strategy is
 * calculated and it is end-recursive instead of being iterative. It, therefore,
 * most closely resembles the algorithm described in
 * "Automatentheorie und Logik", which will be used as a reference.
 */
public class RecursiveAlgorithm implements Solver {
    @Override
    public Collection<? extends ParityVertex> getWinningRegionForPlayer(
            final Arena arena, final Player player) {
        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        final WinningRegionPartition partition = solveGame(vertices);
        return partition.getWinningRegionFor(player);
    }

    /**
     * singleton for empty set to not create an empty set for every instance
     * when one is needed.
     */
    private final static Set<ParityVertex> EMPTY_SET = new HashSet<ParityVertex>();

    /**
     * solves an arena specified by the vertices given by returning the
     * partition of the given vertices into the winning regions. This
     * corresponds to Abbildung 15.5 in Hofmann and Lange (2011) and to both
     * figure 6.3 and 6.4 in LNCS together.
     * 
     * @param vertices
     *            the vertices, to solve the parity game on. This is G in
     *            Abbildung 15.5
     * @return a partition with a set of vertices for each player to win upon.
     */
    private WinningRegionPartition solveGame(
            final Collection<? extends ParityVertex> vertices) {
        // in Abbildung 15.5 this is n
        final int maxPriority = LinkedArena.getMaxPriority(vertices);
        if (maxPriority <= 0) {
            return new WinningRegionPartition(vertices, EMPTY_SET, Player.A);
        }
        final Player sigma = Player.getPlayerForPriority(maxPriority);

        // in Abbildung 15.5 verticesWithMaxPriority is N
        final Set<ParityVertex> verticesWithMaxPriority = new HashSet<>();
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPriority() == maxPriority) {
                verticesWithMaxPriority.add(vertex);
            }
        }

        // in Abbildung 15.5 attractorOfMaxPrio is N'
        final Collection<ParityVertex> attractorOfMaxPrio = getAttractor(
                verticesWithMaxPriority, sigma, vertices);

        // in Abbildung 15.5 partition is W'
        final WinningRegionPartition partition = solveGameForOtherVertices(
                vertices, attractorOfMaxPrio);

        if (partition.getWinningRegionFor(sigma.getOponent()).isEmpty()) {
            // this means player sigma wins all vertices in G\N' and therefore
            // all in G
            return new WinningRegionPartition(vertices, EMPTY_SET, sigma);
        }

        // in Abbildung 15.5 dominionOfSigmaOpponent is N''
        final Collection<ParityVertex> dominionOfSigmaOpponent = getAttractor(
                partition.getWinningRegionFor(sigma.getOponent()),
                sigma.getOponent(), vertices);

        // this is the end-recursive call
        // in Abbildung 15.5 partition2 is W''
        final WinningRegionPartition partition2 = solveGameForOtherVertices(
                vertices, dominionOfSigmaOpponent);
        final Set<ParityVertex> winningRegion2 = new HashSet<>(
                partition2.getWinningRegionFor(sigma.getOponent()));
        winningRegion2.addAll(dominionOfSigmaOpponent);
        return new WinningRegionPartition(
                partition2.getWinningRegionFor(sigma), winningRegion2, sigma);
    }

    /**
     * helper method: solves the game on the remaining vertices, when removing
     * <code>verticesToExclude</code> from <code>allVertices</code>
     * 
     * @param allVertices
     *            the entirety of all vertices
     * @param verticesToExclude
     *            nodes to exclude when solving the game
     * @return partition of winning regions for the vertices left
     */
    private WinningRegionPartition solveGameForOtherVertices(
            final Collection<? extends ParityVertex> allVertices,
            final Collection<ParityVertex> verticesToExclude) {
        final Set<ParityVertex> unsolvedVertices = new HashSet<>(allVertices);
        unsolvedVertices.removeAll(verticesToExclude);
        return solveGame(unsolvedVertices);
    }

    private Collection<ParityVertex> getAttractor(
            final Collection<? extends ParityVertex> vertices,
            final Player sigma,
            final Collection<? extends ParityVertex> allVertices) {
        final Set<ParityVertex> attractor = new HashSet<>(vertices);
        // the vertices which might get into the attractor eventually
        final Set<ParityVertex> otherVertices = new HashSet<>(allVertices);
        int otherSize = otherVertices.size();
        otherVertices.removeAll(attractor);

        while (otherSize > otherVertices.size()) {
            otherSize = otherVertices.size();
            // TODO this loop could be optimised if one could call a vertex's
            // predecessor
            for (final ParityVertex vertex : otherVertices) {
                final Set<ParityVertex> successorsInSubGame = new HashSet<>(
                        vertex.getSuccessors());
                // getSuccessors returns all successor in the original graph.
                // Therefore, we must remove everything outside of allVertices.
                successorsInSubGame.retainAll(allVertices);
                if ((vertex.getPlayer() == sigma && !Collections.disjoint(
                        attractor, successorsInSubGame))
                        || (vertex.getPlayer() == sigma.getOponent() && attractor
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
