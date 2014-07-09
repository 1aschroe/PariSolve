package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

/**
 * Generator on the basis of the family H_{l,b}, defined in Jurdzinski (2000)
 * "Small Progress Measures for Solving Parity Games", Theorem 12
 * 
 * @author Arne Schröder
 */
public class HlbArenaGenerator {
    public static Arena generateHlbArena(final int l, final int b) {
        LinkedArena arena = new LinkedArena();
        addOddLevel(b, arena);
        for (int level = 1; level < l; level++) {
            addEvenLevel(b, level, arena);
        }
        return arena;
    }

    private static void addOddLevel(final int b, final LinkedArena arena) {
        arena.addVertex("s0", 0, Player.A);
        for (int i = 1; i <= b; i++) {
            arena.addVertex("con" + i, 1, Player.B);
            arena.addVertex("s" + i, 0, Player.A);

            arena.addEdge("con" + i, "s" + i);
            arena.addEdge("s" + i, "con" + i);

            arena.addEdge("con" + i, "s" + (i - 1));
            arena.addEdge("s" + (i - 1), "con" + i);
        }
    }

    private static void addEvenLevel(int b, int level, LinkedArena arena) {
        arena.addVertex(level + "s0", 2 * level, Player.B);
        for (int i = 1; i <= b; i++) {
            arena.addVertex(level + "con" + i, 2 * level, Player.A);
            arena.addVertex(level + "s" + i, 2 * level, Player.B);
            arena.addVertex(level + "t" + i, 2 * level + 1, Player.A);

            arena.addEdge(level + "con" + i, level + "s" + i);
            arena.addEdge(level + "s" + i, level + "con" + i);

            arena.addEdge(level + "s" + i, level + "t" + i);
            arena.addEdge(level + "t" + i, level + "con" + i);

            arena.addEdge(level + "con" + i, level + "s" + (i - 1));
            arena.addEdge(level + "s" + (i - 1), level + "con" + i);

            arena.addEdge(level + "con" + i, "con" + i);
            arena.addEdge("con" + i, level + "con" + i);
        }
    }
}
