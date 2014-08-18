package parisolve.backend.algorithms.preprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.LiftableFactory;
import parisolve.backend.algorithms.helper.Solution;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;

public class VertexCompressor {
    private VertexCompressor() {
        // disabled
    }

    protected static Solution solveCompressedArena(Arena arena,
            GraphPreprocessor preprocessor) {
        LinkedArena arenaToSolve = new LinkedArena();
        Map<ParityVertex, Set<ParityVertex>> predecessorMap = LiftableFactory
                .getPredecessorMap(arena.getVertices());
        Map<ParityVertex, ParityVertex> compressedPredecessors = new HashMap<>();
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
        return transferSolutionBack(arenaToSolve,
                new ImmutableMultimap.Builder<ParityVertex, ParityVertex>()
                        .putAll(Multimaps.forMap(compressedPredecessors))
                        .build().inverse(), compressedSuccessors,
                otherVertices, strategy, winningRegionForA, actualStrategy,
                actualWinningRegionForA, actualWinningRegionForB);
    }

    protected static Solution transferSolutionBack(
            LinkedArena arenaToSolve,
            ImmutableMultimap<ParityVertex, ParityVertex> predecessorInverseMap,
            BiMap<ParityVertex, ParityVertex> compressedSuccessors,
            BiMap<ParityVertex, ParityVertex> otherVertices,
            final Map<ParityVertex, ParityVertex> strategy,
            final Set<ParityVertex> winningRegionForA,
            final Map<ParityVertex, ParityVertex> actualStrategy,
            final Set<ParityVertex> actualWinningRegionForA,
            final Set<ParityVertex> actualWinningRegionForB) {
        for (final ParityVertex vertex : arenaToSolve.getVertices()) {
            /*
             * TODO: reconstruct the strategy if (strategy.containsKey(vertex))
             * { actualStrategy.put( getVertexBack(vertex, compressedSuccessors,
             * otherVertices), getVertexBack(strategy.get(vertex),
             * predecessorInverseMap, otherVertices)); }
             */
            if (winningRegionForA.contains(vertex)) {
                actualWinningRegionForA.addAll(getVertexBack(vertex,
                        predecessorInverseMap, otherVertices));
                actualWinningRegionForA.add(getVertexBack(vertex,
                        compressedSuccessors, otherVertices));
            } else {
                actualWinningRegionForB.addAll(getVertexBack(vertex,
                        predecessorInverseMap, otherVertices));
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
            Map<ParityVertex, ParityVertex> compressedPredecessors,
            Map<ParityVertex, ParityVertex> compressedSuccessors,
            Map<ParityVertex, ParityVertex> otherVertices) {
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
            Map<ParityVertex, ParityVertex> compressedPredecessors,
            Map<ParityVertex, ParityVertex> compressedSuccessors) {
        Set<ParityVertex> inStructures = new HashSet<>();
        Map<ParityVertex, List<ParityVertex>> successorsInTunnel = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : arena.getVertices()) {
            if (inStructures.contains(vertex)) {
                continue;
            }
            if (vertex.getSuccessors().size() == 1) {
                // solitaire
                final ParityVertex successor = vertex.getSuccessors()
                        .iterator().next();
                if (predecessorMap.get(successor).size() == 1) {
                    List<ParityVertex> tail = getTail(predecessorMap,
                            successor, successorsInTunnel);
                    successorsInTunnel.put(vertex, tail);
                    inStructures.add(vertex);
                    inStructures.addAll(tail);
                } else {
                    // may still be a cycle
                    ParityVertex tempSuccessor = successor;
                    final List<ParityVertex> path = new ArrayList<ParityVertex>();
                    while (tempSuccessor.getSuccessors().size() == 1
                            && !path.contains(tempSuccessor)) {
                        path.add(tempSuccessor);
                        tempSuccessor = tempSuccessor.getSuccessors()
                                .iterator().next();
                    }
                    if (path.contains(tempSuccessor)) {
                        final int loopStartIndex = path
                                .lastIndexOf(tempSuccessor);
                        final List<ParityVertex> cycleVertices = path.subList(
                                loopStartIndex, path.size());

                        inStructures.addAll(cycleVertices);
                        mapCycle(arenaToSolve, compressedPredecessors,
                                cycleVertices);
                    }
                }
            }
        }
        for (final ParityVertex front : successorsInTunnel.keySet()) {
            List<ParityVertex> tail = successorsInTunnel.get(front);
            tail.add(0, front);
            mapTunnel(arenaToSolve, compressedPredecessors,
                    compressedSuccessors, tail);
        }
        for (final ParityVertex vertex : arena.getVertices()) {
            if (inStructures.contains(vertex)) {
                continue;
            }
            arenaToSolve.addVertex(vertex.getName(), vertex.getPriority(),
                    vertex.getPlayer());
        }
    }

    protected static List<ParityVertex> getTail(
            Map<ParityVertex, Set<ParityVertex>> predecessorMap,
            final ParityVertex successor,
            Map<ParityVertex, List<ParityVertex>> successorsInTunnel) {
        List<ParityVertex> tail = new ArrayList<>();
        tail.add(successor);
        ParityVertex tempSuccessor = successor;
        while (tempSuccessor.getSuccessors().size() == 1
                && !successorsInTunnel.containsKey(tempSuccessor)
                && !successor.equals(tempSuccessor)) {
            final ParityVertex tempSuccessor2 = tempSuccessor.getSuccessors()
                    .iterator().next();
            if (predecessorMap.get(tempSuccessor2).size() == 1) {
                tail.add(tempSuccessor2);
                tempSuccessor = tempSuccessor2;
            } else {
                break;
            }
        }
        if (successorsInTunnel.containsKey(tempSuccessor)) {
            tail.addAll(successorsInTunnel.get(tempSuccessor));
            successorsInTunnel.remove(tempSuccessor);
        }
        return tail;
    }

    protected static void mapCycle(final LinkedArena arenaToSolve,
            final Map<ParityVertex, ParityVertex> compressedPredecessors,
            final Collection<ParityVertex> cycle) {
        final String newVertexName = cycle.stream().map(ParityVertex::getName)
                .collect(Collectors.joining("+"));
        final int priority = cycle.stream().map(ParityVertex::getPriority)
                .mapToInt(x -> x).max().orElse(Integer.MIN_VALUE);
        arenaToSolve.addVertex(newVertexName, priority, Player.A);
        arenaToSolve.addEdge(newVertexName, newVertexName);
        ParityVertex compressedVertex = arenaToSolve.getVertex(newVertexName);
        for (final ParityVertex vertex : cycle) {
            compressedPredecessors.put(vertex, compressedVertex);
        }
    }

    private static void mapTunnel(LinkedArena arenaToSolve,
            Map<ParityVertex, ParityVertex> compressedPredecessors,
            Map<ParityVertex, ParityVertex> compressedSuccessors,
            List<ParityVertex> path) {
        final String newVertexName = path.stream().map(ParityVertex::getName)
                .collect(Collectors.joining("+"));
        final int priority = path.stream().map(ParityVertex::getPriority)
                .mapToInt(x -> x).max().orElse(Integer.MIN_VALUE);
        final ParityVertex lastVertex = path.get(path.size() - 1);
        final Player player = lastVertex.getPlayer();
        arenaToSolve.addVertex(newVertexName, priority, player);
        final ParityVertex compressedVertex = arenaToSolve
                .getVertex(newVertexName);
        for (final ParityVertex vertex : path) {
            if (vertex.equals(lastVertex)) {
                compressedSuccessors.put(vertex, compressedVertex);
            }
            compressedPredecessors.put(vertex, compressedVertex);
        }
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
        if (otherVertices.containsValue(vertex)) {
            return otherVertices.inverse().get(vertex);
        }
        return compressedVertices.inverse().get(vertex);
    }

    private static Collection<? extends ParityVertex> getVertexBack(
            ParityVertex vertex,
            ImmutableMultimap<ParityVertex, ParityVertex> predecessorInverseMap,
            BiMap<ParityVertex, ParityVertex> otherVertices) {
        if (otherVertices.containsValue(vertex)) {
            return new ImmutableSet.Builder<ParityVertex>().add(
                    otherVertices.inverse().get(vertex)).build();
        }
        return predecessorInverseMap.get(vertex);
    }
}
