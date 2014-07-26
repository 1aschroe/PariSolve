package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

/**
 * selfdefined generator with fixed maximal priority (P=5) and rapidly growing (x^3?)
 * solving time for BetterAlgorithm.
 * 
 * @author Arne Schröder
 *
 */
public class ChainArenaGenerator implements LinearArenaGenerator {

    @Override
    public Arena generateArena(int n) {
        LinkedArena arena = new LinkedArena();
        for (int i = 0; i < n; i++) {
            arena.addVertex("vA1#" + i, 1, Player.A);
            arena.addVertex("vA2#" + i, 2, Player.A);
            arena.addVertex("vA4#" + i, 4, Player.A);
            arena.addVertex("vB2#" + i, 2, Player.B);
            arena.addVertex("vB3#" + i, 3, Player.B);
            arena.addVertex("vB5#" + i, 5, Player.A);

            arena.addEdge("vA4#" + i, "vA1#" + i);

            arena.addEdge("vA1#" + i, "vA2#" + i);
            arena.addEdge("vA2#" + i, "vA1#" + i);

            arena.addEdge("vB5#" + i, "vB2#" + i);

            arena.addEdge("vB2#" + i, "vB3#" + i);
            arena.addEdge("vB3#" + i, "vB2#" + i);

            arena.addEdge("vA2#" + i, "vB3#" + i);
            arena.addEdge("vB2#" + i, "vA1#" + i);

            if (i > 0) {
                arena.addEdge("vB3#" + (i - 1), "vA2#" + i);
                arena.addEdge("vA1#" + i, "vB2#" + (i - 1));
            }
        }

        return arena;
    }

}
