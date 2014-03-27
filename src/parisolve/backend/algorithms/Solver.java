package parisolve.backend.algorithms;

import java.util.Collection;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * interface implemented by every algorithm for solving parity games.
 * 
 * @author Arne Schröder
 */
public interface Solver {
    /**
     * returns the vertices from which the player given can win.
     * 
     * @param arena
     *            the arena to play on
     * @param player
     *            the player for whom the winning region is to be determined
     * @return the winning region of the player given. That is, the vertices, on
     *         which they win.
     */
    Collection<? extends ParityVertex> getWinningRegionForPlayer(
            final Arena arena, final Player player);
}
