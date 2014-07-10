package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

/**
 * Generator on the basis of the family W_n, defined on page 14 of Gazda,
 * Willemse (2013)
 * "Zielonka’s Recursive Algorithm: dull, weak and solitaire games and tighter bounds"
 * 
 * @author Arne Schröder
 */
public class WeakArenaGenerator implements LinearArenaGenerator {

    /**
     * 
     * @param n
     *            parameter for the arena. Has to be at least 1.
     * @return
     */
    public Arena generateArena(final int n) {
        LinkedArena arena = new LinkedArena();
        arena.addVertex("u0", 0, Player.A);
        arena.addEdge("u0", "u0");
        arena.addVertex("u1", 1, Player.B);
        arena.addEdge("u1", "u1");

        arena.addVertex("v1", 3, Player.A);
        arena.addEdge("v1", "u0");
        arena.addVertex("v" + (n + 1), 3, Player.B);
        arena.addEdge("v" + (n + 1), "u1");

        arena.addEdge("v1", "v" + (n + 1));
        arena.addEdge("v" + (n + 1), "v1");
        for (int i = 2; i <= n; i++) {
            arena.addVertex("v" + i, i + 2, Player.A);
            arena.addEdge("v" + i, "v" + (i - 1));
            arena.addVertex("v" + (n + i), i + 2, Player.B);
            arena.addEdge("v" + (n + i), "v" + (n + i - 1));

            arena.addEdge("v" + i, "v" + (n + i));
            arena.addEdge("v" + (n + i), "v" + i);
        }
        return arena;
    }
}
