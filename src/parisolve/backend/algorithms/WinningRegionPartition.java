package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * Helper class of RecursiveAlgorithm to return a partition of winning
 * regions. It helps to enforce that winning regions are assigned to the
 * right player. This brings clarification to the informal notion of a pair,
 * as used in
 */
class WinningRegionPartition {
    private final transient Map<Player, Collection<? extends ParityVertex>> partition = new ConcurrentHashMap<>();

    /**
     * create a partition of a set of vertices, which represent the winning
     * regions of a player and its opponent. They should be disjoint.
     * 
     * @param winningRegionForSigma
     *            winning region of player <code>sigma</code>
     * @param winningRegionForOponent
     *            winning region of player <code>sigma</code>'s opponent
     * @param sigma
     *            the player of the first winning region
     */
    public WinningRegionPartition(
            final Collection<? extends ParityVertex> winningRegionForSigma,
            final Collection<? extends ParityVertex> winningRegionForOponent,
            final Player sigma) {
        // TODO: test, whether the two sets are disjoint
        partition.put(sigma, winningRegionForSigma);
        partition.put(sigma.getOponent(), winningRegionForOponent);
    }

    /**
     * accessor method for the winning region of <code>player</code>
     * 
     * @param player
     *            the player to return the winning region for
     * @return <code>player</code>'s winning region
     */
    public Collection<? extends ParityVertex> getWinningRegionFor(
            final Player player) {
        return partition.get(player);
    }
}