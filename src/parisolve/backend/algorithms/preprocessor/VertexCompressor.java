package parisolve.backend.algorithms.preprocessor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.LiftableFactory;
import parisolve.backend.algorithms.helper.Solution;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class VertexCompressor {
    private VertexCompressor() {
        // disabled
    }

    protected static Solution solveCompressedArena(Arena arena,
            GraphPreprocessor preprocessor) {
        LinkedArena arenaToSolve = new LinkedArena();
        Map<ParityVertex, Set<ParityVertex>> predecessorMap = LiftableFactory
                .getPredecessorMap(arena.getVertices());
        BiMap<ParityVertex, ParityVertex> compressedPredecessors = HashBiMap
                .create();
        BiMap<ParityVertex, ParityVertex> compressedSuccessors = HashBiMap
                .create();
        BiMap<ParityVertex, ParityVertex> otherVertices = HashBiMap.create();
        transferAndCompressVertices(arena, arenaToSolve, predecessorMap,
                compressedPredecessors, compressedSuccessors);
        transferAndCompressEdges(arena, arenaToSolve, compressedPredecessors,
                compressedSuccessors, otherVertices);

        Solution solution = preprocessor.solveSelfcyclelessArena(arenaToSolve);

        final Map<ParityVertex, ParityVertex> strategy = solution.getStrategy();
        final Set<ParityVertex> winningRegionForA = solution
                .getWinningRegionFor(Player.A);
        final Map<ParityVertex, ParityVertex> actualStrategy = new ConcurrentHashMap<>();
        final Set<ParityVertex> actualWinningRegionForA = new HashSet<>();
        final Set<ParityVertex> actualWinningRegionForB = new HashSet<>();
        return transferSolutionBack(arenaToSolve, compressedPredecessors,
                compressedSuccessors, otherVertices, strategy,
                winningRegionForA, actualStrategy, actualWinningRegionForA,
                actualWinningRegionForB);
    }

    protected static Solution transferSolutionBack(LinkedArena arenaToSolve,
            BiMap<ParityVertex, ParityVertex> compressedPredecessors,
            BiMap<ParityVertex, ParityVertex> compressedSuccessors,
            BiMap<ParityVertex, ParityVertex> otherVertices,
            final Map<ParityVertex, ParityVertex> strategy,
            final Set<ParityVertex> winningRegionForA,
            final Map<ParityVertex, ParityVertex> actualStrategy,
            final Set<ParityVertex> actualWinningRegionForA,
            final Set<ParityVertex> actualWinningRegionForB) {
        for (final ParityVertex vertex : arenaToSolve.getVertices()) {
            if (strategy.containsKey(vertex)) {
                actualStrategy.put(
                        getVertexBack(vertex, compressedSuccessors,
                                otherVertices),
                        getVertexBack(strategy.get(vertex),
                                compressedPredecessors, otherVertices));
            }
            if (winningRegionForA.contains(vertex)) {
                actualWinningRegionForA.add(getVertexBack(vertex,
                        compressedPredecessors, otherVertices));
                actualWinningRegionForA.add(getVertexBack(vertex,
                        compressedSuccessors, otherVertices));
            } else {
                actualWinningRegionForB.add(getVertexBack(vertex,
                        compressedPredecessors, otherVertices));
                actualWinningRegionForB.add(getVertexBack(vertex,
                        compressedSuccessors, otherVertices));
            }
        }
        Solution actualSolution = new Solution(actualWinningRegionForA,
                actualWinningRegionForB, Player.A, actualStrategy);
        return actualSolution;
    }

    protected static void transferAndCompressEdges(Arena arena,
            LinkedArena arenaToSolve,
            BiMap<ParityVertex, ParityVertex> compressedPredecessors,
            BiMap<ParityVertex, ParityVertex> compressedSuccessors,
            BiMap<ParityVertex, ParityVertex> otherVertices) {
        for (final ParityVertex vertex : arena.getVertices()) {
            String vertexName;
            Set<? extends ParityVertex> successors = vertex.getSuccessors();
            if (compressedSuccessors.containsKey(vertex)) {
                vertexName = compressedSuccessors.get(vertex).getName();
            } else if (!compressedPredecessors.containsKey(vertex)) {
                vertexName = vertex.getName();
                otherVertices.put(vertex, arenaToSolve.getVertex(vertexName));
            } else {
                continue;
            }
            for (final ParityVertex successor : successors) {
                String successorName;
                if (compressedPredecessors.containsKey(successor)) {
                    successorName = compressedPredecessors.get(successor)
                            .getName();
                } else {
                    successorName = successor.getName();
                }
                arenaToSolve.addEdge(vertexName, successorName);
            }
        }
    }

    protected static void transferAndCompressVertices(Arena arena,
            LinkedArena arenaToSolve,
            Map<ParityVertex, Set<ParityVertex>> predecessorMap,
            BiMap<ParityVertex, ParityVertex> compressedPredecessors,
            BiMap<ParityVertex, ParityVertex> compressedSuccessors) {
        for (final ParityVertex vertex : arena.getVertices()) {
            if (vertex.getSuccessors().size() == 1) {
                final ParityVertex successor = vertex.getSuccessors()
                        .iterator().next();
                if (predecessorMap.get(successor).size() == 1) {
                    mapCompressableVertices(arenaToSolve,
                            compressedPredecessors, compressedSuccessors,
                            vertex, successor);
                }
            } else if (predecessorMap.containsKey(vertex)
                    && predecessorMap.get(vertex).size() == 1) {
                final ParityVertex predecessor = predecessorMap.get(vertex)
                        .iterator().next();
                if (predecessor.getSuccessors().size() == 1) {
                    mapCompressableVertices(arenaToSolve,
                            compressedPredecessors, compressedSuccessors,
                            predecessor, vertex);
                }
            }
            if (!compressedPredecessors.containsKey(vertex)
                    && !compressedSuccessors.containsKey(vertex)) {
                arenaToSolve.addVertex(vertex.getName(), vertex.getPriority(),
                        vertex.getPlayer());
            }
        }
    }

    protected static void mapCompressableVertices(LinkedArena arenaToSolve,
            BiMap<ParityVertex, ParityVertex> compressedPredecessors,
            BiMap<ParityVertex, ParityVertex> compressedSuccessors,
            final ParityVertex vertex, final ParityVertex successor) {
        String newVertexName = vertex.getName() + "+" + successor.getName();
        arenaToSolve.addVertex(newVertexName,
                Math.max(vertex.getPriority(), successor.getPriority()),
                successor.getPlayer());
        ParityVertex compressedVertex = arenaToSolve.getVertex(newVertexName);
        compressedPredecessors.put(vertex, compressedVertex);
        compressedSuccessors.put(successor, compressedVertex);
    }

    static ParityVertex getVertex(final ParityVertex vertex,
            final BiMap<ParityVertex, ParityVertex> compressedVertices,
            final BiMap<ParityVertex, ParityVertex> otherVertices) {
        if (otherVertices.containsKey(vertex)) {
            return otherVertices.get(vertex);
        }
        return compressedVertices.get(vertex);
    }

    static ParityVertex getVertexBack(final ParityVertex vertex,
            final BiMap<ParityVertex, ParityVertex> compressedVertices,
            final BiMap<ParityVertex, ParityVertex> otherVertices) {
        if (otherVertices.inverse().containsKey(vertex)) {
            return otherVertices.inverse().get(vertex);
        }
        return compressedVertices.inverse().get(vertex);
    }
}
