package parisolve;

import java.util.Collection;
import java.util.Map;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;

/**
 * Interface for the interaction between the user and the program.
 * 
 * It can be used to communicate to the user via info- and error-messages as
 * well as via displaying information about the data being acted upon. For
 * receiving communication from the user listeners can be installed.
 * 
 * @author Arne Schröder
 */
public interface UserInterface {
    /**
     * displays general information to the user.
     * 
     * @param message
     *            text to display to the user
     */
    void displayInfo(final String message);

    /**
     * displays an error message to the user.
     * 
     * @param message
     *            text to display to the user
     */
    void displayError(final String message);

    /**
     * adds a listener to act upon the user's requests.
     * 
     * @param listener
     *            listener to add
     */
    void addUserListener(final UserListener listener);

    /**
     * requests the <code>UserInterface</code> to run in dispatch-mode, waiting
     * for the user to act and calling the listeners accordingly.
     * 
     * This method is thread-blocking and will only return once the user
     * requests to end the program. Therefore, every setup has to be completed
     * beforehand.
     */
    void run();

    /**
     * tells the <code>UserInterface</code> to highlight the winning regions of
     * player A and B and their respective strategies.
     * 
     * @param winningRegionForA
     *            winning region of player A
     * @param strategy
     *            strategies of both players which lead to this partition
     */
    void highlightSolution(
            final Collection<? extends ParityVertex> winningRegionForA,
            final Map<ParityVertex, ParityVertex> strategy);

    /**
     * tells the <code>UserInterface</code> that the arena specified is now the
     * current arena to work upon and it should be displayed.
     * 
     * @param currentArena
     *            the arena to display
     */
    void populateGraphWithArena(final Arena currentArena);

    /**
     * tells the <code>UserInterface</code> to handle the argument which where
     * given to the program by the user. Usually these are arenas to load (and
     * solve).
     * 
     * @param arenas
     *            the user's arguments
     */
    void handleArguments(final String[] arenas);
}
