package parisolve.io;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.Player;

/**
 * Family of arenas defined on p. 6 in "Recursive Algorithm for Parity Games
 * requires Exponential Time" by Friedmann (2011)
 * 
 * @author Arne Schröder
 */
public class HardArenaGenerator implements LinearArenaGenerator {

    @Override
    public Arena generateArena(int n) {
        LinkedArena arena = new LinkedArena();

        for (int i = 1; i <= n; i++) {
            arena.addVertex("a" + i, 1 - (i % 2), Player
                    .getPlayerForPriority(i).getOponent());
            arena.addVertex("b" + i, 1 - (i % 2),
                    Player.getPlayerForPriority(i));
        }
        for (int i = 0; i < n; i++) {
            arena.addVertex("c" + i, 3 * i + 5, Player.getPlayerForPriority(i)
                    .getOponent());
            arena.addVertex("d" + i, 3 * i + 4, Player.getPlayerForPriority(i));
            arena.addVertex("e" + i, 3 * i + 3, Player.getPlayerForPriority(i)
                    .getOponent());
        }

        for (int i = 1; i <= n; i++) {
            arena.addEdge("a" + i, "b" + i);
            arena.addEdge("a" + i, "d" + (i - 1));

            arena.addEdge("b" + i, "a" + i);
            if (i < n) {
                arena.addEdge("b" + i, "c" + i);
            }
        }
        for (int i = 0; i < n; i++) {
            arena.addEdge("c" + i, "b" + (i + 1));
            arena.addEdge("c" + i, "d" + i);

            arena.addEdge("d" + i, "e" + i);
            if (i > 0) {
                arena.addEdge("d" + i, "d" + (i - 1));
            }
            if (i < n - 1) {
                arena.addEdge("d" + i, "d" + (i + 1));
            }

            arena.addEdge("e" + i, "b" + (i + 1));
            arena.addEdge("e" + i, "d" + i);
        }

        return arena;
    }

}
