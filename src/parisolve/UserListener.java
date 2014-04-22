package parisolve;

import java.io.IOException;

import parisolve.backend.Arena;
import parisolve.backend.algorithms.Solver;

/**
 * to be implemented to be informed when a user-action takes place.
 */
public interface UserListener {

    /**
     * informs that an arena has been opened.
     * 
     * @param arena
     *            the opened arena
     */
    void openedArena(Arena arena);

    /**
     * informs that the user requests the current arena to be saved.
     * 
     * @param path
     *            the path to save the arena by
     * @throws IOException 
     */
    void save(String path) throws IOException;

    /**
     * informs that the user requests the current arena to be solved.
     * 
     * @param solver
     *            the algorithm to solve the arena with
     */
    void solve(final Solver solver);

}
