package parisolve.backend;

import java.util.Collection;

public interface Solver {
	Collection<? extends ParityVertex> getWinningRegionForPlayer(Arena arena, int player);
}
