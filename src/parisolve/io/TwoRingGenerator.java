package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

/**
 * own idea for a graph, inspired by the title page of
 * "Practical Improvements to Parity Game Solving".
 * 
 * @author Arne Schröder
 */
public class TwoRingGenerator implements LinearArenaGenerator {

    @Override
    public Arena generateArena(int n) {
        LinkedArena arena = new LinkedArena();
        for (int i = 0; i < n; i++) {
            arena.addVertex("inner" + i, 2 * i, Player.B);
            arena.addVertex("middle" + i, 2 * i + 1, Player.A);
            arena.addVertex("outerA" + i, i, Player.A);
            arena.addVertex("outerB" + i, i, Player.B);

            arena.addEdge("outerA" + i, "outerB" + i);
            arena.addEdge("outerB" + i, "outerA" + i);
        }

        for (int i = 0; i < n; i++) {
            arena.addEdge("inner" + i, "inner" + ((i + 1) % n));
            arena.addEdge("inner" + i, "middle" + i);

            arena.addEdge("middle" + i, "inner" + ((i + 1) % n));
            arena.addEdge("middle" + i, "outerB" + i);
            arena.addEdge("outerA" + i, "middle" + ((i - 1 + n) % n));
        }
        return arena;
    }

}
