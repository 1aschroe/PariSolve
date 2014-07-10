package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

public class ResilientArenaGenerator implements LinearArenaGenerator {
    public Arena generateArena(final int n) {
        LinkedArena arena = new LinkedArena();
        for (int i = 1; i <= n; i++) {
            arena.addVertex("v" + i, i + 1, Player.getPlayerForPriority(i + 1));
            arena.addVertex("u" + i, i % 2, Player.getPlayerForPriority(i + 1));
            arena.addVertex("w" + i, i % 2, Player.getPlayerForPriority(i));
        }

        arena.addEdge("v" + n, "u" + n);
        arena.addEdge("u" + n, "w" + n);
        arena.addEdge("w" + n, "u" + n);
        for (int i = 1; i < n; i++) {
            arena.addEdge("v" + i, "u" + i);
            arena.addEdge("v" + i, "v" + (i + 1));

            arena.addEdge("u" + i, "w" + i);
            arena.addEdge("u" + i, "v" + (i + 1));

            arena.addEdge("w" + i, "u" + i);
            arena.addEdge("w" + (i + 1), "w" + i);
        }

        return arena;
    }
}
