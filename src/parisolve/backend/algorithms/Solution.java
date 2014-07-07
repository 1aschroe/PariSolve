package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * stores a winning region partition as well as a strategy enforcing these
 * regions.
 * 
 * @author Arne Schröder
 *
 */
public class Solution extends WinningRegionPartition {
    /**
     * singleton for empty set to not create an empty set for every instance
     * when one is needed.
     */
    protected static final Set<ParityVertex> EMPTY_SET = new HashSet<ParityVertex>();

    private final Map<ParityVertex, ParityVertex> strategy;

    /**
     * this constructor tells the solution that all vertices are won by player
     * sigma and that an arbitrary strategy can be used.
     * 
     * @param vertices
     * @param sigma
     */
    public Solution(final Set<ParityVertex> vertices, final Player sigma) {
        super(vertices, EMPTY_SET, sigma);
        strategy = getArbitraryStrategy(vertices);
    }

    /**
     * similar as the (Set, Player)-constructor but more convenient.
     * 
     * @param dominionPartition
     */
    public Solution(WinningRegionPartition partition) {
        super(partition);
        strategy = getArbitraryStrategy(getVertices());
    }

    public Solution(Set<ParityVertex> winningRegionFor,
            Set<ParityVertex> winningRegionOfSigmaOpponent,
            Player sigma, Map<ParityVertex, ParityVertex> strategy) {
        super(winningRegionFor, winningRegionOfSigmaOpponent, sigma);
        this.strategy = strategy;
    }

    public static Map<ParityVertex, ParityVertex> getArbitraryStrategy(
            final Set<ParityVertex> vertices) {
        Map<ParityVertex, ParityVertex> strategy = new HashMap<>();
        for (final ParityVertex vertex : vertices) {
            ParityVertex successor = vertex.getSuccessorFromSet(vertices);
            if (successor != null) {
                strategy.put(vertex, successor);
            }
        }
        return strategy;
    }

    public Map<ParityVertex, ParityVertex> getStrategy() {
        // TODO: make it immutable
        return strategy;
    }
}
