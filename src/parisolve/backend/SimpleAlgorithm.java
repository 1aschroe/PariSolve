package parisolve.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
		Collection<? extends ParityVertex> vertices = arena.getVertices();
		int n = vertices.size();
		long maxK = calculateMaxK(vertices);

		for (long k = 0; k <= maxK; k++) {
			Map<ParityVertex, Long> nu = new HashMap<>();
			
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

	private long calculateMaxK(Collection<? extends ParityVertex> vertices) {
		int n = vertices.size();
		int maxPriority = Integer.MIN_VALUE;
		for (ParityVertex vertex : vertices) {
			if (vertex.getPriority() > maxPriority) {
				maxPriority = vertex.getPriority();
			}
		}
		return (long) (n * n * Math.pow(n, maxPriority));
	}

    public static long getValue(ParityVertex v, ParityVertex u,
            Map<ParityVertex, Long> nuForLastK, int n) {
        return getValueFromPriority(v.getPriority(), n) + nuForLastK.get(u);
    }

    // These are used for memoization to not always calculate Math.pow(n, i).
    public static int currentN = 0;
    public static List<Long> valuesForPriority = new ArrayList<>();

    public static long getValueFromPriority(int i, int n) {
        if (currentN != n) {
            valuesForPriority = new ArrayList<>();
            currentN = n;
        }
        if (valuesForPriority.size() <= i) {
            for (int newI = valuesForPriority.size(); newI <= i; newI++){
                valuesForPriority.add(calculateValueFromPriority(newI, n));
            }
        }
        return valuesForPriority.get(i);
    }
    
    public static long calculateValueFromPriority(int i, int n) {
        return (long) ((i % 2 == 0) ? Math.pow(n, i) : -Math.pow(n, i));
    }
}
