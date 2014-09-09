package parisolve.backend.algorithms.preprocessor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.Player;
import parisolve.backend.algorithms.Solver;
import parisolve.backend.algorithms.helper.Solution;

public class GraphPreprocessor implements Solver {
    public enum Optimization {
        VERTEX_COMPRESSION, SELFCYCLE_REMOVAL /* , SCC_DECOMPOSITION */;
    }

    private Solver actualSolver;
    private Set<Optimization> optimizationsToApply = new HashSet<>();

    public GraphPreprocessor(final Solver solver) {
        this.actualSolver = solver;
    }

    public void addOptimization(Optimization optimization) {
        optimizationsToApply.add(optimization);
    }

    @Override
    public Solution getSolution(Arena arena) {
        return solveCompressedArena(arena);
    }

    int arenaSize = Integer.MIN_VALUE;

    protected Solution solveCompressedArena(Arena arena) {
        arenaSize = arena.size();
        if (optimizationsToApply.contains(Optimization.VERTEX_COMPRESSION)) {
            return VertexCompressor.solveCompressedArena(arena, this);
        } else {
            return this.solveSelfcyclelessArena(arena);
        }
    }

    protected Solution solveSelfcyclelessArena(Arena arena) {
        if (optimizationsToApply.contains(Optimization.SELFCYCLE_REMOVAL)) {
            return SelfcycleRemover.solveSelfcyclelessArena(arena, this);
        } else {
            return solve(arena);
        }
    }

    public Solution solve(Arena arena) {
        if (arena.isEmpty()) {
            return new Solution(Collections.emptySet(), Player.A);
        }
        if (arena.size() < arenaSize) {
            return solveCompressedArena(arena);
        }
        return actualSolver.getSolution(arena);
    }
}
