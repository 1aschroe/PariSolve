package parisolve.backend.algorithms.preprocessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.AttractiveBetterAlgorithm;
import parisolve.backend.algorithms.RecursiveAlgorithm.AttractorStrategyPair;
import parisolve.backend.algorithms.helper.LiftableFactory;
import parisolve.backend.algorithms.helper.Solution;

import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class SelfcycleRemover {

    private SelfcycleRemover() {
        // disable
    }

    public static Solution solveSelfcyclelessArena(final Arena arena,
            final GraphPreprocessor preprocessor) {
        Set<ParityVertex> winningForA = new HashSet<>();
        Set<ParityVertex> winningForB = new HashSet<>();
        Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();

        solveSelfcycles(arena, winningForA, winningForB, strategy);

        BiMap<ParityVertex, ParityVertex> vertexCorrespondence = HashBiMap
                .create();

        LinkedArena arenaToSolve = transferRestOfArena(arena, winningForA,
                winningForB, vertexCorrespondence);
        Solution solution = preprocessor.solve(arenaToSolve);
        winningForA.addAll(getAll(solution.getWinningRegionFor(Player.A),
                vertexCorrespondence.inverse()));
        winningForB.addAll(getAll(solution.getWinningRegionFor(Player.B),
                vertexCorrespondence.inverse()));
        strategy.putAll(getAll(solution.getStrategy(),
                vertexCorrespondence.inverse()));
        return new Solution(winningForA, winningForB, Player.A, strategy);
    }

    protected static Collection<ParityVertex> getAll(
            final Collection<ParityVertex> keys,
            final Map<ParityVertex, ParityVertex> map) {
        return Maps.filterKeys(map, Predicates.in(keys)).values();
    }

    protected static Map<ParityVertex, ParityVertex> getAll(
            Map<ParityVertex, ParityVertex> mapToMap,
            Map<ParityVertex, ParityVertex> correspondence) {
        Map<ParityVertex, ParityVertex> map = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : mapToMap.keySet()) {
            map.put(correspondence.get(vertex),
                    correspondence.get(mapToMap.get(vertex)));
        }
        return map;
    }

    protected static LinkedArena transferRestOfArena(final Arena arena,
            Set<ParityVertex> winningForA, Set<ParityVertex> winningForB,
            BiMap<ParityVertex, ParityVertex> vertexCorrespondence) {
        LinkedArena arenaToSolve = new LinkedArena();
        for (final ParityVertex vertex : arena.getVertices()) {
            if (winningForA.contains(vertex) || winningForB.contains(vertex)) {
                continue;
            }
            arenaToSolve.addVertex(vertex.getName(), vertex.getPriority(),
                    vertex.getPlayer());
            ParityVertex newVertex = arenaToSolve.getVertex(vertex.getName());
            vertexCorrespondence.put(vertex, newVertex);
        }

        for (final ParityVertex vertex : arena.getVertices()) {
            if (winningForA.contains(vertex) || winningForB.contains(vertex)) {
                continue;
            }
            for (final ParityVertex successor : vertex.getSuccessors()) {
                if (!successor.equals(vertex) && vertexCorrespondence.containsKey(successor)) {
                    arenaToSolve.addEdge(vertex.getName(), successor.getName());
                }
            }
        }
        return arenaToSolve;
    }

    protected static void solveSelfcycles(final Arena arena,
            Set<ParityVertex> winningForA, Set<ParityVertex> winningForB,
            Map<ParityVertex, ParityVertex> strategy) {
        LiftableFactory liftableFactory = new LiftableFactory(
                arena.getVertices());

        for (final ParityVertex vertex : arena.getVertices()) {
            if (winningForA.contains(vertex) || winningForB.contains(vertex)) {
                continue;
            }
            if (vertex.getSuccessors().contains(vertex)) {
                // selfcycle
                final Player vertexPlayer = vertex.getPlayer();
                if (Player.getPlayerForPriority(vertex.getPriority()) == vertexPlayer) {
                    // this is a winning vertex for this player
                    AttractorStrategyPair attractor = AttractiveBetterAlgorithm
                            .getAttractor(
                                    new ImmutableSet.Builder<ParityVertex>()
                                            .add(vertex).build(), vertexPlayer,
                                    arena.getVertices(), liftableFactory);
                    strategy.putAll(attractor.getStrategy());
                    if (vertexPlayer == Player.A) {
                        winningForA.addAll(attractor.getAttractor());
                    } else {
                        winningForB.addAll(attractor.getAttractor());
                    }
                }
            }
        }
    }
}
