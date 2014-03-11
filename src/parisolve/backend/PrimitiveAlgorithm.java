package parisolve.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Naive approach by trying all possible strategies, to have a comparison
 * 
 */
public class PrimitiveAlgorithm implements Solver {
    class StrategyValuePair {
        private Map<ParityVertex, ParityVertex> strategy = null;
        private int strategiesValue = Integer.MIN_VALUE;

        public StrategyValuePair(HashMap<ParityVertex, ParityVertex> strategy, int value) {
            this.strategy = strategy;
            this.strategiesValue = value;
        }

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
    public Collection<? extends ParityVertex> getWinningRegionForPlayer(Arena arena, int player) {
        Collection<? extends ParityVertex> vertices = arena.getVertices();
        Map<ParityVertex, ParityVertex> strategy = new HashMap<>();
        // TODO: simplify this with streams and lambda-expressions
        Set<ParityVertex> zeroVertices = new HashSet<>();
        for (ParityVertex vertex : vertices) {
            if (vertex.getPlayer() == 0) {
                zeroVertices.add(vertex);
            }
        }
        HashSet<ParityVertex> oneVertices = new HashSet<>(vertices);
        oneVertices.removeAll(zeroVertices);

        StrategyValuePair strategyValuePair = tryAllStrategiesForZeroRecursively(oneVertices, zeroVertices, strategy, arena, new StrategyValuePair());
        
        Set<ParityVertex> winningRegion = new HashSet<>();
        for (ParityVertex vertex : arena.getVertices()) {
            if (findWinner(vertex, strategyValuePair.getStrategy()) == player) {
                winningRegion.add(vertex);
            }
        }
        return winningRegion;
    }

    private StrategyValuePair tryAllStrategiesForZeroRecursively(Set<ParityVertex> oneVertices, Set<ParityVertex> zeroVertices,
            Map<ParityVertex, ParityVertex> strategy, Arena arena, StrategyValuePair bestStrategy) {
        if (zeroVertices.isEmpty()) {
            StrategyValuePair newStrategyWithValue = tryAllStrategiesForOneRecursively(oneVertices, arena, strategy, new StrategyValuePair(null, Integer.MAX_VALUE));
            if (newStrategyWithValue.getStrategiesValue() > bestStrategy.getStrategiesValue()) {
                return newStrategyWithValue;
            }
            return bestStrategy;
        }
        ParityVertex vertex = zeroVertices.iterator().next();
        zeroVertices.remove(vertex);
        Collection<? extends ParityVertex> successors = arena.getSuccessors(vertex);
        for (ParityVertex successor : successors) {
            strategy.put(vertex, successor);
            bestStrategy = tryAllStrategiesForZeroRecursively(oneVertices, zeroVertices, strategy, arena, bestStrategy);
        }
        zeroVertices.add(vertex);
        return bestStrategy;
    }

    private StrategyValuePair tryAllStrategiesForOneRecursively(Set<ParityVertex> oneVertices, Arena arena, Map<ParityVertex, ParityVertex> strategy,
            StrategyValuePair bestStrategy) {
        if (oneVertices.isEmpty()) {
            int numberOfVerticesToZero = 0;
            for (ParityVertex vertex : arena.getVertices()) {
                if (findWinner(vertex, strategy) == 0) {
                    numberOfVerticesToZero++;
                }
            }
            if (numberOfVerticesToZero < bestStrategy.getStrategiesValue()) {
                StrategyValuePair pair = new StrategyValuePair(new HashMap<>(strategy), numberOfVerticesToZero);
                return pair;
            }
            return bestStrategy;
        }
        ParityVertex vertex = oneVertices.iterator().next();
        oneVertices.remove(vertex);
        Collection<? extends ParityVertex> successors = arena.getSuccessors(vertex);
        for (ParityVertex successor : successors) {
            strategy.put(vertex, successor);
            bestStrategy = tryAllStrategiesForOneRecursively(oneVertices, arena, strategy, bestStrategy);
        }
        oneVertices.add(vertex);
        return bestStrategy;
    }

    private static int findWinner(ParityVertex vertex, Map<ParityVertex, ParityVertex> strategy) {
        ParityVertex currentVertex = vertex;
        List<ParityVertex> path = new ArrayList<ParityVertex>();
        while (!path.contains(currentVertex)) {
            path.add(currentVertex);
            currentVertex = strategy.get(currentVertex);
        }
        int loopStartIndex = path.lastIndexOf(currentVertex);
        int maxParity = Integer.MIN_VALUE;
        for (int i = loopStartIndex; i < path.size(); i++) {
            if (path.get(i).getParity() > maxParity) {
                maxParity = path.get(i).getParity();
            }
        }
        return maxParity % 2;
    }
    
    private static String getGraphVizFromStrategy(Map<ParityVertex, ParityVertex> strategy) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("digraph strategy {\n");
        Map<ParityVertex, Integer> numbersOfVertices = new HashMap<>();
        int i = 0;
        for (ParityVertex vertex : strategy.keySet()) {
            resultBuilder.append(String.format("  z%d[shape=%s,label=\"%d\"];\n", i, vertex.getPlayer() == 0 ? "oval" : "box", vertex.getParity()));
            numbersOfVertices.put(vertex, i++);
        }
        resultBuilder.append("\n");
        for (ParityVertex vertex : strategy.keySet()) {
            resultBuilder.append(String.format("  z%d->z%d;\n", numbersOfVertices.get(vertex), numbersOfVertices.get(strategy.get(vertex))));
        }
        resultBuilder.append("}");
        return resultBuilder.toString();
    }
}
