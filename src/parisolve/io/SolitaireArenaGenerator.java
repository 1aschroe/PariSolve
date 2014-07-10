package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

public class SolitaireArenaGenerator implements LinearArenaGenerator {
    public Arena generateArena(final int n) {
        LinkedArena arena = new LinkedArena();
        // in the definition of the family this is player As vertex, but that
        // does not change the solitaire-property.
        arena.addVertex("v0", 2, Player.B);
        arena.addEdge("v0", "v0");

        for (int i = 1; i <= 2 * n; i++) {
            arena.addVertex("v" + i, 2 + i, Player.B);
            arena.addEdge("v" + i, "v" + (i - 1));
        }

        for (int j = 1; j <= n; j++) {
            arena.addVertex("u" + j, 1, Player.A);
            arena.addEdge("u" + j, "u" + j);
            arena.addEdge("u" + j, "v" + (2 * j - 1));
        }

        return arena;
    }
}
