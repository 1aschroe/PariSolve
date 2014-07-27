package parisolve.backend.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.Solution;

/**
 * Naive approach, trying all possible strategies and comparing their value for
 * a specific player. Implemented to have a comparison for future algorithms.
 */
public class PrimitiveAlgorithm implements Solver {
    /**
     * Helper-class, to be able to return both strategy and its value. A
     * strategies value is the number of vertices in a player's winning region.
     */
    class StrategyValuePair {
        /**
         * the strategy.
         */
        private Map<ParityVertex, ParityVertex> strategy;
        /**
         * the strategy's value.
         */
        private int strategiesValue = Integer.MIN_VALUE;

        /**
         * creates a pair of a strategy and it's value to not have the need to
         * recalculate the strategy's value for every comparison.
         * 
         * @param strategy
         *            the strategy
         * @param value
         *            the strategy's value
         */
        public StrategyValuePair(
                final Map<ParityVertex, ParityVertex> strategy, final int value) {
            this.strategy = strategy;
            this.strategiesValue = value;
        }

        /**
         * creates a strategy-value-pair with its default values. That is, the
         * strategy is null and its value is minimal.
         */
        public StrategyValuePair() {
            // leaves the values at their default
        }

        public Map<ParityVertex, ParityVertex> getStrategy() {
            return strategy;
        }

        public int getStrategiesValue() {
            return strategiesValue;
        }
    }

    @Override
    public final Solution getSolution(final Arena arena) {
        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
        final Set<ParityVertex> zeroVertices = vertices.parallelStream()
                .filter(vertex -> vertex.getPlayer() == Player.A)
                .collect(Collectors.toSet());
        final Set<ParityVertex> oneVertices = new HashSet<>(vertices);
        oneVertices.removeAll(zeroVertices);

        final StrategyValuePair strategyValuePair = tryAllStrategiesForZeroRecursively(
                oneVertices, zeroVertices, strategy, arena,
                new StrategyValuePair());

        return getSolutionUsingStrategy(strategyValuePair.getStrategy());
    }

    /**
     * recursive function to fix strategy for player 0.
     * 
     * @param oneVertices
     *            the vertices of player 1
     * @param zeroVertices
     *            the vertices of player 0, which no successor has been chosen
     *            for yet
     * @param arena
     *            the arena this games takes place in
     * @param strategy
     *            the fixed (partial) strategy so far
     * @param bestStrategySoFar
     *            the (for player 0) best full strategy, known so far, and its
     *            value
     * @return the (for player 0) best full strategy for the fixed strategy
     *         <code>strategy</code> and its value
     */
    private StrategyValuePair tryAllStrategiesForZeroRecursively(
            final Set<ParityVertex> oneVertices,
            final Set<ParityVertex> zeroVertices,
            final Map<ParityVertex, ParityVertex> strategy, final Arena arena,
            final StrategyValuePair bestStrategySoFar) {
        if (zeroVertices.isEmpty()) {
            final StrategyValuePair newStrategyWithValue = tryAllStrategiesForOneRecursively(
                    oneVertices, arena, strategy, new StrategyValuePair(null,
                            Integer.MAX_VALUE));
            if (newStrategyWithValue.getStrategiesValue() > bestStrategySoFar
                    .getStrategiesValue()) {
                return newStrategyWithValue;
            }
            return bestStrategySoFar;
        }
        final ParityVertex vertex = zeroVertices.iterator().next();
        zeroVertices.remove(vertex);
        final Collection<? extends ParityVertex> successors = vertex
                .getSuccessors();
        StrategyValuePair bestStrategy = bestStrategySoFar;
        for (final ParityVertex successor : successors) {
            strategy.put(vertex, successor);
            bestStrategy = tryAllStrategiesForZeroRecursively(oneVertices,
                    zeroVertices, strategy, arena, bestStrategy);
        }
        zeroVertices.add(vertex);
        return bestStrategy;
    }

    /**
     * recursive function to fix strategy for player 1.
     * 
     * @param oneVertices
     *            the vertices of player 1, which no successor has been chosen
     *            for yet
     * @param arena
     *            the arena this games takes place in
     * @param strategy
     *            the fixed (partial) strategy so far
     * @param bestStrategySoFar
     *            the (for player 1) best full strategy, known so far, and its
     *            value
     * @return the (for player 1) best full strategy for the fixed strategy
     *         <code>strategy</code> and its value
     */
    private StrategyValuePair tryAllStrategiesForOneRecursively(
            final Set<ParityVertex> oneVertices, final Arena arena,
            final Map<ParityVertex, ParityVertex> strategy,
            final StrategyValuePair bestStrategySoFar) {
        StrategyValuePair bestStrategy = bestStrategySoFar;
        if (oneVertices.isEmpty()) {
            final int valueOfStrategy = getSolutionUsingStrategy(strategy)
                    .getWinningRegionFor(Player.A).size();
            if (valueOfStrategy < bestStrategySoFar.getStrategiesValue()) {
                bestStrategy = new StrategyValuePair(new ConcurrentHashMap<>(
                        strategy), valueOfStrategy);
            }
        } else {
            final ParityVertex vertex = oneVertices.iterator().next();
            oneVertices.remove(vertex);
            final Collection<? extends ParityVertex> successors = vertex
                    .getSuccessors();
            for (final ParityVertex successor : successors) {
                strategy.put(vertex, successor);
                bestStrategy = tryAllStrategiesForOneRecursively(oneVertices,
                        arena, strategy, bestStrategy);
            }
            oneVertices.add(vertex);
        }
        return bestStrategy;
    }

    /**
     * determines a strategy's value as stored in StrategyValuePair.
     * 
     * @param strategy
     *            the strategy to test
     * @return strategy's value
     */
    private Solution getSolutionUsingStrategy(
            final Map<ParityVertex, ParityVertex> strategy) {
        final Set<ParityVertex> winningForZero = new HashSet<>();
        final Set<ParityVertex> winningForOne = new HashSet<>();
        // the strategy contains all vertices as keys
        for (final ParityVertex vertex : strategy.keySet()) {
            if (winningForZero.contains(vertex)
                    || winningForOne.contains(vertex)) {
                continue;
            }
            ParityVertex currentVertex = vertex;
            final List<ParityVertex> path = new ArrayList<ParityVertex>();
            while (!path.contains(currentVertex)
                    && !(winningForZero.contains(vertex) || winningForOne
                            .contains(vertex))) {
                path.add(currentVertex);
                currentVertex = strategy.get(currentVertex);
            }
            final Player player;
            if (winningForZero.contains(vertex)) {
                player = Player.A;
            } else if (winningForOne.contains(vertex)) {
                player = Player.B;
            } else {
                // path.contains(currentVertex)
                final int loopStartIndex = path.lastIndexOf(currentVertex);
                final List<ParityVertex> loop = path.subList(loopStartIndex,
                        path.size());
                final int maxPriority = Arena.getMaxPriority(loop);
                player = Player.getPlayerForPriority(maxPriority);
            }
            if (player == Player.A) {
                winningForZero.addAll(path);
            } else {
                winningForOne.addAll(path);
            }
        }
        return new Solution(winningForZero, winningForOne, Player.A, strategy);
    }
}
