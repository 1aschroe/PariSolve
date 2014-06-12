package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.LiftableFactory;

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
    public final Collection<? extends ParityVertex> getWinningRegionForPlayer(
            final Arena arena, final Player player) {
        final Set<? extends ParityVertex> vertices = arena.getVertices();
        liftable = new LiftableFactory(vertices);
        final WinningRegionPartition partition = solveGame(vertices);
        return partition.getWinningRegionFor(player);
    }

    /**
     * liftable factory provided for a single run of the algorithm which knows
     * the predecessors and can provide liftable instances.
     */
    protected LiftableFactory liftable;

    /**
     * singleton for empty set to not create an empty set for every instance
     * when one is needed.
     */
    protected static final Set<ParityVertex> EMPTY_SET = new HashSet<ParityVertex>();

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
    protected final WinningRegionPartition solveGame(
            final Set<? extends ParityVertex> vertices) {
        // in Abbildung 15.5 this is n
        final int maxPriority = LinkedArena.getMaxPriority(vertices);
        if (maxPriority <= 0) {
            return new WinningRegionPartition(vertices, EMPTY_SET, Player.A);
        }

        WinningRegionPartition shortcut = takeShortcut(vertices, maxPriority);
        if (shortcut != null) {
            return shortcut;
        }

        final Player sigma = Player.getPlayerForPriority(maxPriority);

        final WinningRegionPartition dominionPartition = removeDominionOfSigmaOpponent(
                vertices, maxPriority, sigma);
        // 'verticesWinnableForSigma' is to be understood as 'vertices which
        // sigma _could_ win on'. This does not mean that (s)he does, nor does
        // it that (s)he does not. But it is not impossible from our point of
        // view right now.
        Collection<? extends ParityVertex> verticesWinnableForSigma = dominionPartition
                .getWinningRegionFor(sigma);

        final Collection<ParityVertex> dominionOfSigmaOpponent = getDominionOfSigmaOpponent(
                verticesWinnableForSigma, maxPriority, sigma);

        if (dominionOfSigmaOpponent.isEmpty()) {
            return dominionPartition;
        }

        // this is the end-recursive call
        // in Abbildung 15.5 partition2 is W''
        // in Schewe (2008) partition2 is W
        final WinningRegionPartition partition2 = solveGameForOtherVertices(
                verticesWinnableForSigma, dominionOfSigmaOpponent);
		// TODO: obviously the following assignment is not necessary. However,
		// the winning region of sigma's opponent consists of its dominion as
		// well as its winning region in the other vertices.
        final Collection<ParityVertex> winningRegionOfSigmaOpponent = dominionOfSigmaOpponent;
		winningRegionOfSigmaOpponent.addAll(partition2.getWinningRegionFor(sigma.getOponent()));
		return new WinningRegionPartition(partition2.getWinningRegionFor(sigma),
				winningRegionOfSigmaOpponent, sigma);
    }

    /**
     * makes it possible to calculate a dominion <code>D</code>of
     * <code>sigma.getOpponent()</code> and returning a partition (V\D, D). This
     * method makes it possible for <code>BigStepAlgorithm</code> to extend the
     * behaviour of <code>RecursiveAlgorithm</code>.
     * 
     * In this class a dummy-implementation is given.
     * 
     * @param vertices
     *            vertices to divide in a dominion and the rest
     * @param maxPriority
     *            the maximal priority of the game
     * @param sigma
     *            the player for which we want to find a set of vertices on
     *            which (s)he cannot win, so we can ignore it
     * @return a partition (V\D, D), where <code>sigma</code> cannot win on D.
     *         But no definite answer can be given to V\D.
     */
    protected WinningRegionPartition removeDominionOfSigmaOpponent(
            final Set<? extends ParityVertex> vertices,
            final int maxPriority, final Player sigma) {
        return new WinningRegionPartition(vertices, EMPTY_SET, sigma);
    }

    /**
     * calculates a dominion of <code>sigma.getOpponent()</code> so one can take
     * these vertices out of the problem as being solved. A dominion is a set,
     * from which the opposing player (in this case <code>sigma</code>) cannot
     * escape and which are part of the players (
     * <code>sigma.getOpponent()</code>) winning region.
     * 
     * @param vertices
     *            the vertices to consider
     * @param maxPriority
     *            the maximal priority in the whole graph
     * @param sigma
     *            the player for which we want to find a region, on which he
     *            looses
     * @return dominion of sigma's opponent.
     */
    protected final Collection<ParityVertex> getDominionOfSigmaOpponent(
            final Collection<? extends ParityVertex> vertices,
            final int maxPriority, final Player sigma) {
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
            return EMPTY_SET;
        }

        // in Abbildung 15.5 dominionOfSigmaOpponent is N''
        return getAttractor(partition.getWinningRegionFor(sigma.getOponent()),
                sigma.getOponent(), vertices);
    }

    /**
     * makes it possible to take a shortcut in the recursive algorithm before
     * any expensive computation is done.
     * 
     * It is used by <code>BigStepAlgorithm</code> to insert the d=2-case.
     * 
     * The return value is either <code>null</code> (which will then be ignored)
     * or the solution, that is the correct winning partition. Therefore, the
     * implementation in <code>RecursiveAlgorithm</code> is the
     * dummy-implementation of returning <code>null</code>.
     * 
     * @param vertices
     *            the vertices to consider
     * @param maxPriority
     *            the maximal priority of the vertices given
     * @return either <code>null</code> or the right partition
     */
    protected WinningRegionPartition takeShortcut(
            final Set<? extends ParityVertex> vertices,
            final int maxPriority) {
        return null;
    }

    /**
     * helper method: solves the game on the remaining vertices, when removing
     * <code>verticesToExclude</code> from <code>allVertices</code>.
     * 
     * @param allVertices
     *            the entirety of all vertices
     * @param verticesToExclude
     *            nodes to exclude when solving the game
     * @return partition of winning regions for the vertices left
     */
    protected final WinningRegionPartition solveGameForOtherVertices(
            final Collection<? extends ParityVertex> allVertices,
            final Collection<ParityVertex> verticesToExclude) {
        final Set<ParityVertex> unsolvedVertices = new HashSet<>(allVertices);
        unsolvedVertices.removeAll(verticesToExclude);
        return solveGame(unsolvedVertices);
    }

    /**
     * calculates the attractor of a given set of vertices with respect to
     * player <code>sigma</code>. That is, from the collection of vertices
     * returned, sigma can force every path into <code>vertices</code>.
     * 
     * @param vertices
     *            the vertices to attract to
     * @param sigma
     *            the player to force
     * @param allVertices
     *            the vertices to consider
     * @return the attractor of <code>sigma</code> to <code>vertices</code> with
     *         respect to the subgame on <code>allVertices</code>
     */
    protected final Collection<ParityVertex> getAttractor(
            final Collection<? extends ParityVertex> vertices,
            final Player sigma,
            final Collection<? extends ParityVertex> allVertices) {
        final Set<ParityVertex> attractor = new HashSet<>(vertices);
        // the vertices which might get into the attractor eventually
        final Set<ParityVertex> otherVertices = new HashSet<>(allVertices);
        otherVertices.removeAll(attractor);
        Liftable iterator = liftable.getLiftableInstance(otherVertices, true);

        for (final ParityVertex vertex : iterator) {
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
                iterator.liftWasSuccessful(vertex);
            }
        }

        return attractor;
    }
}
