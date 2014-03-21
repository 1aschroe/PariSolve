package parisolve;

import java.util.Collection;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.PrimitiveAlgorithm;
import parisolve.backend.algorithms.Solver;

public class StartUp {

    static Arena currentArena;

    public static void main(String[] args) {
        final UI ui = new UI();
        ui.addOpenListener(new OpenListener() {
            @Override
            public void openedArena(Arena arena) {
                currentArena = arena;
                ui.populateGraphWithArena(currentArena);
            }
        });
        ui.addSolveListener(new SolveListener() {
            @Override
            public void solve() {
                Solver solver = new PrimitiveAlgorithm();
                Collection<? extends ParityVertex> winningRegionForPlayer = solver.getWinningRegionForPlayer(currentArena, 0);
                ui.highlightWinningRegion(winningRegionForPlayer);
            }
        });
        ui.run();
    }

}
