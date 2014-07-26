package parisolve;

import java.io.IOException;
import java.util.Collection;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.SolutionWithTime;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

/**
 * defines how PariSolve interacts with the user.
 * 
 * @author Arne Schröder
 *
 */
public final class InteractionListener implements UserListener {
    /**
     * the arena currently open (loaded from file or generated) to be worked
     * with in the next steps (to be solved or saved).
     */
    static Arena currentArena;
    /**
     * the winning region of A in <code>currentArena</code> or null if A's
     * winning region has not been calculated. Used to determine if two
     * consecutive solves of one arena solve it the same way.
     */
    static Collection<? extends ParityVertex> currentWinningRegion = null;

    /**
     * whether to display information how long solving took
     */
    private final boolean displayTimeToSolve;
    // TOOD: should this instance know the ui? or should the ui rather hand
    // a backpointer to itself or the methods return values?
    private final UserInterface ui;

    InteractionListener(boolean doTime, UserInterface ui) {
        this.displayTimeToSolve = doTime;
        this.ui = ui;
    }

    @Override
    public void openedArena(final Arena arena) {
        currentArena = arena;
        currentWinningRegion = null;
        ui.populateGraphWithArena(currentArena);
    }

    @Override
    public void save(final String path) throws IOException {
        ArenaManager.saveArena(currentArena, path);
    }

    @Override
    public void solve(final Solver solver) {
        if (currentArena == null) {
            ui.displayError("No arena loaded");
            return;
        }
        SolutionWithTime result = solver.solveAndTime(currentArena);
        Collection<ParityVertex> winningRegion = result.getSolution()
                .getWinningRegionFor(Player.A);
        if (currentWinningRegion == null) {
            currentWinningRegion = winningRegion;
        } else {
            if (!currentWinningRegion.equals(winningRegion)) {
                ui.displayError("Different algorithms did not solve the arena the same.");
                try {
                    ArenaManager.saveArena(currentArena, "error.txt");
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        ui.highlightSolution(winningRegion, result.getSolution().getStrategy());
        if (displayTimeToSolve) {
            ui.displayInfo(String.format(UserInterface.TIME_MSG, solver
                    .getClass().getSimpleName(), result.getTime()));
        }
    }
}