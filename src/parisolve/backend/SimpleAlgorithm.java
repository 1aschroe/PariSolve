package parisolve.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Idea given in theorem 7.6 LNCS 2500 p 113.
 * 
 * Solution via dynamic programming
 */
public class SimpleAlgorithm implements Solver {

	private static final int maxK = 20;

	@Override
	public Collection<? extends ParityVertex> getWinningRegionForPlayer(
			Arena arena, int player) {
		Map<ParityVertex, Integer> nuForLastK = new HashMap<>();
		Collection<? extends ParityVertex> vertices = arena.getVertices();
		int n = vertices.size();

		for (int k = 0; k <= maxK; k++) {
			Map<ParityVertex, Integer> nu = new HashMap<>();
			for (ParityVertex v : vertices) {
				Collection<? extends ParityVertex> successors = arena
						.getSuccessors(v);
				int vsPlayer = v.getPlayer();
				ParityVertex bestSuc = null;
				for (ParityVertex suc : successors) {
					if (bestSuc == null) {
						bestSuc = suc;
					} else {
						int sucValue = getValue(v, suc, nuForLastK, n);
						int bestValue = getValue(v, bestSuc, nuForLastK, n);
						if ((vsPlayer == 0 && (sucValue > bestValue)) || (vsPlayer == 1 && (sucValue < bestValue))) {
							bestSuc = suc;
						}
					}
				}
				nu.put(v, getValue(v, bestSuc, nuForLastK, n));
			}
			nuForLastK = nu;
		}
		
		Set<ParityVertex> winningRegion = new HashSet<>();
		for (ParityVertex v : vertices) {
			if (Math.pow(-1, player) * nuForLastK.get(v) > 0) {
				winningRegion.add(v);
			}
		}
		
		return winningRegion;
	}

	public static int getValue(ParityVertex v, ParityVertex u,
			Map<ParityVertex, Integer> nuForLastK, int n) {
		int i = v.getParity();
		// TODO: optimize: Memoize this function
		return (int) ((i % 2 == 0) ? Math.pow(n, i) : -Math.pow(n, i))
				+ (nuForLastK.containsKey(u) ? nuForLastK.get(u) : 0);
	}

}
