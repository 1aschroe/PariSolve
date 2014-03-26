package parisolve;

import parisolve.backend.algorithms.Solver;

/**
 * to be implemented to be informed when a solve-action is requested.
 */
public interface SolveListener {

    /**
     * informs that the arena is supposed to be solved.
     * @param solver the algorithm to solve the arena with
     */
    void solve(final Solver solver);
}
