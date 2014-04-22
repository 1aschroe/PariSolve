package parisolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

public abstract class AbstractUI implements UserInterface {
    /**
     * listeners to inform, when the user requests the current arena to be
     * solved
     */
    private final List<SolveListener> solveListeners = new ArrayList<SolveListener>();
    /**
     * listeners to inform, once a arena has been loaded.
     */
    private final List<OpenListener> openListeners = new ArrayList<OpenListener>();

    @Override
    public final void addSolveListener(final SolveListener solveListener) {
        if (!solveListeners.contains(solveListener)) {
            solveListeners.add(solveListener);
        }
    }

    /**
     * informs the listeners, that the user requests the current arena to be
     * solved using the algorithm instance given.
     * 
     * The current arena must be loaded beforehand.
     * 
     * @param solver
     *            the algorithm to use for solving the arena
     */
    protected final void fireSolve(final Solver solver) {
        for (final SolveListener listener : solveListeners) {
            listener.solve(solver);
        }
    }

    @Override
    public final void addOpenListener(final OpenListener openListener) {
        if (!openListeners.contains(openListener)) {
            openListeners.add(openListener);
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
    protected final void generateArena(final int numberOfVertices,
            final double averageDegree, final int maxPriority) {
        final Arena arena = ArenaManager.generateRandomArena(numberOfVertices,
                averageDegree, maxPriority);
        for (final OpenListener listener : openListeners) {
            listener.openedArena(arena);
        }
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
            for (final OpenListener listener : openListeners) {
                listener.openedArena(arena);
            }
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
    public void highlightRegion(
            final Collection<? extends ParityVertex> winningRegion) {
        // left empty to not have to implement this
    }

    @Override
    public void populateGraphWithArena(final Arena currentArena) {
        // left empty to not have to implement this
    }

}
