package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

/**
 * self-invented generator with fixed maximal priority (P=5) and rapidly growing
 * (x^3?) solving time for <code>SmallMeasureAlgorithm</code>.
 * 
 * @author Arne Schröder
 *
 */
public class ChainArenaGenerator implements LinearArenaGenerator {

    @Override
    public Arena generateArena(int n) {
        LinkedArena arena = new LinkedArena();
        for (int i = 1; i <= n; i++) {
            arena.addVertex("a" + i, 1, Player.A);
            arena.addVertex("a" + (n + i), 2, Player.A);
            arena.addVertex("a" + (2 * n + i), 4, Player.A);
            arena.addVertex("b" + i, 2, Player.B);
            arena.addVertex("b" + (n + i), 3, Player.B);
            arena.addVertex("b" + (2 * n + i), 5, Player.B);

            arena.addEdge("a" + (2 * n + i), "a" + i);

            arena.addEdge("a" + i, "a" + (n + i));
            arena.addEdge("a" + (n + i), "a" + i);

            arena.addEdge("b" + (2 * n + i), "b" + i);

            arena.addEdge("b" + i, "b" + (n + i));
            arena.addEdge("b" + (n + i), "b" + i);

            arena.addEdge("a" + (n + i), "b" + (n + i));
            arena.addEdge("b" + i, "a" + i);

            if (i > 1) {
                arena.addEdge("b" + (n + i), "a" + (n + i - 1));
                arena.addEdge("a" + (i - 1), "b" + i);
            }
        }

        return arena;
    }

}
