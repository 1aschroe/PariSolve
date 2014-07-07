package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import parisolve.backend.Arena;
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
    /**
     * singleton for empty set to not create an empty set for every instance
     * when one is needed.
     */
    protected static final Set<ParityVertex> EMPTY_SET = new HashSet<ParityVertex>();

    @Override
    public final Solution getSolution(final Arena arena) {
        final Set<ParityVertex> vertices = arena.getVertices();
        liftable = new LiftableFactory(vertices);
        return solveGame(vertices);
    }

    /**
     * liftable factory provided for a single run of the algorithm which knows
     * the predecessors and can provide liftable instances.
     */
    protected LiftableFactory liftable;

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
    protected final Solution solveGame(final Set<ParityVertex> vertices) {
        // in Abbildung 15.5 this is n
        final int maxPriority = Arena.getMaxPriority(vertices);
        if (maxPriority <= 0) {
            return new Solution(vertices, Player.A);
        }

        // this enables BigStepAlgorithm to take a shortcut
        final Solution shortcut = takeShortcut(vertices, maxPriority);
        if (shortcut != null) {
            return shortcut;
        }

        final Player sigma = Player.getPlayerForPriority(maxPriority);

        // this enables BigStepAlgorithm to calculate a dominion of
        // sigma.getOpponent() using BetterAlgorithm with small n
        final AttractorStrategyPair dominion = getDominionOfSigmaOpponentQuickly(
                vertices, maxPriority, sigma);

        // vertices can be reused because it is not used in this method any more
        final Set<ParityVertex> verticesWinnableForSigma = vertices;
        // 'verticesWinnableForSigma' is to be understood as 'vertices which
        // sigma _could_ win on'. This does not mean that (s)he wins, nor does
        // it mean that (s)he does not win. But it is not possible right now to
        // rule out that (s)he could win.
        verticesWinnableForSigma.removeAll(dominion.getAttractor());

        final AttractorStrategyPair dominionOfSigmaOpponent = getDominionOfSigmaOpponent(
                verticesWinnableForSigma, maxPriority, sigma);

        if (dominionOfSigmaOpponent.getAttractor().isEmpty()) {
            Map<ParityVertex, ParityVertex> strategy = dominionOfSigmaOpponent
                    .getStrategy();
            strategy.putAll(dominion.getStrategy());
            addArbitraryStrategyForSigmaOnVertices(sigma,
                    verticesWinnableForSigma, strategy);
            return new Solution(verticesWinnableForSigma,
                    dominion.getAttractor(), sigma, strategy);
        }

        // this is the end-recursive call
        // in Abbildung 15.5 partition2 is W''
        // in Schewe (2008) partition2 is W
        final Solution partition2 = solveGameForOtherVertices(
                verticesWinnableForSigma,
                dominionOfSigmaOpponent.getAttractor());
        // TODO: obviously the following assignment is not necessary. However,
        // the winning region of sigma's opponent consists of its dominion as
        // well as its winning region in the other vertices.
        final Set<ParityVertex> winningRegionOfSigmaOpponent = dominionOfSigmaOpponent
                .getAttractor();
        winningRegionOfSigmaOpponent.addAll(partition2
                .getWinningRegionFor(sigma.getOponent()));
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
        strategy.putAll(dominionOfSigmaOpponent.getStrategy());
        strategy.putAll(partition2.getStrategy());
        return new Solution(partition2.getWinningRegionFor(sigma),
                winningRegionOfSigmaOpponent, sigma, strategy);
    }

    protected static void addArbitraryStrategyForSigmaOnVertices(
            final Player sigma,
            final Set<ParityVertex> verticesWinnableForSigma,
            Map<ParityVertex, ParityVertex> strategy) {
        for (final ParityVertex vertex : verticesWinnableForSigma) {
            if (vertex.getPlayer() == sigma && !strategy.containsKey(vertex)) {
                ParityVertex successor = vertex
                        .getSuccessorFromSet(verticesWinnableForSigma);
                if (successor != null) {
                    strategy.put(vertex, successor);
                }
            }
        }
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
    protected AttractorStrategyPair getDominionOfSigmaOpponentQuickly(
            final Set<ParityVertex> vertices, final int maxPriority,
            final Player sigma) {
        return EMPTY_PAIR;
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
    protected final AttractorStrategyPair getDominionOfSigmaOpponent(
            final Collection<? extends ParityVertex> vertices,
            final int maxPriority, final Player sigma) {
        // in Abbildung 15.5 verticesWithMaxPriority is N
        final Set<ParityVertex> verticesWithMaxPriority = vertices
                .parallelStream()
                .filter(vertex -> vertex.getPriority() == maxPriority)
                .collect(Collectors.toSet());

        // in Abbildung 15.5 attractorOfMaxPrio is N'
        final AttractorStrategyPair attractorOfMaxPrio = getAttractor(
                verticesWithMaxPriority, sigma, vertices);

        // in Abbildung 15.5 partition is W'
        final Solution solutionWithoutMaxPrio = solveGameForOtherVertices(
                vertices, attractorOfMaxPrio.getAttractor());

        if (solutionWithoutMaxPrio.getWinningRegionFor(sigma.getOponent())
                .isEmpty()) {
            // this means player sigma wins all vertices in G\N' and therefore
            // all in G
            Map<ParityVertex, ParityVertex> strategy = solutionWithoutMaxPrio
                    .getStrategy();
            strategy.putAll(attractorOfMaxPrio.getStrategy());
            return new AttractorStrategyPair(EMPTY_SET, strategy);
        }

        // in Abbildung 15.5 dominionOfSigmaOpponent is N''
        AttractorStrategyPair dominion = getAttractor(
                solutionWithoutMaxPrio.getWinningRegionFor(sigma.getOponent()),
                sigma.getOponent(), vertices);

        for (final ParityVertex from : solutionWithoutMaxPrio.getStrategy()
                .keySet()) {
            if (!dominion.getStrategy().containsKey(from)) {
                dominion.getStrategy().put(from,
                        solutionWithoutMaxPrio.getStrategy().get(from));
            }
        }
        return dominion;
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
    protected Solution takeShortcut(final Set<ParityVertex> vertices,
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
    protected final Solution solveGameForOtherVertices(
            final Collection<? extends ParityVertex> allVertices,
            final Collection<ParityVertex> verticesToExclude) {
        final Set<ParityVertex> unsolvedVertices = new HashSet<>(allVertices);
        unsolvedVertices.removeAll(verticesToExclude);
        return solveGame(unsolvedVertices);
    }

    /**
     * represents an attractor of a set together with the strategy to get to
     * that set.
     * 
     * @author Arne Schröder
     */
    public static class AttractorStrategyPair {
        private final Set<ParityVertex> attractor;
        private final Map<ParityVertex, ParityVertex> strategy;

        public AttractorStrategyPair(final Set<ParityVertex> attractor,
                final Map<ParityVertex, ParityVertex> strategy) {
            this.attractor = attractor;
            this.strategy = strategy;
        }

        public Set<ParityVertex> getAttractor() {
            return attractor;
        }

        public Map<ParityVertex, ParityVertex> getStrategy() {
            return strategy;
        }
    }

    public static final AttractorStrategyPair EMPTY_PAIR = new AttractorStrategyPair(
            EMPTY_SET, new ConcurrentHashMap<ParityVertex, ParityVertex>());

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
    protected final AttractorStrategyPair getAttractor(
            final Collection<? extends ParityVertex> vertices,
            final Player sigma,
            final Collection<? extends ParityVertex> allVertices) {
        final Set<ParityVertex> attractor = new HashSet<>(vertices);
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
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
            if (vertex.getPlayer() == sigma.getOponent()
                    && attractor.containsAll(successorsInSubGame)) {
                attractor.add(vertex);
                iterator.liftWasSuccessful(vertex);
            } else if (vertex.getPlayer() == sigma) {
                for (final ParityVertex successor : successorsInSubGame) {
                    if (attractor.contains(successor)) {
                        attractor.add(vertex);
                        iterator.liftWasSuccessful(vertex);
                        strategy.put(vertex, successor);
                        break;
                    }
                }
            }
        }

        return new AttractorStrategyPair(attractor, strategy);
    }
}
