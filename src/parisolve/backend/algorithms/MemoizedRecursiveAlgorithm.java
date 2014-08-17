package parisolve.backend.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.helper.Solution;

public class MemoizedRecursiveAlgorithm extends RecursiveAlgorithm {

    final Map<Set<ParityVertex>, Solution> solutionMemoization = new HashMap<>();

    @Override
    protected Solution solveGame(Set<ParityVertex> vertices) {
        if (solutionMemoization.containsKey(vertices)) {
            return solutionMemoization.get(vertices);
        } else {
            Solution solution = super.solveGame(vertices);
            solutionMemoization.put(vertices, solution);
            return solution;
        }
    }
}
