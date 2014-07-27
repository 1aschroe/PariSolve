package parisolve.backend.algorithms;

import parisolve.backend.Arena;
import parisolve.backend.algorithms.helper.Solution;
import parisolve.backend.algorithms.helper.SolutionWithTime;

/**
 * interface implemented by every algorithm for solving parity games.
 * 
 * @author Arne Schröder
 */
public interface Solver {
    /**
     * returns the vertices from which the player given can win. Should only be
     * accessed if the running time is irrelevant.
     * 
     * @param arena
     *            the arena to solve
     * @return the solution of the arena. That is, the winning sets for both
     *         players, and, if possible, the winning strategies for these.
     */
    Solution getSolution(final Arena arena);

    /**
     * solves the arena given and measures the time it took to do so
     * 
     * @param arena
     *            the arena to solve
     * @return the solution to the arena given and the measured time to
     *         calculate it
     */
    default SolutionWithTime solveAndTime(final Arena arena) {
        // the garbage collector does introduce a lot of 'noise'.
        // Therefore, always running the garbage collector prior to
        // measurement should make these more reliable.
        Runtime.getRuntime().gc();
        final long start = System.currentTimeMillis();
        Solution solution = getSolution(arena);
        final long stop = System.currentTimeMillis();
        return new SolutionWithTime(solution, stop - start);
    }
}
