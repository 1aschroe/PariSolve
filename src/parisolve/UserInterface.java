package parisolve;

import java.util.Collection;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;

public interface UserInterface {

    void displayInfo(final String message);

    void displayError(final String message);

    void addOpenListener(final OpenListener listener);

    void addSolveListener(final SolveListener listener);
    
    void run();

    void highlightRegion(Collection<? extends ParityVertex> winningRegion);

    void populateGraphWithArena(Arena currentArena);
}
