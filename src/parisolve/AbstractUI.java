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
    protected List<SolveListener> solveListeners = new ArrayList<SolveListener>();
    protected List<OpenListener> openListeners = new ArrayList<OpenListener>();

    @Override
    public void addSolveListener(final SolveListener solveListener) {
        if (!solveListeners.contains(solveListener)) {
            solveListeners.add(solveListener);
        }
    }

    protected void fireSolve(Solver solver) {
        for (final SolveListener listener : solveListeners) {
            listener.solve(solver);
        }
    }

    @Override
    public void addOpenListener(final OpenListener openListener) {
        if (!openListeners.contains(openListener)) {
            openListeners.add(openListener);
        }
    }

    protected void loadArenaFromFile(final String filename) {
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
    public void highlightRegion(Collection<? extends ParityVertex> winningRegion) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void populateGraphWithArena(Arena currentArena) {
        // TODO Auto-generated method stub
        
    }

}
