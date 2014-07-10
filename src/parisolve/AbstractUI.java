package parisolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;
import parisolve.io.HlbArenaGenerator;
import parisolve.io.SolitaireArenaGenerator;
import parisolve.io.WeakArenaGenerator;

public abstract class AbstractUI implements UserInterface {
    /**
     * listeners to inform, once the user has taken an action
     */
    private final List<UserListener> userListeners = new ArrayList<UserListener>();

    /**
     * informs the listeners, that the user requests the current arena to be
     * solved using the algorithm instance given.
     * 
     * A current arena must have been loaded beforehand.
     * 
     * @param solver
     *            the algorithm to use for solving the arena
     */
    protected final void fireSolve(final Solver solver) {
        for (final UserListener listener : userListeners) {
            listener.solve(solver);
        }
    }

    /**
     * informs the listeners, that the user requests the current arena to be
     * saved under the path specified.
     * 
     * This requires an arena to have been loaded beforehand.
     * 
     * @param path
     *            the path to save the arena by
     */
    protected final void fireSave(final String path) {
        try {
            for (final UserListener listener : userListeners) {
                listener.save(path);
            }
        } catch (IOException e) {
            displayError("While saving the arena, the following exception occurred:\n"
                    + e.getClass().getSimpleName()
                    + ": "
                    + e.getMessage()
                    + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    protected final void fireOpened(final Arena arena) {
        for (final UserListener listener : userListeners) {
            listener.openedArena(arena);
        }
    }

    @Override
    public final void addUserListener(final UserListener openListener) {
        if (!userListeners.contains(openListener)) {
            userListeners.add(openListener);
        }
    }

    /**
     * randomly generates and loads an arena according to the preferences set
     * and informs the open-listeners that this has happened.
     * 
     * @param numberOfVertices
     *            the number of vertices the generated arena will have
     * @param averageDegree
     *            approximate value of what the average degree should be like
     *            (this cannot be exact, as the degrees are chosen randomly)
     * @param maxPriority
     *            the maximal priority in the graph
     */
    protected final void generateRandomArena(final int numberOfVertices,
            final double averageDegree, final int maxPriority) {
        final Arena arena = ArenaManager.generateRandomArena(numberOfVertices,
                averageDegree, maxPriority);
        fireOpened(arena);
    }

    /**
     * generates an arena as specified by Jurdzinski (2000) Theorem 12.
     */
    protected final void generateHlbArena(final int l, final int b) {
        final Arena arena = HlbArenaGenerator.generateHlbArena(l, b);
        fireOpened(arena);
    }

    protected final void generateWeakArena(final int n) {
        final Arena arena = WeakArenaGenerator.generateWeakArena(n);
        fireOpened(arena);
    }

    protected final void generateSolitaireArena(final int n) {
        final Arena arena = SolitaireArenaGenerator.generateSolitaireArena(n);
        fireOpened(arena);
    }

    /**
     * load the arena stored in the file specified. Both the GraphViz/DOT-format
     * and the txt-format are supported. If the files extension is '.arena' the
     * first is tried.
     * 
     * @param filename
     *            the arena-file's filename
     */
    protected final void loadArenaFromFile(final String filename) {
        try {
            final Arena arena = ArenaManager.loadArena(filename);
            fireOpened(arena);
        } catch (IOException e) {
            displayError("While loading the arena, the following exception occurred:\n"
                    + e.getMessage()
                    + "\n"
                    + Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            displayError("Unexpected exception occurred: " + e);
        }
    }

    @Override
    public void highlightSolution(
            final Collection<? extends ParityVertex> winningRegion,
            final Map<ParityVertex, ParityVertex> strategy) {
        // left empty to not have to implement this
    }

    @Override
    public void populateGraphWithArena(final Arena currentArena) {
        // left empty to not have to implement this
    }
}
