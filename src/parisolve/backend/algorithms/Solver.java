package parisolve.backend.algorithms;

import java.util.Collection;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;

public interface Solver {
	Collection<? extends ParityVertex> getWinningRegionForPlayer(Arena arena, int player);
}
