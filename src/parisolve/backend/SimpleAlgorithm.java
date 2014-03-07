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

	@Override
	public Collection<? extends ParityVertex> getWinningRegionForPlayer(
			Arena arena, int player) {
		Map<ParityVertex, Long> nuForLastK = new HashMap<>();
		Map<ParityVertex, ParityVertex> strategies = new HashMap<>();
		Collection<? extends ParityVertex> vertices = arena.getVertices();
		int n = vertices.size();
		long maxK = calculateMaxK(vertices);

		for (long k = 0; k <= maxK; k++) {
			Map<ParityVertex, Long> nu = new HashMap<>();
			Map<ParityVertex, ParityVertex> newStrategies = new HashMap<>();
			
			for (ParityVertex v : vertices) {
				Collection<? extends ParityVertex> successors = arena
						.getSuccessors(v);
				int vsPlayer = v.getPlayer();
				ParityVertex bestSuc = null;
				for (ParityVertex suc : successors) {
					if (bestSuc == null) {
						bestSuc = suc;
					} else {
						long sucValue = getValue(v, suc, nuForLastK, n);
						long bestValue = getValue(v, bestSuc, nuForLastK, n);
						if ((vsPlayer == 0 && (sucValue > bestValue)) || (vsPlayer == 1 && (sucValue < bestValue))) {
							bestSuc = suc;
						}
					}
				}
				nu.put(v, getValue(v, bestSuc, nuForLastK, n));
				newStrategies.put(v, bestSuc);
			}
			nuForLastK = nu;
			strategies = newStrategies;
		}
		
		Set<ParityVertex> winningRegion = new HashSet<>();
		for (ParityVertex v : vertices) {
			if (Math.pow(-1, player) * nuForLastK.get(v) > 0) {
				winningRegion.add(v);
			}
		}
		
		return winningRegion;
	}

	private long calculateMaxK(Collection<? extends ParityVertex> vertices) {
		int n = vertices.size();
		int maxParity = Integer.MIN_VALUE;
		for (ParityVertex vertex : vertices) {
			if (vertex.getParity() > maxParity) {
				maxParity = vertex.getParity();
			}
		}
		return (long) (n * n * Math.pow(n, maxParity));
	}

	public static long getValue(ParityVertex v, ParityVertex u,
			Map<ParityVertex, Long> nuForLastK, int n) {
		return getValueFromParity(v.getParity(), n) + (nuForLastK.containsKey(u) ? nuForLastK.get(u) : 0 );
	}

	public static long getValueFromParity(int i, int n) {
		// TODO: optimize: Memoize this function
		return (long) ((i % 2 == 0) ? Math.pow(n, i) : -Math.pow(n, i));
	}
}
