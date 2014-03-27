package parisolve.backend.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

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
    public final Collection<? extends ParityVertex> getWinningRegionForPlayer(
            final Arena arena, final Player player) {
        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
        // TODO: simplify this with streams and lambda-expressions
        final Set<ParityVertex> zeroVertices = new HashSet<>();
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPlayer() == Player.A) {
                zeroVertices.add(vertex);
            }
        }
        final HashSet<ParityVertex> oneVertices = new HashSet<>(vertices);
        oneVertices.removeAll(zeroVertices);

        final StrategyValuePair strategyValuePair = tryAllStrategiesForZeroRecursively(
                oneVertices, zeroVertices, strategy, arena,
                new StrategyValuePair());

        final Set<ParityVertex> winningRegion = new HashSet<>();
        for (final ParityVertex vertex : arena.getVertices()) {
            if (findWinner(vertex, strategyValuePair.getStrategy()) == player) {
                winningRegion.add(vertex);
            }
        }
        return winningRegion;
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
        final Collection<? extends ParityVertex> successors = arena
                .getSuccessors(vertex);
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
            int valueOfStrategy = getValueOfStrategy(strategy);
            if (valueOfStrategy < bestStrategySoFar.getStrategiesValue()) {
                bestStrategy = new StrategyValuePair(new ConcurrentHashMap<>(
                        strategy), valueOfStrategy);
            }
        } else {
            final ParityVertex vertex = oneVertices.iterator().next();
            oneVertices.remove(vertex);
            final Collection<? extends ParityVertex> successors = arena
                    .getSuccessors(vertex);
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
    private int getValueOfStrategy(
            final Map<ParityVertex, ParityVertex> strategy) {
        int numberOfVerticesToZero = 0;
        // the strategy contains all vertices as keys
        for (final ParityVertex vertex : strategy.keySet()) {
            if (findWinner(vertex, strategy) == Player.A) {
                numberOfVerticesToZero++;
            }
        }
        return numberOfVerticesToZero;
    }

    /**
     * given a fixed strategy and a starting point, determines, which player
     * would win. Follows strategy until a cycle is found and then determines
     * the highest value in cycle.
     * 
     * @param vertex
     *            the starting point
     * @param strategy
     *            the strategy fixed
     * @return number of player to win (either 0 or 1)
     */
    private static Player findWinner(final ParityVertex vertex,
            final Map<ParityVertex, ParityVertex> strategy) {
        ParityVertex currentVertex = vertex;
        final List<ParityVertex> path = new ArrayList<ParityVertex>();
        while (!path.contains(currentVertex)) {
            path.add(currentVertex);
            currentVertex = strategy.get(currentVertex);
        }
        final int loopStartIndex = path.lastIndexOf(currentVertex);
        int maxPriority = Integer.MIN_VALUE;
        for (int i = loopStartIndex; i < path.size(); i++) {
            if (path.get(i).getPriority() > maxPriority) {
                maxPriority = path.get(i).getPriority();
            }
        }
        return Player.getPlayerForInt(maxPriority);
    }

    /**
     * utility method for printing GraphViz-representation to inspect strategy.
     * That is, for every vertex only the outgoing edge, chosen by the strategy,
     * is shown.
     * 
     * @param strategy
     *            the strategy to convert
     * @return String in dot-format to be understood by GraphViz
     */
    private static String getGraphVizFromStrategy(
            final Map<ParityVertex, ParityVertex> strategy) {
        final StringBuilder resultBuilder = new StringBuilder(25);
        resultBuilder.append("digraph strategy {\n");

        // print vertices and store their numbers
        final Map<ParityVertex, Integer> numbersOfVertices = new ConcurrentHashMap<>();
        int numberOfVertex = 0;
        for (final ParityVertex vertex : strategy.keySet()) {
            resultBuilder
                    .append(String.format("  z%d[shape=%s,label=\"%d\"];\n",
                            numberOfVertex,
                            vertex.getPlayer().getShapeString(),
                            vertex.getPriority()));
            numbersOfVertices.put(vertex, numberOfVertex);
            numberOfVertex++;
        }

        resultBuilder.append('\n');

        // print edges
        for (final ParityVertex vertex : strategy.keySet()) {
            resultBuilder.append(String.format("  z%d->z%d;\n",
                    numbersOfVertices.get(vertex),
                    numbersOfVertices.get(strategy.get(vertex))));
        }
        resultBuilder.append('}');
        return resultBuilder.toString();
    }
}
