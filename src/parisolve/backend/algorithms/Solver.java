package parisolve.backend.algorithms;

import parisolve.backend.Arena;

/**
 * interface implemented by every algorithm for solving parity games.
 * 
 * @author Arne Schröder
 */
public interface Solver {
    /**
     * returns the vertices from which the player given can win.
     * 
     * @param arena
     *            the arena to play on
     * @param player
     *            the player for whom the winning region is to be determined
     * @return the winning region of the player given. That is, the vertices, on
     *         which they win.
     */
    Solution getSolution(
            final Arena arena);
}
